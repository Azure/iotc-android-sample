package com.azuresamples.azureadsampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.DeviceCredentials;
import com.github.lucadruda.iotcentral.service.DeviceTemplate;

import java.io.IOException;

public class ApplicationActivity extends Activity {

    private Application application;
    /*    private TextView scopeId;
        private TextView masterKey;
        private TextView deviceId;*/
    private Spinner modelsSpinner;
    private DeviceTemplate[] models;
    private Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity);
        application = (Application) getIntent().getSerializableExtra("app");

/*        scopeId = (TextView) findViewById(R.id.scopeId);
        masterKey = (TextView) findViewById(R.id.masterKey);
        deviceId = (TextView) findViewById(R.id.deviceId);*/
        modelsSpinner = (Spinner) findViewById(R.id.models);

        nextBtn = (Button) findViewById(R.id.next);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(android.view.View v) {
                Intent appIntent = new Intent(getActivity(), DeviceActivity.class);
                appIntent.putExtra("app", application);
                appIntent.putExtra("templateId", String.valueOf(modelsSpinner.getSelectedItem()));
                startActivity(appIntent);
            }
        });
        credThread.start();

    }

    Thread credThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                /*final DeviceCredentials credentials = IoTCentral.getDataClient().getCredentials(application.getId());*/
                models = IoTCentral.getDataClient().listTemplates(application.getId());
                final String[] ids = new String[models.length];
                for (int i = 0; i < models.length; i++) {
                    ids[i] = models[i].getId();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /*scopeId.setText(credentials.getIdScope());
                        masterKey.setText(credentials.getPrimaryKey());*/

                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, ids);
                        modelsSpinner.setAdapter(dataAdapter);
                    }
                });
                /*scopeId.setText(credentials.getIdScope());*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });


    public Activity getActivity() {
        return this;
    }
}
