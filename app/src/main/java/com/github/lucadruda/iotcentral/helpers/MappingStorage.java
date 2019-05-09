package com.github.lucadruda.iotcentral.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import com.github.lucadruda.iotcentral.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MappingStorage {

    public static final String MAPPING_VERSION = "$version";
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
        this.cache.put(MAPPING_VERSION, "1");
        for (String key : this.preferences.getAll().keySet()) {
            this.cache.put(key, this.preferences.getString(key, null));
        }

    }


    public static MappingStorage getFromCommandPayload(Context context, String deviceId, String mapping) {
        JsonParser parser = new JsonParser();
        JsonObject payload = ((JsonObject) parser.parse(mapping));
        String mapJson = payload.get(Constants.MAP_COMMAND_FIELD).getAsString();
        JsonObject mapObj = ((JsonObject) parser.parse(mapJson));
        MappingStorage storage = new MappingStorage(context, deviceId);
        storage.cache.clear();
        for (Map.Entry<String, JsonElement> entry : mapObj.entrySet()) {
            storage.add(entry.getKey(), entry.getValue().getAsString());
        }
        storage.add(MAPPING_VERSION, payload.get(Constants.MAP_COMMAND_VERSION).getAsString());
        return storage;
    }


    public void add(String gattPair, String telemetryField) {
        this.cache.put(gattPair, telemetryField);
    }

    public void remove(String uuid) {
        this.cache.remove(uuid);
    }


    public int getMappingVersion() {
        return Integer.parseInt(this.cache.get(MAPPING_VERSION));
    }

    public void updateVersion() {
        this.cache.put(MAPPING_VERSION, "" + (getMappingVersion() + 1));
    }

    public boolean commit() {
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
            if (this.cache.get(key).equalsIgnoreCase(iotcTelemetry)) {
                return key;
            }
        }
        return null;
    }

    public HashMap<String, String> getAll() {
        return cache;
    }

    public String getJsonString() {
        JsonObject obj = new JsonObject();
        JsonObject mapObj = new JsonObject();
        for (String key : this.cache.keySet()) {
            if (!key.equals(MAPPING_VERSION)) {
                mapObj.addProperty(key, this.cache.get(key));
            }
        }
        obj.addProperty(Constants.MAP_PROPERTY_NAME, mapObj.toString());
        return obj.toString();
    }

    public String getIoTCTelemetry(String gattUUID) {
        return this.cache.get(gattUUID);
    }

    public int size() {
        return this.cache.size();
    }
}
