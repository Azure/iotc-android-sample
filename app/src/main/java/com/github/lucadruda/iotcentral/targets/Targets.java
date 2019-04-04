package com.github.lucadruda.iotcentral.targets;

import com.github.lucadruda.iotcentral.targets.ST.STTarget;
import com.github.lucadruda.iotcentral.targets.generic.GenericTarget;

import java.util.HashMap;

public class Targets {


    public static HashMap<String, Target> targets = new HashMap<String, Target>() {
        {
            put("STMicroelecronics", new STTarget("STMicroelecronics"));
            put("Generic", new GenericTarget("Generic"));
        }
    };

    public static Service servicelookup(String uuid) {
        for (String tKey : targets.keySet()) {
            Target curTarget = targets.get(tKey);
            if (curTarget.lookup(uuid)) {
                for (String sKey : curTarget.getServices().keySet()) {
                    Service curService = curTarget.getServices().get(sKey);
                    if (curService.lookup(uuid)) {
                        return curService;
                    }
                }
            }
        }
        return ((GenericTarget) targets.get("Generic")).getGenericService();

    }


    public static Feature featureslookup(String uuid) {
        for (String tKey : targets.keySet()) {
            Target curTarget = targets.get(tKey);
            if (curTarget.lookup(uuid)) {
                for (String fKey : curTarget.getFeatures().keySet()) {
                    Feature curFeature = curTarget.getFeatures().get(fKey);
                    if (curFeature.lookup(uuid)) {
                        return curFeature;
                    }
                }
            }
        }
        return ((GenericTarget) targets.get("Generic")).getGenericFeature();
    }
}
