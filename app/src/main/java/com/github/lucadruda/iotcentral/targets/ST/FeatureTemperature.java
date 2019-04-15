package com.github.lucadruda.iotcentral.targets.ST;

import com.github.lucadruda.iotcentral.targets.Feature;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class FeatureTemperature extends Feature {
    public FeatureTemperature(String name, String id) {
        super(name, id);
    }

    @Override
    public float getData(byte[] data) {
        short buff = ByteBuffer.wrap(data, 2, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
        return buff / 10.0f;
    }
}
