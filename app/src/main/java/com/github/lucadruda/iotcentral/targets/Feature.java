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
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        if (data.length == 1) {
            return (float) (buffer.get(0) & 0xFF);
        }
        return buffer.getShort();
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
