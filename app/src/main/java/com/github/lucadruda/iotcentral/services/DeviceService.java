package com.github.lucadruda.iotcentral.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class DeviceService extends Service {
    private final IBinder mBinder = new DeviceService.LocalBinder();

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
}
