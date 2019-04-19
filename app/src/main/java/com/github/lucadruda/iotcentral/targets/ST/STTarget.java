package com.github.lucadruda.iotcentral.targets.ST;

import com.github.lucadruda.iotcentral.targets.Feature;
import com.github.lucadruda.iotcentral.targets.Service;
import com.github.lucadruda.iotcentral.targets.Target;

import java.util.HashMap;

public class STTarget extends Target {


    public STTarget(String targetName) {
        super(targetName);
        suffixes = new String[]{
                "11e1-9ab4-0002a5d5c51b", "11e1-ac36-0002a5d5c51b"
        };
    }

    @Override
    protected HashMap<String, Feature> setFeatures() {
        return new HashMap<String, Feature>() {{
            put("00010000-0001", new FeatureTemperature("Temperature", "00010000-0001"));
            put("00000040-0001", new FeatureCompass("Compass", "00000040-0001"));
        }};

    }

    @Override
    protected HashMap<String, Service> setServices() {
        return new HashMap<String, Service>() {{
            put("00000000-0001", new Service("ST Standard Service", "00000000-0001"));
            put("00000000-0002", new Service("ST Basic Service", "00000000-0002"));
            put("00000000-0003", new Service("ST Extended Service", "00000000-0003"));
            put("00000000-000e", new Service("ST Debug Service", "00000000-000e"));
            put("00000000-000f", new Service("ST Configuration Service", "00000000-000f"));
        }};

    }

    @Override
    protected String[] servicesSuffixes() {
        return suffixes;
    }

}