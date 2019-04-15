package com.github.lucadruda.iotcentral.helpers;

import android.bluetooth.BluetoothGattCharacteristic;

import java.io.Serializable;
import java.util.UUID;

public class GattPair {

    private String serviceId;
    private String characteristicId;

    public GattPair(String serviceId, String characteristicId) {
        this.serviceId = serviceId;
        this.characteristicId = characteristicId;
    }

    public GattPair(String pairString) {
        String[] pair = pairString.split("/");
        this.serviceId = pair[0];
        this.characteristicId = pair[1];
    }

    public GattPair(BluetoothGattCharacteristic characteristic) {
        this.serviceId = characteristic.getService().getUuid().toString();
        this.characteristicId = characteristic.getUuid().toString();
    }

    public String getServiceId() {
        return serviceId;
    }


    public String getCharacteristicId() {
        return characteristicId;
    }

    public UUID getServiceUUID() {
        return UUID.fromString(serviceId);
    }


    public UUID getCharacteristicUUID() {
        return UUID.fromString(characteristicId);
    }


    public String getKey() {
        return this.serviceId + "/" + this.characteristicId;
    }

}
