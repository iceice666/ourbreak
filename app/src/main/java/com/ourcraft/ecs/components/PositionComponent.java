package com.ourcraft.ecs.components;

import com.jme3.math.Vector3f;
import com.simsilica.es.EntityComponent;

public record PositionComponent(float x, float y, float z) implements EntityComponent {

    public PositionComponent {
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) {
            throw new IllegalArgumentException("position coordinates must be finite");
        }
    }

    public static PositionComponent of(Vector3f v) {
        return new PositionComponent(v.x, v.y, v.z);
    }

    public Vector3f toVector3f() {
        return new Vector3f(x, y, z);
    }
}
