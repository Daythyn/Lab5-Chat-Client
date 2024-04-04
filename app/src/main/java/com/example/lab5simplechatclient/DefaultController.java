package com.example.lab5simplechatclient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DefaultController extends AbstractController {

    public static final String ELEMENT_OUTPUT_PROPERTY = "Output";

    public void changeOutputText(String newText) {
        setModelProperty(ELEMENT_OUTPUT_PROPERTY, newText);
    }

    public void sendGetRequest() {
        invokeModelMethod("sendGetRequest", null);
    }

    public void sendClearRequest() {
        invokeModelMethod("sendClearRequest", null);
    }

    public void sendPostRequest(String message) {

        invokeModelMethod("sendPostRequest", message);
    }

}
