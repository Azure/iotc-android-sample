package com.github.lucadruda.iotcentral.targets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Service {

    private String name;
    private String id;

    public Service(String name, String id) {
        this.name = name;
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean lookup(String uuid) {
        String prefix = uuid.substring(0, 13);
        if (getId().equals(prefix)) {
            return true;
        }
        return false;
    }

}
