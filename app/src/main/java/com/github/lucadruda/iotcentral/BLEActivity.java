package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lucadruda.iotcentral.adapters.GattAdapter;
import com.github.lucadruda.iotcentral.services.BLEService;
import com.github.lucadruda.iotcentral.helpers.MappingStorage;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.services.DeviceService;

public class BLEActivity extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private Application application;
    private String templateId;
    private String deviceName;
    private String deviceAddress;
    private ExpandableListView serviceList;
    private BLEService bleService;
    private MappingStorage storage;
    private Button connectBtn;


    private final ServiceConnection bleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // service connected. let's connect the BLE service
            bleService = ((BLEService.LocalBinder) service).getService();
            if (!bleService.initialize()) {
                Toast.makeText(getActivity(), R.string.ble_not_supported, Toast.LENGTH_LONG).show();
                finish();
            }
            bleService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };

    private final ServiceConnection deviceServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // service connected. let's connect the BLE service
            bleService = ((BLEService.LocalBinder) service).getService();
            if (!bleService.initialize()) {
                Toast.makeText(getActivity(), R.string.ble_not_supported, Toast.LENGTH_LONG).show();
                finish();
            }
            bleService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_service);
        deviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);
        ((TextView) findViewById(R.id.device_address)).setText(deviceAddress);
        connectBtn = findViewById(R.id.connectBLE);
        connectBtn.setOnClickListener(onConnectButtonClick());
        application = (Application) getIntent().getSerializableExtra(MainActivity.APPLICATION);
        templateId = (String) getIntent().getSerializableExtra(ApplicationActivity.DEVICE_TEMPLATE_ID);
        serviceList = findViewById(R.id.gatt_services_list);
        storage = new MappingStorage(this, deviceName);
        getSupportActionBar().setTitle((String) getIntent().getSerializableExtra(EXTRAS_DEVICE_NAME));

        // binding to background BLE service and GATT manager
        Intent bleServiceIntent = new Intent(this, BLEService.class);
        bindService(bleServiceIntent, bleServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, getReceiverFilter());
        if (bleService != null) {
            final boolean result = bleService.connect(deviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(bleServiceConnection);
        bleService = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Activity getActivity() {
        return this;
    }

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                invalidateOptionsMenu();
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                invalidateOptionsMenu();
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                serviceList.setAdapter(new GattAdapter(getActivity(), bleService.getSupportedGattServices(), IoTCentral.getMeasures(templateId)));
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
// data available
            } else if (BLEService.TELEMETRY_ASSIGNED.equals(action)) {

                String gattUUID = intent.getStringExtra(BLEService.MEASURE_MAPPING_GATT);
                String telemetryID = intent.getStringExtra(BLEService.MEASURE_MAPPING_IOTC);
                if (gattUUID != null) {
                    if (telemetryID != null) {
                        storage.add(gattUUID, telemetryID);
                        connectBtn.setEnabled(true);
                    } else {
                        storage.remove(gattUUID);
                    }
                }
                if (storage.size() == 0) {
                    connectBtn.setEnabled(false);
                }
            }
        }
    };

    private IntentFilter getReceiverFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BLEService.TELEMETRY_ASSIGNED);
        return intentFilter;
    }

    private View.OnClickListener onConnectButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
// commit preferences
                storage.commit();
                Intent deviceServiceIntent = new Intent(getApplicationContext(), DeviceService.class);
                bindService(deviceServiceIntent, bleServiceConnection, BIND_AUTO_CREATE);
            }
        };
    }
}
