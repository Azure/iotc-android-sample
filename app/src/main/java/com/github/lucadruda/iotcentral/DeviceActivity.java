package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import com.github.lucadruda.iotc.device.IoTCClient;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECT;
import com.github.lucadruda.iotc.device.exceptions.IoTCentralException;
import com.github.lucadruda.iotcentral.adapters.IoTCAdapter;
import com.github.lucadruda.iotcentral.helpers.LoadingAlert;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.Device;
import com.github.lucadruda.iotcentral.service.DeviceCredentials;
import com.github.lucadruda.iotcentral.service.exceptions.DataException;
import com.github.lucadruda.iotcentral.service.types.DeviceTemplate;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DeviceActivity extends AppCompatActivity {

    private Application application;
    private String templateId;
    private Device[] devices;
    private RecyclerView scannedView;
    private LoadingAlert templateLoader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_activity);
        application = (Application) getIntent().getSerializableExtra(Constants.APPLICATION);
        templateId = (String) getIntent().getSerializableExtra(Constants.DEVICE_TEMPLATE_ID);
        getSupportActionBar().setTitle(application.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        scannedView = findViewById(R.id.modelsView);
        scannedView.setHasFixedSize(true);
        scannedView.setLayoutManager(new LinearLayoutManager(getActivity()));
        templateLoader = new LoadingAlert(this, "Loading devices");
        templateLoader.start();
        findViewById(R.id.newDevice).setOnClickListener(getOnClickListener(false));
        iotcThread.start();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

        }
        return true;
    }

    Thread iotcThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                devices = IoTCentral.getDataClient().listDevices(application.getId(), templateId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        IoTCAdapter dataAdapter = new IoTCAdapter(getActivity(), devices, getOnClickListener(true));
                        scannedView.setAdapter(dataAdapter);
                        templateLoader.stop();
                    }
                });
            } catch (DataException e) {
                e.printStackTrace();
            }
        }
    });

    public Activity getActivity() {
        return this;
    }

    private View.OnClickListener getOnClickListener(final boolean deviceId) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Device device = (Device) v.getTag();
                if (device == null) {
                    return;
                }
                Intent appIntent = new Intent(getActivity(), DeviceScanActivity.class);
                appIntent.putExtra(Constants.APPLICATION, application);
                appIntent.putExtra(Constants.DEVICE_TEMPLATE_ID, templateId);
                if (deviceId) {
                    appIntent.putExtra(Constants.DEVICE_NAME, device.getDeviceId());
                    appIntent.putExtra(Constants.DEVICE_EXISTS, true);
                }
                startActivity(appIntent);
            }
        };

    }
}
