package com.github.lucadruda.iotcentral.targets.generic;

import com.github.lucadruda.iotcentral.targets.Feature;
import com.github.lucadruda.iotcentral.targets.ST.FeatureTemperature;
import com.github.lucadruda.iotcentral.targets.Service;
import com.github.lucadruda.iotcentral.targets.Target;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
        return null;
    }

    @Override
    protected HashMap<String, Service> setServices() {
        return new HashMap<String, Service>() {{
            put("00001800-0000", new Service("Generic Access", "00001800-0000"));
            put("0000180a-0000", new Service("Device Information Service", "0000180a-0000"));
            put("00001801-0000", new Service("Generic Attribute", "00001801-0000"));
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