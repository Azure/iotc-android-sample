package com.github.lucadruda.iotcentral.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class MappingStorage {

    public static final String TEMP_ID = "$tempDeviceId";
    private static final String PREFIX = "IOTC_MEASURE_MAPPING_";
    private SharedPreferences preferences;
    private Context context;
    private HashMap<String, String> cache;
    private String deviceId;

    public MappingStorage(Context context, String deviceId) {
        this.context = context;
        this.deviceId = deviceId;
        this.preferences = context.getSharedPreferences(PREFIX + deviceId, Context.MODE_PRIVATE);
        this.cache = new HashMap<>();
        for (String key : this.preferences.getAll().keySet()) {
            this.cache.put(key, this.preferences.getString(key, null));
        }

    }


    public void add(String uuid, String telemetryField) {
        this.cache.put(uuid, telemetryField);
    }

    public void remove(String uuid) {
        this.cache.remove(uuid);
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean commit() {
        if (this.deviceId.equals(TEMP_ID)) {
            return false;
        }
        if (preferences == null) {
            this.preferences = context.getSharedPreferences(PREFIX + deviceId, Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = this.preferences.edit();
        for (String key : this.cache.keySet()) {
            editor.putString(key, this.cache.get(key));
            editor.apply();
        }
        return editor.commit();
    }

    public String getGattUUID(String iotcTelemetry) {
        for (String key : this.cache.keySet()) {
            if (this.cache.get(key).equals(iotcTelemetry)) {
                return key;
            }
        }
        return null;
    }

    public String getIoTCTelemetry(String gattUUID) {
        return this.cache.get(gattUUID);
    }

    public int size() {
        return this.cache.size();
    }
}
