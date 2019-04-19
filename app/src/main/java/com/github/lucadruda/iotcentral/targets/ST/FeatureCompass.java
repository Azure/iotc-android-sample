package com.github.lucadruda.iotcentral.targets.ST;

import com.github.lucadruda.iotcentral.targets.Feature;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class FeatureCompass extends Feature {
    public FeatureCompass(String name, String id) {
        super(name, id);
    }

    @Override
    public float getData(byte[] data) {
        int buff = ByteBuffer.wrap(data, 2, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
        return buff / 100.0f;
    }
}
