package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lucadruda.iotc.device.enums.IOTC_CONNECTION_STATE;
import com.github.lucadruda.iotcentral.adapters.GattAdapter;
import com.github.lucadruda.iotcentral.helpers.GattPair;
import com.github.lucadruda.iotcentral.helpers.TraceManager;
import com.github.lucadruda.iotcentral.service.Device;
import com.github.lucadruda.iotcentral.services.BLEService;
import com.github.lucadruda.iotcentral.helpers.MappingStorage;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.services.DeviceService;
import com.github.lucadruda.iotcentral.targets.Feature;
import com.github.lucadruda.iotcentral.targets.Targets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class BLEActivity extends BaseActivity {


    private Application application;
    private String templateId;
    private BLEService bleService;
    private DeviceService deviceService;
    private Device device;
    private String deviceAddress;
    private MappingStorage storage;
    private Button connectBtn;
    private Button readBtn;
    private TextView logView;
    private TraceManager traceManager;
    private boolean isRunning = false;
    private HashMap<String, List<String>> serviceMap;
    private final int DEVICE_SCAN_REQUEST = 1;
    private final int DEVICE_MAPPING_REQUEST = 2;

    private final ServiceConnection bleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // service connected. let's connect the BLE service
            bleService = ((BLEService.LocalBinder) service).getService();
            if (!bleService.initialize()) {
                Toast.makeText(getActivity(), R.string.ble_not_supported, Toast.LENGTH_LONG).show();
                finish();
            }
            bleService.connect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };

    private final ServiceConnection iotcServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // service connected. let's connect the BLE service
            deviceService = ((DeviceService.LocalBinder) service).getService();
            deviceService.initialize();
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
        device = (Device) getIntent().getSerializableExtra(Constants.DEVICE);
        application = (Application) getIntent().getSerializableExtra(Constants.APPLICATION);
        templateId = (String) getIntent().getSerializableExtra(Constants.DEVICE_TEMPLATE_ID);

        storage = new MappingStorage(getApplicationContext(), device.getDeviceId());
        setTitle(application.getName());

// CONNECT DEVICE TO IOTCENTRAL FIRST
        connectIoTCService();
        readBtn = findViewById(R.id.readBLE);
        readBtn.setVisibility(View.INVISIBLE);
        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readData();
            }
        });

        logView = findViewById(R.id.logView);
        traceManager = new TraceManager(logView);

        findViewById(R.id.disconnectBLE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceService.disconnectDevice();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isRunning = true;
        // binding to background BLE service and GATT manager


    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(iotcReceiver, getIoTCReceiverFilter());
        registerReceiver(gattReceiver, getGattReceiverFilter());
        isRunning = true;
        if (bleService == null && deviceAddress != null) {
            connectBLEService(deviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattReceiver);
        isRunning = false;

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (bleService != null) {
            unbindService(bleServiceConnection);
        }
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
        if (loadingAlert.isStarted()) {
            loadingAlert.stop();
        }
        super.onBackPressed();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(iotcReceiver);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == DEVICE_SCAN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                deviceAddress = data.getStringExtra(Constants.DEVICE_ADDRESS);
                findViewById(R.id.deviceInfo).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.device_address)).setText(deviceAddress);
                ((TextView) findViewById(R.id.deviceName)).setText(device.getName());
                connectBLEService(deviceAddress);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // user pressed back button at scan
                deviceAddress = null;
                finish();
            }
        }
        if (requestCode == DEVICE_MAPPING_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                // mapping has been saved in storage. update value on cloud
                //refresh storage
                storage = new MappingStorage(getApplicationContext(), device.getDeviceId());
                deviceService.syncMapping(storage.getJsonString(), storage.getJsonVersion());
                initTelemetry();
                readBtn.setVisibility(View.VISIBLE);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
// if mapping has been canceled. scan again
                deviceAddress = null;
                launchScan();
            }
        }
    }

    private final BroadcastReceiver gattReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BLEService.ACTION_GATT_CONNECTED:
                    break;
                case BLEService.ACTION_GATT_DISCONNECTED:
                    break;
                case BLEService.ACTION_GATT_SERVICES_DISCOVERED:
                    // start the mapping activity
                    // Show all the supported services and characteristics on the user interface.
                    loadingAlert.stop("Services fetched!");
                    serviceMap = new HashMap<>();
                    for (BluetoothGattService service : bleService.getSupportedGattServices()) {
                        List<String> chars = new ArrayList();
                        for (BluetoothGattCharacteristic bleChar : service.getCharacteristics()) {
                            chars.add(bleChar.getUuid().toString());
                        }
                        serviceMap.put(service.getUuid().toString(), chars);
                    }

                    launchMapping();
                    break;
                case BLEService.ACTION_DATA_AVAILABLE:
                    // data available
                    retrieveData(intent.getStringExtra(Constants.MEASURE_MAPPING_GATT_PAIR), intent.getByteArrayExtra(BLEService.EXTRA_DATA));
                    break;
            }
        }
    };

    private final BroadcastReceiver iotcReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case Constants.IOTCENTRAL_DEVICE_CONNECTION_CHANGE:
                    if (intent.getStringExtra(Constants.IOTCENTRAL_DEVICE_CONNECTION_STATUS).equals(IOTC_CONNECTION_STATE.CONNECTION_OK.toString())) {
                        // just connected. open scan activity to select a device
                        loadingAlert.stop(getString(R.string.connected));
                        // launch scan
                        launchScan();
                    } else {
                        loadingAlert.stop(getString(R.string.disconnected));
                        stopTelemetry();
                        readBtn.setVisibility(View.GONE);
                        findViewById(R.id.disconnectBLE).setVisibility(View.GONE);
                        findViewById(R.id.connectBLE).setVisibility(View.VISIBLE);
                    }
                    break;
                case Constants.IOTCENTRAL_MAPPING_CHANGE:
                    String cmdPayload = intent.getStringExtra(Constants.MAPPING_PAYLOAD);
                    MappingStorage currentCloudStorage = MappingStorage.getFromCommandPayload(getApplicationContext(), device.getDeviceId(), cmdPayload);
                    if (currentCloudStorage.getMappingVersion() > storage.getMappingVersion()) {
                        // version on cloud is higher. let's substitute
                        storage = currentCloudStorage;
                        storage.commit();
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constants.TELEMETRY_REFRESHED));
                        if (BLEActivity.this.isRunning) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notificationAlert.show("A new mapping has been set on the cloud. Refresh", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            stopTelemetry();
                                            launchMapping();
                                        }
                                    });

                                }
                            });

                        }
                        deviceService.syncMapping(storage.getJsonString(), storage.getJsonVersion());
                    }
                    break;
                case Constants.IOTCENTRAL_COMMAND_RECEIVED:
                    onCommandReceived(intent.getStringExtra(Constants.IOTCENTRAL_COMMAND_TEXT));
                    break;
            }
        }
    };

    private IntentFilter getGattReceiverFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private IntentFilter getIoTCReceiverFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.IOTCENTRAL_DEVICE_CONNECTION_CHANGE);
        intentFilter.addAction(Constants.IOTCENTRAL_COMMAND_RECEIVED);
        intentFilter.addAction(Constants.IOTCENTRAL_MAPPING_CHANGE);
        return intentFilter;
    }

    private View.OnClickListener onConnectButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
// commit preferences
                storage.commit();
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
        loadingAlert.start("Connecting to IoTCentral...");
        if (deviceService == null) {
            Intent iotcServiceIntent = new Intent(getApplicationContext(), DeviceService.class);
            iotcServiceIntent.putExtra(Constants.DEVICE, device);
            iotcServiceIntent.putExtra(Constants.APPLICATION, application);
            iotcServiceIntent.putExtra(Constants.DEVICE_TEMPLATE_ID, templateId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(iotcServiceIntent);
            } else {
                startService(iotcServiceIntent);
            }
            bindService(iotcServiceIntent, iotcServiceConnection, BIND_AUTO_CREATE);
        } /*else {
            deviceService.initialize();
        }*/
    }

    private void connectBLEService(String deviceAddress) {
        loadingAlert.start("Connecting to Bluetooth device...");
        if (bleService == null) {
            Intent bleServiceIntent = new Intent(this, BLEService.class);
            bleServiceIntent.putExtra(Constants.DEVICE_ADDRESS, deviceAddress);
            bindService(bleServiceIntent, bleServiceConnection, BIND_AUTO_CREATE);
        } else {
            bleService.connect();
        }
    }


    private void initTelemetry() {
        traceManager.info("Starting telemetry initialization...");
        HashMap<String, String> mappings = storage.getAll();
        for (String gattPair : mappings.keySet()) {
            if (!gattPair.equals(MappingStorage.MAPPING_VERSION)) {
                traceManager.trace("Mapping char");
                bleService.setupCharacteristic(gattPair, true);
            }
        }
        traceManager.info("Telemetry listeners registered!");
    }

    private void launchScan() {
        Intent scanIntent = new Intent(getActivity(), DeviceScanActivity.class);
        scanIntent.putExtra(Constants.APPLICATION, application);
        startActivityForResult(scanIntent, DEVICE_SCAN_REQUEST);
    }

    private void launchMapping() {
        if (serviceMap.size() > 0) {
            Intent mappingIntent = new Intent(getActivity(), MappingActivity.class);
            mappingIntent.putExtra(Constants.BLE_SERVICES_MAP, serviceMap);
            mappingIntent.putExtra(Constants.DEVICE, device);
            mappingIntent.putExtra(Constants.DEVICE_ADDRESS, deviceAddress);
            mappingIntent.putExtra(Constants.MEASURES, (Serializable) IoTCentral.getMeasures(templateId));
            startActivityForResult(mappingIntent, DEVICE_MAPPING_REQUEST);
        }
    }

    private void stopTelemetry() {
        traceManager.info("Stopping telemetry...");
        HashMap<String, String> mappings = storage.getAll();
        for (String gattPair : mappings.keySet()) {
            if (!gattPair.equals(MappingStorage.MAPPING_VERSION)) {
                traceManager.trace("Mapping char");
                bleService.setupCharacteristic(gattPair, false);
            }
        }
        traceManager.info("Telemetry listeners stopped!");
    }

    private void readData() {
        bleService.readCharacteristics();
    }

    private void retrieveData(String gattKey, byte[] data) {
        String iotcField = storage.getIoTCTelemetry(gattKey);
        Feature feature = Targets.featureslookup(new GattPair(gattKey).getCharacteristicUUID().toString());
        // parse value
        // deviceService.sendTelemetry(iotcField, data.toString());
        float val = feature.getData(data);
        traceManager.trace("Sending " + iotcField + " = " + val);
        deviceService.sendTelemetry(iotcField, "" + val);
    }

    private void onCommandReceived(String text) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "COMMAND_CHANNEL",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("COMMAND_DESCRIPTOR");
            mNotificationManager.createNotificationChannel(channel);
        }
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_cloud_circle_black_24dp);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.drawable.ic_cloud_circle_black_24dp) // notification icon
                .setLargeIcon(bm) // notification icon
                .setContentTitle("Azure IoTCentral") // title for notification
                .setContentText(text)// message for notification
                .setAutoCancel(true); // clear notification after click
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(new Random().nextInt(100), mBuilder.build());
    }
}
