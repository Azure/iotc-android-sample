package com.github.lucadruda.iotcentral.targets.generic;

import com.github.lucadruda.iotcentral.targets.Feature;
import com.github.lucadruda.iotcentral.targets.ST.FeatureTemperature;
import com.github.lucadruda.iotcentral.targets.Service;
import com.github.lucadruda.iotcentral.targets.Target;

import java.util.HashMap;

public class GenericTarget extends Target {


    public static final String DEFAULT_FEATURE_NAME = "Unknown feature";
    private Feature feature = new Feature(DEFAULT_FEATURE_NAME, "");

    public GenericTarget(String targetName) {
        super(targetName);
        suffixes = new String[]{
                "1000-8000-00805f9b34fb"
        };
    }

    @Override
    protected HashMap<String, Feature> setFeatures() {
        return new HashMap<String, Feature>() {{
            put("00002A37-0000", new Feature("Heart Rate", "00002A37-0000"));
            put("00002A19-0000", new Feature("Battery Level", "00002A19-0000"));
            put("00002A1C-0000", new FeatureSimTemp("Temperature", "00002A1C-0000"));
        }};
    }

    @Override
    protected HashMap<String, Service> setServices() {
        return new HashMap<String, Service>() {{
            put("00001800-0000", new Service("Generic Access", "00001800-0000"));
            put("0000180a-0000", new Service("Device Information Service", "0000180a-0000"));
            put("00001801-0000", new Service("Generic Attribute", "00001801-0000"));
            put("0000180F-0000", new Service("Generic Battery", "0000180F-0000"));
            put("0000180D-0000", new Service("Generic Hearth Rate", "0000180D-0000"));
            put("00001809-0000", new Service("Simulated Health Termometer", "00001809-0000"));
        }};

    }

    @Override
    protected String[] servicesSuffixes() {
        return suffixes;
    }

    public Feature getGenericFeature() {
        return feature;
    }

    public Service getGenericService() {
        return this.getServices().get("00001800-0000");
    }

}