package com.github.lucadruda.iotcentral.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RadioButton;
import android.widget.Toast;

import com.github.lucadruda.iotc.device.Command;
import com.github.lucadruda.iotc.device.IoTCClient;
import com.github.lucadruda.iotc.device.callbacks.IoTCCallback;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECT;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECTION_STATE;
import com.github.lucadruda.iotc.device.enums.IOTC_EVENTS;
import com.github.lucadruda.iotc.device.exceptions.IoTCentralException;
import com.github.lucadruda.iotcentral.Constants;
import com.github.lucadruda.iotcentral.IoTCentral;
import com.github.lucadruda.iotcentral.MainActivity;
import com.github.lucadruda.iotcentral.R;
import com.github.lucadruda.iotcentral.helpers.MappingStorage;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.DataClient;
import com.github.lucadruda.iotcentral.service.Device;
import com.github.lucadruda.iotcentral.service.DeviceCredentials;
import com.github.lucadruda.iotcentral.service.exceptions.DataException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class DeviceService extends Service {


    private String deviceName;
    private String modelId;
    private IoTCClient iotcClient;
    private Application application;
    private DeviceCredentials credentials;
    private LocalBroadcastManager broadcastManager;
    private DataClient iotcentral;
    private Intent connChangedIntent;
    private Intent cmdReceivedIntent;
    private Device device;


    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        // return the current instance. The service intent is responsible to instantiate the class
        public DeviceService getService() {
            return DeviceService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        deviceName = intent.getStringExtra(Constants.DEVICE_NAME);
        application = (Application) intent.getSerializableExtra(Constants.APPLICATION);
        modelId = intent.getStringExtra(Constants.DEVICE_TEMPLATE_ID);
        connChangedIntent = new Intent(Constants.IOTCENTRAL_DEVICE_CONNECTION_CHANGE);
        cmdReceivedIntent = new Intent(Constants.IOTCENTRAL_COMMAND_RECEIVED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initChannels(this);
            Notification.Builder builder = new Notification.Builder(this, "default")
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.app_running))
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(1, notification);
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.app_running))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1, notification);
        }
        return START_STICKY;
    }


    private void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }

    private void sendTelemetry() {
    }

    public void initialize() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    iotcentral = IoTCentral.getDataClient();
                    credentials = iotcentral.getCredentials(application.getId());
                    try {
                        device = iotcentral.createDevice(application.getId(), deviceName, modelId);
                    } catch (DataException e) {
                        if (e.getCode().equals(DataException.IOTCENTRAL_DATA_EXCEPTION_CODES.CONFLICT)) {
                            // device already exists
                            device = iotcentral.getDeviceById(application.getId(), deviceName.toLowerCase());
                        }
                    }
                    if (device != null) {
                        iotcClient = new IoTCClient(device.getDeviceId(), credentials.getIdScope(), IOTC_CONNECT.SYMM_KEY, credentials.getPrimaryKey());
                        connectDevice();
                    } else {
                        connChangedIntent.putExtra(Constants.IOTCENTRAL_DEVICE_CONNECTION_STATUS, IOTC_CONNECTION_STATE.COMMUNICATION_ERROR);
                        broadcastManager.sendBroadcast(connChangedIntent);
                    }
                } catch (DataException e) {
                    connChangedIntent.putExtra(Constants.IOTCENTRAL_DEVICE_CONNECTION_STATUS, IOTC_CONNECTION_STATE.COMMUNICATION_ERROR);
                    broadcastManager.sendBroadcast(connChangedIntent);
                }
            }
        }).start();
    }

    protected void connectDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    iotcClient.Connect();
                    iotcClient.on(IOTC_EVENTS.Command, handleCommand());
                    connChangedIntent.putExtra(Constants.IOTCENTRAL_DEVICE_CONNECTION_STATUS, IOTC_CONNECTION_STATE.CONNECTION_OK.toString());
                    connChangedIntent.putExtra(MappingStorage.DEVICE_ID, device.getDeviceId());
                    broadcastManager.sendBroadcast(connChangedIntent);

                } catch (IoTCentralException e) {
                    connChangedIntent.putExtra(Constants.IOTCENTRAL_DEVICE_CONNECTION_STATUS, IOTC_CONNECTION_STATE.COMMUNICATION_ERROR);
                    broadcastManager.sendBroadcast(connChangedIntent);
                }
            }
        }).start();
    }

    public void disconnectDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    iotcClient.Disconnect(new IoTCCallback() {
                        @Override
                        public void Exec(Object result) {
                            connChangedIntent.putExtra(Constants.IOTCENTRAL_DEVICE_CONNECTION_STATUS, "Disconnected");
                            connChangedIntent.putExtra(MappingStorage.DEVICE_ID, device.getDeviceId());
                            broadcastManager.sendBroadcast(connChangedIntent);
                        }
                    });
                } catch (IoTCentralException e) {
                    connChangedIntent.putExtra(Constants.IOTCENTRAL_DEVICE_CONNECTION_STATUS, IOTC_CONNECTION_STATE.COMMUNICATION_ERROR);
                    broadcastManager.sendBroadcast(connChangedIntent);
                }
            }
        }).start();
    }

    public void sendTelemetry(String key, String value) {
        iotcClient.SendTelemetry(String.format("{\"%s\":\"%s\"}", key, value), null);
    }

    private IoTCCallback handleCommand() {
        return new IoTCCallback() {
            @Override
            public void Exec(Object result) {
                Command cmd = null;
                if (result instanceof Command) {
                    cmd = (Command) result;
                }
                try {
                    JsonParser parser = new JsonParser();
                    int vers = ((JsonObject) parser.parse(cmd.getPayload())).get("vers").getAsInt();
                    cmdReceivedIntent.putExtra(Constants.IOTCENTRAL_COMMAND_TEXT, "A new firmware is available. Version: " + vers);
                    broadcastManager.sendBroadcast(cmdReceivedIntent);
                    iotcClient.SendProperty(cmd.getResponseObject("Command received by " + deviceName), null);
                } catch (IoTCentralException e) {
                    e.printStackTrace();
                }
            }
        };
    }


}
