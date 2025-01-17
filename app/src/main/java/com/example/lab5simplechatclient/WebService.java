package com.example.lab5simplechatclient;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

public class WebService extends AbstractModel {

    private static final String TAG = "ExampleWebServiceModel";

    private static final String USERNAME = "Daythyn";

    private static final String SERVICE_URL = "https://testbed.jaysnellen.com:8443/SimpleChat/board";

    private MutableLiveData<JSONObject> jsonData;

    private String outputText, message;

    private final ExecutorService requestThreadExecutor;
    private final Runnable httpGetRequestThread, httpPostRequestThread, httpClearRequestThread;
    private Future<?> pending;

    public WebService() {

        requestThreadExecutor = Executors.newSingleThreadExecutor();

        httpGetRequestThread = new Runnable() {

            @Override
            public void run() {

                /* If a previous request is still pending, cancel it */

                if (pending != null) { pending.cancel(true); }

                /* Begin new request now, but don't wait for it */

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("GET", SERVICE_URL, null));
                }
                catch (Exception e) { Log.e(TAG, " Exception: ", e); }

            }

        };

        httpPostRequestThread = new Runnable() {

            @Override
            public void run() {

                /* If a previous request is still pending, cancel it */

                if (pending != null) { pending.cancel(true); }

                /* Begin new request now, but don't wait for it */

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("POST", SERVICE_URL, message));
                }
                catch (Exception e) { Log.e(TAG, " Exception: ", e); }

            }

        };

        httpClearRequestThread = new Runnable() {

            @Override
            public void run() {

                /* If a previous request is still pending, cancel it */

                if (pending != null) { pending.cancel(true); }

                /* Begin new request now, but don't wait for it */

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("DELETE", SERVICE_URL, null));
                }
                catch (Exception e) { Log.e(TAG, " Exception: ", e); }

            }

        };

    }

    public void initDefault() {

        setOutputText("Click the button to send an HTTP GET request ...");

    }

    public String getOutputText() {
        return outputText;
    }

    public void setOutputText(String newText) {

        String oldText = this.outputText;
        this.outputText = newText;

        Log.i(TAG, "Output Text Change: From " + oldText + " to " + newText);

        firePropertyChange(DefaultController.ELEMENT_OUTPUT_PROPERTY, oldText, newText);

    }

    // Start GET Request (called from Controller)

    public void sendGetRequest() {
        httpGetRequestThread.run();
    }

    // Start POST Request (called from Controller)

    public void sendPostRequest(String message) {
        this.message = message;
        httpPostRequestThread.run();
    }

    //Clear Request
    public void sendClearRequest() {
        httpClearRequestThread.run();
    }

    // Setter / Getter Methods for JSON LiveData

    private void setJsonData(JSONObject json) {

        this.getJsonData().postValue(json);

        try {
            setOutputText(json.getString("messages"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public MutableLiveData<JSONObject> getJsonData() {
        if (jsonData == null) {
            jsonData = new MutableLiveData<>();
        }
        return jsonData;
    }

    // Private Class for HTTP Request Threads

    private class HTTPRequestTask implements Runnable {

        private static final String TAG = "HTTPRequestTask";
        private final String method, urlString;
        private String message = null;

        HTTPRequestTask(String method, String urlString, String message) {
            this.method = method;
            this.urlString = urlString;
            this.message = message;
        }

        @Override
        public void run() {
            JSONObject results = doRequest(urlString);
            setJsonData(results);
        }

        /* Create and Send Request */

        private JSONObject doRequest(String urlString) {

            StringBuilder r = new StringBuilder();
            String line;

            HttpURLConnection conn = null;
            JSONObject results = null;

            /* Log Request Data */

            try {

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Create Request */

                URL url = new URL(urlString);

                conn = (HttpURLConnection)url.openConnection();

                conn.setReadTimeout(10000); /* ten seconds */
                conn.setConnectTimeout(15000); /* fifteen seconds */

                conn.setRequestMethod(method);
                conn.setDoInput(true);

                /* Add Request Parameters (if any) */

                if (method.equals("POST") ) {

                    conn.setDoOutput(true);

                    // Create request parameters (these will be echoed back by the example API)
                    String p = "";
                    JSONObject json = new JSONObject();
                    json.put("name", USERNAME);
                    json.put("message", message);
                    p = json.toString();

                    // Write parameters to request body

                    OutputStream out = conn.getOutputStream();
                    out.write(p.getBytes());
                    out.flush();
                    out.close();

                }

                /* Send Request */

                conn.connect();

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Get Reader for Results */

                int code = conn.getResponseCode();

                if (code == HttpsURLConnection.HTTP_OK || code == HttpsURLConnection.HTTP_CREATED) {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    /* Read Response Into StringBuilder */

                    do {
                        line = reader.readLine();
                        if (line != null)
                            r.append(line);
                    }
                    while (line != null);

                }

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Parse Response as JSON */

                results = new JSONObject(r.toString());

            }
            catch (Exception e) {
                Log.e(TAG, " Exception: ", e);
            }
            finally {
                if (conn != null) { conn.disconnect(); }
            }

            /* Finished; Log and Return Results */

            Log.d(TAG, " JSON: " + r.toString());

            return results;

        }

    }

}

