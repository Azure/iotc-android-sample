package com.github.lucadruda.iotcentral.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RadioButton;
import android.widget.Toast;

import com.github.lucadruda.iotc.device.IoTCClient;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECT;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECTION_STATE;
import com.github.lucadruda.iotc.device.exceptions.IoTCentralException;
import com.github.lucadruda.iotcentral.Constants;
import com.github.lucadruda.iotcentral.IoTCentral;
import com.github.lucadruda.iotcentral.MainActivity;
import com.github.lucadruda.iotcentral.R;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.DeviceCredentials;

import java.io.IOException;

public class DeviceService extends Service {

    public static final String IOTCENTRAL_CONNECTION = "IOTCENTRAL_CONNECTION";
    public static final String IOTCENTRAL_DEVICENAME = "IOTCENTRAL_DEVICENAME";
    public static final String IOTCENTRAL_DEVICE_CONNECTION_STATUS = "IOTCENTRAL_DEVICE_CONNECTION_STATUS";
    public static final String IOTCENTRAL_DEVICE_CONNECTION_CHANGE = "IOTCENTRAL_DEVICE_CONNECTION_CHANGE";

    private String deviceId;
    private IoTCClient iotcClient;
    private Application application;
    private DeviceCredentials credentials;
    private LocalBroadcastManager broadcastManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent.getAction().equals(IOTCENTRAL_CONNECTION)) {
            broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
            deviceId = intent.getStringExtra(IOTCENTRAL_DEVICENAME);
            application = (Application) intent.getSerializableExtra(Constants.APPLICATION);
            Intent connChangedIntent = new Intent(IOTCENTRAL_DEVICE_CONNECTION_CHANGE);
            try {
                credentials = IoTCentral.getDataClient().getCredentials(application.getId());
                iotcClient = new IoTCClient(deviceId, credentials.getIdScope(), IOTC_CONNECT.SYMM_KEY, credentials.getPrimaryKey());
                iotcClient.Connect();
                broadcastManager.sendBroadcast(connChangedIntent);
                connChangedIntent.putExtra(IOTCENTRAL_DEVICE_CONNECTION_STATUS, IOTC_CONNECTION_STATE.CONNECTION_OK);


            } catch (IOException | IoTCentralException e) {
                connChangedIntent.putExtra(IOTCENTRAL_DEVICE_CONNECTION_STATUS, IOTC_CONNECTION_STATE.COMMUNICATION_ERROR);
                broadcastManager.sendBroadcast(connChangedIntent);
            }
        }
        return START_STICKY;
    }

    private void sendTelemetry() {
    }

}
