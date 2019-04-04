package com.github.lucadruda.iotcentral.targets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Feature {

    private String name;
    private String id;

    public Feature(String name, String id) {
        this.name = name;
        this.id = id;
    }


    public float getData(byte[] data) {
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();
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
