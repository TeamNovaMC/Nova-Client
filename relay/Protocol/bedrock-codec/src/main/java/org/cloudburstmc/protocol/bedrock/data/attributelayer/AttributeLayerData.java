package org.cloudburstmc.protocol.bedrock.data.attributelayer;

import lombok.Value;

import java.util.List;

@Value
public class AttributeLayerData {

    String layerName;
    int dimension;
    AttributeLayerSettings settings;
    List<EnvironmentAttributeData> attributes;
}
