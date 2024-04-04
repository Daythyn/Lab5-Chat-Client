package com.example.lab5simplechatclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.lab5simplechatclient.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;

public class MainActivity extends AppCompatActivity implements AbstractView {

    public static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private DefaultController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        /* Create Controller and Models */

        controller = new DefaultController();
        WebService model = new WebService();

        /* Register Activity View and Model with Controller */

        controller.addView(this);
        controller.addModel(model);

        /* Initialize Model to Default Values */

        model.initDefault();

        binding.clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.sendClearRequest();
            }
        });

        binding.postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.sendPostRequest(binding.inputTextField.getText().toString());
            }
        });

        controller.sendGetRequest();

    }

    @Override
    public void modelPropertyChange(final PropertyChangeEvent evt) {

        String propertyName = evt.getPropertyName();
        String propertyValue = evt.getNewValue().toString();

        Log.i(TAG, "New " + propertyName + " Value from Model: " + propertyValue);

        if ( propertyName.equals(DefaultController.ELEMENT_OUTPUT_PROPERTY) ) {

            String oldPropertyValue = binding.outputTextField.getText().toString();

            if ( !oldPropertyValue.equals(propertyValue) ) {

                binding.outputTextField.setText(propertyValue);
            }

        }

    }

}