package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Trace;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lucadruda.iotc.device.enums.IOTC_CONNECTION_STATE;
import com.github.lucadruda.iotcentral.adapters.GattAdapter;
import com.github.lucadruda.iotcentral.helpers.GattPair;
import com.github.lucadruda.iotcentral.helpers.LoadingAlert;
import com.github.lucadruda.iotcentral.helpers.TraceManager;
import com.github.lucadruda.iotcentral.services.BLEService;
import com.github.lucadruda.iotcentral.helpers.MappingStorage;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.services.DeviceService;

import java.util.HashMap;
import java.util.Random;

public class BLEActivity extends AppCompatActivity {


    private Application application;
    private String templateId;
    private String deviceName;
    private String deviceAddress;
    private ExpandableListView serviceList;
    private BLEService bleService;
    private DeviceService deviceService;
    private MappingStorage storage;
    private Button connectBtn;
    private LoadingAlert gattLoader, iotcLoader;
    private TextView logView;
    private TraceManager traceManager;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_service);
        deviceName = getIntent().getStringExtra(Constants.DEVICE_NAME);
        deviceAddress = getIntent().getStringExtra(Constants.DEVICE_ADDRESS);
        ((TextView) findViewById(R.id.device_address)).setText(deviceAddress);
        ((EditText) findViewById(R.id.deviceId)).setText(deviceName);
        if (getIntent().getBooleanExtra(Constants.DEVICE_EXISTS, false)) {
            ((EditText) findViewById(R.id.deviceId)).setEnabled(false);
        }
        connectBtn = findViewById(R.id.connectBLE);
        connectBtn.setOnClickListener(onConnectButtonClick());
        application = (Application) getIntent().getSerializableExtra(Constants.APPLICATION);
        templateId = (String) getIntent().getSerializableExtra(Constants.DEVICE_TEMPLATE_ID);
        serviceList = findViewById(R.id.gatt_services_list);
        logView = findViewById(R.id.logView);
        traceManager = new TraceManager(logView);
        storage = new MappingStorage(getApplicationContext(), deviceName);
        getSupportActionBar().setTitle(deviceName);
        gattLoader = new LoadingAlert(this, "Loading services");
        gattLoader.start();
        iotcLoader = new LoadingAlert(this, "Connecting device");
        // binding to background BLE service and GATT manager
        Intent bleServiceIntent = new Intent(this, BLEService.class);
        bindService(bleServiceIntent, bleServiceConnection, BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, getReceiverFilter());
        registerReceiver(updateReceiver, getReceiverFilter());

    }

/*    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(updateReceiver, getReceiverFilter());

        if (bleService != null) {
            final boolean result = bleService.connect(deviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (gattLoader.isStarted()) {
            gattLoader.stop();
        }
        if (iotcLoader.isStarted()) {
            iotcLoader.stop();
        }
    }*/

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

    @Override
    public void onBackPressed() {
        if (gattLoader.isStarted()) {
            gattLoader.stop();
        }
        super.onBackPressed();
    }

    private Activity getActivity() {
        return this;
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                invalidateOptionsMenu();
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                invalidateOptionsMenu();
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                gattLoader.stop();
                serviceList.setVisibility(View.VISIBLE);
                serviceList.setAdapter(new GattAdapter(getActivity(), deviceName, bleService.getSupportedGattServices(), IoTCentral.getMeasures(templateId)));
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
// data available
                retrieveData(intent.getStringExtra(BLEService.MEASURE_MAPPING_GATT), intent.getByteArrayExtra(BLEService.EXTRA_DATA));
            } else if (BLEService.TELEMETRY_ASSIGNED.equals(action)) {

                String gattPair = intent.getStringExtra(BLEService.MEASURE_MAPPING_GATT);
                String telemetryID = intent.getStringExtra(BLEService.MEASURE_MAPPING_IOTC);
                if (gattPair != null) {
                    if (telemetryID != null) {
                        storage.add(gattPair, telemetryID);
                        connectBtn.setEnabled(true);
                    } else {
                        storage.remove(gattPair);
                    }
                }
                if (storage.size() == 0) {
                    connectBtn.setEnabled(false);
                }
            } else if (Constants.IOTCENTRAL_DEVICE_CONNECTION_CHANGE.equals(action)) {
                if (Constants.IOTCENTRAL_DEVICE_CONNECTION_CHANGE.equals(action)) {
                    iotcLoader.stop();
                    if (intent.getStringExtra(Constants.IOTCENTRAL_DEVICE_CONNECTION_STATUS).equals(IOTC_CONNECTION_STATE.CONNECTION_OK.toString())) {
                        storage.setDeviceId(intent.getStringExtra(MappingStorage.DEVICE_ID));
                        storage.commit();
                        serviceList.setVisibility(View.GONE);
                        logView.setVisibility(View.VISIBLE);
                        initTelemetry();
                        findViewById(R.id.iconOK).setVisibility(View.VISIBLE);
                        findViewById(R.id.disconnectBLE).setVisibility(View.VISIBLE);
                        findViewById(R.id.connectBLE).setVisibility(View.GONE);

                    } else {
                        findViewById(R.id.iconFAIL).setVisibility(View.VISIBLE);
                        findViewById(R.id.disconnectBLE).setVisibility(View.GONE);
                        findViewById(R.id.connectBLE).setVisibility(View.VISIBLE);
                    }
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
        intentFilter.addAction(Constants.IOTCENTRAL_DEVICE_CONNECTION_CHANGE);
        return intentFilter;
    }

    private View.OnClickListener onConnectButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
// commit preferences
                storage.commit();
                iotcLoader.start();
                connectIoTCService();
                /* Intent deviceServiceIntent = new Intent(getApplicationContext(), DeviceService.class);
                connectionIntent.putExtra(Constants.DEVICE_NAME, deviceName);
                connectionIntent.putExtra(Constants.DEVICE_ADDRESS, deviceAddress);
                connectionIntent.putExtra(Constants.APPLICATION, application);
                connectionIntent.putExtra(Constants.DEVICE_TEMPLATE_ID, templateId);
                //startService(deviceServiceIntent);
                startActivity(connectionIntent);*/

            }
        };
    }

    private void connectIoTCService() {
        Intent iotcServiceIntent = new Intent(getApplicationContext(), DeviceService.class);
        iotcServiceIntent.putExtra(Constants.DEVICE_NAME, deviceName);
        iotcServiceIntent.putExtra(Constants.DEVICE_ADDRESS, deviceAddress);
        iotcServiceIntent.putExtra(Constants.APPLICATION, application);
        iotcServiceIntent.putExtra(Constants.DEVICE_TEMPLATE_ID, templateId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(iotcServiceIntent);
        } else {
            startService(iotcServiceIntent);
        }
        findViewById(R.id.disconnectBLE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceService.disconnectDevice();
            }
        });

        findViewById(R.id.connectBLE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceService.connectDevice();
            }
        });
    }

    private void initTelemetry() {
        traceManager.info("Starting telemetry initialization...");
        HashMap<String, String> mappings = storage.getAll();
        for (String gattPair : mappings.keySet()) {
            if (!gattPair.equals(MappingStorage.DEVICE_ID)) {
                traceManager.trace("Mapping char");
                bleService.setCharacteristicNotification(gattPair, true);
                bleService.readCharacteristic(gattPair);
            }
        }
        traceManager.info("Telemetry listeners registered!");
    }

    private void retrieveData(String gattKey, byte[] data) {
        String iotcField = storage.getIoTCTelemetry(gattKey);
        // parse value
        // deviceService.sendTelemetry(iotcField, data.toString());
        deviceService.sendTelemetry(iotcField, "" + (new Random().nextInt(100)));
    }
}
