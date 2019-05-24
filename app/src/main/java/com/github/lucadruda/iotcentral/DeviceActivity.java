package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.github.lucadruda.iotc.device.IoTCClient;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECT;
import com.github.lucadruda.iotc.device.exceptions.IoTCentralException;
import com.github.lucadruda.iotcentral.adapters.IoTCAdapter;
import com.github.lucadruda.iotcentral.helpers.InputAlert;
import com.github.lucadruda.iotcentral.helpers.LoadingAlert;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.Device;
import com.github.lucadruda.iotcentral.service.DeviceCredentials;
import com.github.lucadruda.iotcentral.service.exceptions.DataException;
import com.github.lucadruda.iotcentral.service.types.DeviceTemplate;
import com.github.lucadruda.iotcentral.services.DeviceService;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DeviceActivity extends BaseActivity {

    private Application application;
    private String templateId;
    private Device[] devices;
    private RecyclerView devicesView;
    private FloatingActionButton newDeviceBtn;
    private Device device;
    private DeviceService deviceService;


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
        loadingAlert.start("Loading devices");
        ((TextView) findViewById(R.id.listTitle)).setText("Devices");
        findViewById(R.id.floatingBox).setVisibility(View.VISIBLE);
        newDeviceBtn = findViewById(R.id.addBtn);
        newDeviceBtn.setOnClickListener(getOnClickListener());
        listThread.start();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

        }
        return true;
    }

    Thread listThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                devices = dataClient.listDevices(application.getId(), templateId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        IoTCAdapter dataAdapter = new IoTCAdapter(getActivity(), devices, getOnClickListener());
                        devicesView.setAdapter(dataAdapter);
                        loadingAlert.stop();
                    }
                });
            } catch (DataException e) {
                e.printStackTrace();
            }
        }
    });

    private Thread getCreationDeviceThread(final String deviceName, final String deviceId) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingAlert.start("Creating device \"" + deviceName + "\"");
                        }
                    });
                    device = dataClient.createDevice(application.getId(), deviceName, templateId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingAlert.stop();
                            startServiceActivity();
                        }
                    });
                } catch (DataException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public Activity getActivity() {
        return this;
    }

    private View.OnClickListener getOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == newDeviceBtn) {
                    new InputAlert(getActivity(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = ((EditText) ((AlertDialog) dialog).findViewById(Constants.INPUTDIALOG_ID)).getText().toString();
                            getCreationDeviceThread(name, name.toLowerCase().replace(' ', '-')).start();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, getString(R.string.create_device)).start();
                } else {
                    //existing device
                    device = (Device) v.getTag();
                    startServiceActivity();
                }
            }
        };

    }

    private void startServiceActivity() {
        Intent appIntent = new Intent(getActivity(), BLEActivity.class);
        appIntent.putExtra(Constants.APPLICATION, application);
        appIntent.putExtra(Constants.DEVICE, device);
        appIntent.putExtra(Constants.DEVICE_TEMPLATE_ID, templateId);
        startActivity(appIntent);
    }
}
