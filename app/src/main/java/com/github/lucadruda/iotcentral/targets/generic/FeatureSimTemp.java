package com.github.lucadruda.iotcentral.targets.generic;

import com.github.lucadruda.iotcentral.targets.Feature;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class FeatureSimTemp extends Feature {
    public FeatureSimTemp(String name, String id) {
        super(name, id);
    }

    @Override
    public float getData(byte[] data) {
        short buff = ByteBuffer.wrap(data, 2, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
        return buff / 10.0f;
    }
}
