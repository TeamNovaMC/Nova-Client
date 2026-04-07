package org.cloudburstmc.protocol.bedrock.data;

import lombok.Data;
import lombok.Value;
import org.cloudburstmc.math.vector.Vector3f;

import java.awt.Color;

@Data
public class LocatorBarWaypoint {

    private int updateFlag;
    private Boolean visible;
    private WorldPosition worldPosition;
    private Integer textureId;
    private Color color;
    private Boolean clientPositionAuthority;
    private Long entityUniqueId;

    @Value
    public static class WorldPosition {
        Vector3f position;
        int dimension;
    }
}
