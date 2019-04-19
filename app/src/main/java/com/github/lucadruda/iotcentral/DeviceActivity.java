package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
    private RecyclerView devicesView;
    private LoadingAlert templateLoader;
    private FloatingActionButton newDeviceBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        application = (Application) getIntent().getSerializableExtra(Constants.APPLICATION);
        templateId = (String) getIntent().getSerializableExtra(Constants.DEVICE_TEMPLATE_ID);
        getSupportActionBar().setTitle(application.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        devicesView = findViewById(R.id.listView);
        devicesView.setHasFixedSize(true);
        devicesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        templateLoader = new LoadingAlert(this, "Loading devices");
        templateLoader.start();
        ((TextView) findViewById(R.id.listTitle)).setText("Devices");
        findViewById(R.id.floatingBox).setVisibility(View.VISIBLE);
        newDeviceBtn = findViewById(R.id.addBtn);
        newDeviceBtn.setOnClickListener(getOnClickListener(false));
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
                        devicesView.setAdapter(dataAdapter);
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

    private View.OnClickListener getOnClickListener(final boolean exists) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appIntent = new Intent(getActivity(), DeviceScanActivity.class);
                appIntent.putExtra(Constants.APPLICATION, application);
                appIntent.putExtra(Constants.DEVICE_TEMPLATE_ID, templateId);
                if (exists) {
                    final Device device = (Device) v.getTag();
                    if (device == null) {
                        return;
                    }
                    appIntent.putExtra(Constants.DEVICE_NAME, device.getDeviceId());
                    appIntent.putExtra(Constants.DEVICE_EXISTS, true);
                }
                startActivity(appIntent);
            }
        };

    }
}
