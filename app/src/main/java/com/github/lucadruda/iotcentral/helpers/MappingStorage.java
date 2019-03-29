package com.github.lucadruda.iotcentral.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class MappingStorage {

    public static final String TEMP_ID = "$tempDeviceId";
    private static final String PREFIX = "IOTC_MEASURE_MAPPING_";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public MappingStorage(Context context, String deviceId) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFIX + deviceId, Context.MODE_PRIVATE);
        this.editor = this.preferences.edit();
    }

    public void add(String uuid, String telemetryField) {
        this.editor.putString(uuid, telemetryField);
        this.editor.apply();
    }

    public void remove(String uuid) {
        this.editor.remove(uuid);
        this.editor.apply();
    }
}
