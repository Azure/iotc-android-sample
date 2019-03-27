package com.github.lucadruda.iotcentral.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;

public class BluetoothManager {

    //public static listDevices
    private static BluetoothAdapter adapter;

    private static BluetoothAdapter getInstance() {
        if (adapter == null) {
            adapter = BluetoothAdapter.getDefaultAdapter();
        }
        return adapter;
    }

    public Set<BluetoothDevice> listPaired() {
        return getInstance().getBondedDevices();
    }
}
