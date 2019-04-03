package com.github.lucadruda.iotcentral.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.github.lucadruda.iotcentral.helpers.GattPair;

import java.util.List;
import java.util.UUID;

public class BLEService extends Service {

    private BluetoothManager blManager;
    private BluetoothAdapter blAdapter;
    private String deviceAddress;
    private BluetoothGatt blGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static String TELEMETRY_ASSIGNED =
            "TELEMETRY_ASSIGNED";

    public final static String MEASURE_MAPPING_GATT = "MEASURE_MAPPING_GATT";
    public final static String MEASURE_MAPPING_IOTC = "MEASURE_MAPPING_IOTC";

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        // return the current instance. The service intent is responsible to instantiate the class
        public BLEService getService() {
            return BLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                blGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
           /* final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));*/
            intent.putExtra(MEASURE_MAPPING_GATT, new GattPair(characteristic).getKey());
            intent.putExtra(EXTRA_DATA, data);
        }
        sendBroadcast(intent);
    }


    public boolean initialize() {

        if (blManager == null) {
            blManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (blManager == null) {
                //Unable to initialize BluetoothManager
                return false;
            }
        }

        blAdapter = blManager.getAdapter();
        if (blAdapter == null) {
            //Unable to obtain a BluetoothAdapter
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (blAdapter == null || address == null) {
            return false;
        }

        final BluetoothDevice device = blAdapter.getRemoteDevice(address);
        if (device == null) {
            // Device not found.  Unable to connect.
            return false;
        }
        // no autoconnect
        blGatt = device.connectGatt(this, false, gattCallback);
        mConnectionState = STATE_CONNECTING;
        return true;
    }


    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (blGatt == null) {
            return;
        }
        blGatt.close();
        blGatt = null;
    }

    /* -------------------------------- GATT functions -------------------------------------*/
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (blAdapter == null || blGatt == null) {
            return;
        }
        blGatt.readCharacteristic(characteristic);
    }

    public void readCharacteristic(String gattPair) {
        if (blAdapter == null || blGatt == null) {
            return;
        }
        GattPair pair = new GattPair(gattPair);
        BluetoothGattCharacteristic chars = blGatt.getService(pair.getServiceUUID()).getCharacteristic(pair.getCharacteristicUUID());
        readCharacteristic(chars);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (blAdapter == null || blGatt == null) {
            return;
        }
        blGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public void setCharacteristicNotification(String gattPair,
                                              boolean enabled) {
        if (blAdapter == null || blGatt == null) {
            return;
        }
        GattPair pair = new GattPair(gattPair);
        BluetoothGattCharacteristic chars = blGatt.getService(pair.getServiceUUID()).getCharacteristic(pair.getCharacteristicUUID());
        setCharacteristicNotification(chars, enabled);
    }


    public List<BluetoothGattService> getSupportedGattServices() {
        if (blGatt == null) return null;

        return blGatt.getServices();
    }
}
