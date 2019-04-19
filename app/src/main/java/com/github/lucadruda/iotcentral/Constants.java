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


    public static final String MAPPING_STORAGE = "MAPPING_STORAGE";

    /*---------------------- INTENTS APPLICATION EXTRAS --------------------------------------*/
    public static final String APPLICATION = "APPLICATION";
    public static final String DEVICE_TEMPLATE_ID = "TEMPLATE_ID";
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String DEVICE_EXISTS = "DEVICE_EXISTS";

    /*------------------------- IOTCENTRAL EXTRAS -----------------------------------*/
    public static final String IOTCENTRAL_CONNECTION = "IOTCENTRAL_CONNECTION";
    public static final String IOTCENTRAL_DEVICE_CONNECTION_STATUS = "IOTCENTRAL_DEVICE_CONNECTION_STATUS";
    public static final String IOTCENTRAL_DEVICE_CONNECTION_CHANGE = "IOTCENTRAL_DEVICE_CONNECTION_CHANGE";
    public static final String IOTCENTRAL_COMMAND_RECEIVED = "IOTCENTRAL_COMMAND_RECEIVED";
    public static final String IOTCENTRAL_COMMAND_TEXT = "IOTCENTRAL_COMMAND_TEXT";
}
