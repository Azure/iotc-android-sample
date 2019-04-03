

package com.github.lucadruda.iotcentral.bluetooth;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute");
        attributes.put("00000000-0001-11e1-9ab4-0002a5d5c51b", "ST Standard Service");
        attributes.put("00000000-0003-11e1-9ab4-0002a5d5c51b", "ST Extended Service");
        attributes.put("00000000-000e-11e1-9ab4-0002a5d5c51b", "ST Debug Service");
        attributes.put("00000000-000f-11e1-9ab4-0002a5d5c51b", "ST Configuration Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        attributes.put("00010000-0001-11e1-ac36-0002a5d5c51b", "Temperature");
        attributes.put("00020000-0001-11e1-ac36-0002a5d5c51b", "Battery");
        attributes.put("00080000-0001-11e1-ac36-0002a5d5c51b", "Humidity");
        attributes.put("00800000-0001-11e1-ac36-0002a5d5c51b", "Acceleration");
        attributes.put("00400000-0001-11e1-ac36-0002a5d5c51b", "Gyroscope");
        attributes.put("00100000-0001-11e1-ac36-0002a5d5c51b", "Pressure");
        attributes.put("04000000-0001-11e1-ac36-0002a5d5c51b", "MicLevel");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
