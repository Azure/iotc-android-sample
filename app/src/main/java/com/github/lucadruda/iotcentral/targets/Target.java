package com.github.lucadruda.iotcentral.targets;

import java.util.HashMap;

public abstract class Target {
    private String targetName;
    private HashMap<String, Feature> features;
    private HashMap<String, Service> services;
    protected String[] suffixes;

    protected Target(String targetName) {
        this.targetName = targetName;
        this.features = setFeatures();
        this.services = setServices();
    }

    protected abstract HashMap<String, Feature> setFeatures();

    protected abstract HashMap<String, Service> setServices();

    protected HashMap<String, Feature> getFeatures() {
        return features;
    }

    protected HashMap<String, Service> getServices() {
        return services;
    }

    protected abstract String[] servicesSuffixes();

    public boolean lookup(String uuid) {
        String suffix = uuid.substring(14);
        for (String serviceSuffix : servicesSuffixes()) {
            if (serviceSuffix.equals(suffix)) {
                return true;
            }
        }
        return false;
    }


}
