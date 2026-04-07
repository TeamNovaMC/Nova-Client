package org.cloudburstmc.protocol.bedrock.data.attributelayer;

import lombok.Value;

@Value
public class AttributeLayerSettings {

    int priority;
    Weight weight;
    boolean enabled;
    boolean transitionsPaused;

    public interface Weight {
    }

    @Value
    public static class FloatWeight implements Weight {
        float value;
    }

    @Value
    public static class StringWeight implements Weight {
        String value;
    }
}
