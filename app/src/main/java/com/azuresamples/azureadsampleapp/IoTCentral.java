package com.azuresamples.azureadsampleapp;

import com.github.lucadruda.iotcentral.service.ARMClient;
import com.github.lucadruda.iotcentral.service.DataClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class IoTCentral {
    public static final IoTCentral instance = new IoTCentral();

    public static IoTCentral getInstance() {
        return instance;
    }

    private static DataClient dataClient;
    private static ARMClient armClient;


    public static DataClient getDataClient() {
        return dataClient;
    }

    public static ARMClient getArmClient() {
        return armClient;
    }

    public static DataClient createDataClient(String accessToken) throws InterruptedException, ExecutionException, URISyntaxException, IOException {
        dataClient = new DataClient(accessToken);
        return dataClient;
    }

    public static ARMClient createARMClient(String accessToken) throws InterruptedException, ExecutionException, URISyntaxException, IOException {
        armClient = new ARMClient(accessToken);
        return armClient;
    }
}
