package com.github.lucadruda.iotcentral;

public class Constants {

    /*---------------------- AUTHORIZATION --------------------------------------------------*/
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String USER_EMAIL = "user_email";


    public static final String CLIENT_ID = "04b07795-8ddb-461a-bbee-02f9e1bf7b46";
    public static final String IOTC_TOKEN_AUDIENCE = "https://apps.azureiotcentral.com/";
    public static final String RM_TOKEN_AUDIENCE = "https://management.azure.com/";
    public static final String GRAPH_TOKEN_AUDIENCE = "https://graph.windows.net/";
    public static final String IOTC_DATA_URL = "https://api.azureiotcentral.com/v1-beta";
    public static final String AUTHORITY_BASE = "https://login.microsoftonline.com/";
    public static final String IOTC_TEMPLATE_ID = "iotc-devkit-sample:1.0.0";

    /*------------------------------- MAPPING CONSTANTS ---------------------*/
    public static final String MAPPING_STORAGE = "MAPPING_STORAGE";
    public static final String MAP_PROPERTY_NAME = "ble_mapping";
    public static final String MAP_PROPERTY_VERSION = "ble_version";
    public static final String MAP_COMMAND = "updMapping";
    public static final String MAP_COMMAND_FIELD = "mapping";
    public static final String MAP_COMMAND_VERSION = "version";
    public static final String MAPPING_PAYLOAD = "MAP_SETTING_PAYLOAD";
    public final static String TELEMETRY_ASSIGNED = "TELEMETRY_ASSIGNED";
    public final static String TELEMETRY_REFRESHED = "TELEMETRY_REFRESHED";
    public final static String MEASURE_MAPPING_GATT_PAIR = "MEASURE_MAPPING_GATT_PAIR";
    public final static String MEASURE_MAPPING_IOTC = "MEASURE_MAPPING_IOTC";

    /*------------------------------ BLE CONSTANTS -------------------------------*/
    public static final String BLE_SERVICES_MAP = "BLE_SERVICES_MAP";


    /*---------------------- INTENTS APPLICATION EXTRAS --------------------------------------*/
    public static final String APPLICATION = "APPLICATION";
    public static final String DEVICE = "DEVICE";
    public static final String DEVICE_TEMPLATE_ID = "TEMPLATE_ID";
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ID = "DEVICE_ID";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String DEVICE_EXISTS = "DEVICE_EXISTS";
    public static final String MEASURES = "MEASURES";

    /*------------------------- IOTCENTRAL EXTRAS -----------------------------------*/
    public static final String IOTCENTRAL_CONNECTION = "IOTCENTRAL_CONNECTION";
    public static final String IOTCENTRAL_DEVICE_CONNECTION_STATUS = "IOTCENTRAL_DEVICE_CONNECTION_STATUS";
    public static final String IOTCENTRAL_DEVICE_CONNECTION_CHANGE = "IOTCENTRAL_DEVICE_CONNECTION_CHANGE";
    public static final String IOTCENTRAL_MAPPING_CHANGE = "IOTCENTRAL_MAPPING_CHANGE";
    public static final String IOTCENTRAL_COMMAND_RECEIVED = "IOTCENTRAL_COMMAND_RECEIVED";
    public static final String IOTCENTRAL_COMMAND_TEXT = "IOTCENTRAL_COMMAND_TEXT";

    public static final int INPUTDIALOG_ID = -1100001;
}
