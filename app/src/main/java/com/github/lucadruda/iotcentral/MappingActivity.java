package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.adapters.GattAdapter;
import com.github.lucadruda.iotcentral.adapters.IoTCAdapter;
import com.github.lucadruda.iotcentral.helpers.InputAlert;
import com.github.lucadruda.iotcentral.helpers.MappingStorage;
import com.github.lucadruda.iotcentral.helpers.TraceManager;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.Device;
import com.github.lucadruda.iotcentral.service.exceptions.DataException;
import com.github.lucadruda.iotcentral.service.types.Measure;
import com.github.lucadruda.iotcentral.services.BLEService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MappingActivity extends BaseActivity {
    private Device device;
    private String deviceAddress;
    private Button saveBtn;
    private MappingStorage storage;
    private ExpandableListView serviceList;
    private HashMap<String, List<String>> serviceMap;
    private List<Measure> measures;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapping_activity);
        device = (Device) getIntent().getSerializableExtra(Constants.DEVICE);
        deviceAddress = getIntent().getStringExtra(Constants.DEVICE_ADDRESS);
        serviceMap = (HashMap<String, List<String>>) getIntent().getSerializableExtra(Constants.BLE_SERVICES_MAP);
        measures = (List<Measure>) getIntent().getSerializableExtra(Constants.MEASURES);
        storage = new MappingStorage(getApplicationContext(), device.getDeviceId());
        setTitle(device.getName());
        ((TextView) findViewById(R.id.deviceName)).setText(device.getName());
        ((TextView) findViewById(R.id.device_address)).setText(deviceAddress);
        saveBtn = findViewById(R.id.saveMapping);
        saveBtn.setOnClickListener(getClickListener(Activity.RESULT_OK));
        saveBtn.setEnabled(storage.size() > 0);
        findViewById(R.id.discardMapping).setOnClickListener(getClickListener(Activity.RESULT_CANCELED));
        serviceList = findViewById(R.id.gatt_services_list);
        serviceList.setAdapter(new GattAdapter(getActivity(), storage, serviceMap, measures));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // binding to background BLE service and GATT manager


    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mappingReceiver, getReceiverFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mappingReceiver);
    }

    private View.OnClickListener getClickListener(final int result) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                if (result == RESULT_OK) {
                    storage.commit();
                }
                setResult(result, returnIntent);
                finish();
            }
        };
    }

    private BroadcastReceiver mappingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.TELEMETRY_ASSIGNED)) {
                String gattPair = intent.getStringExtra(Constants.MEASURE_MAPPING_GATT_PAIR);
                String telemetryID = intent.getStringExtra(Constants.MEASURE_MAPPING_IOTC);
                if (gattPair != null) {
                    if (telemetryID != null) {
                        storage.add(gattPair, telemetryID);
                    } else {
                        storage.remove(gattPair);
                    }
                }
                saveBtn.setEnabled(storage.size() > 0);
            } else if (intent.getAction().equals(Constants.TELEMETRY_REFRESHED)) {
                notificationAlert.show("A new mapping has been set on the cloud. Refresh");
                storage = new MappingStorage(getApplicationContext(), device.getDeviceId());
                serviceList.invalidate();
                serviceList.setAdapter(new GattAdapter(getActivity(), storage, serviceMap, measures));
            }
        }
    };

    private IntentFilter getReceiverFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.TELEMETRY_ASSIGNED);
        intentFilter.addAction(Constants.TELEMETRY_REFRESHED);
        return intentFilter;
    }
}