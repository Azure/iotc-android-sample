package com.github.lucadruda.iotcentral;

import com.github.lucadruda.iotcentral.service.ARMClient;
import com.github.lucadruda.iotcentral.service.DataClient;
import com.github.lucadruda.iotcentral.service.templates.DevKitTemplate;
import com.github.lucadruda.iotcentral.service.types.Measure;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class IoTCentral {
    public static final IoTCentral instance = new IoTCentral();

    public static IoTCentral getInstance() {
        return instance;
    }

    private static DataClient dataClient;
    private static ARMClient armClient;


    public static DataClient getDataClient() {
        //TODO: check if null and handle token refresh
        return dataClient;
    }

    public static ARMClient getArmClient() {
        //TODO: check if null and handle token refresh
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

    public static List<Measure> getMeasures(String templateId) {
        return new DevKitTemplate().measures(templateId);
    }

}
