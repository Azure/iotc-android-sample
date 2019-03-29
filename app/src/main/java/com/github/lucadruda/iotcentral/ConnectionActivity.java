package com.github.lucadruda.iotcentral;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.lucadruda.iotc.device.enums.IOTC_CONNECTION_STATE;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.services.BLEService;
import com.github.lucadruda.iotcentral.services.DeviceService;

public class ConnectionActivity extends AppCompatActivity {

    private String deviceName;
    private String deviceAddress;
    private Application application;
    private String templateId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        deviceName = getIntent().getStringExtra(Constants.DEVICE_NAME);
        deviceAddress = getIntent().getStringExtra(Constants.DEVICE_ADDRESS);
        ((TextView) findViewById(R.id.device_address)).setText(deviceAddress);
        application = (Application) getIntent().getSerializableExtra(Constants.APPLICATION);
        templateId = (String) getIntent().getSerializableExtra(Constants.DEVICE_TEMPLATE_ID);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, new IntentFilter(DeviceService.IOTCENTRAL_DEVICE_CONNECTION_CHANGE));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DeviceService.IOTCENTRAL_DEVICE_CONNECTION_CHANGE.equals(action)) {
                findViewById(R.id.connectionProgress).setVisibility(View.GONE);
                if (intent.getStringExtra(DeviceService.IOTCENTRAL_DEVICE_CONNECTION_STATUS).equals(IOTC_CONNECTION_STATE.CONNECTION_OK)) {
                    findViewById(R.id.iconOK).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.iconFAIL).setVisibility(View.VISIBLE);
                }
            }
        }
    };
};
