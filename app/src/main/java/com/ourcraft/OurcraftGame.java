package com.ourcraft;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.ourcraft.ecs.components.ModelComponent;
import com.ourcraft.ecs.components.PositionComponent;
import com.ourcraft.ecs.systems.ModelViewState;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;

public class OurcraftGame extends SimpleApplication {

    private EntityData entityData;

    public static void main(String[] args) {
        new OurcraftGame().start();
    }

    @Override
    public void simpleInitApp() {
        entityData = new DefaultEntityData();
        stateManager.attach(new ModelViewState(entityData, rootNode));

        viewPort.setBackgroundColor(new ColorRGBA(0.1f, 0.12f, 0.15f, 1f));

        DirectionalLight sun = new DirectionalLight(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
        rootNode.addLight(sun);
        rootNode.addLight(new AmbientLight(ColorRGBA.White.mult(0.3f)));

        spawnCube(0f, 0f, 0f);
        spawnCube(2f, 0f, 0f);

        cam.setLocation(new Vector3f(5f, 3f, 8f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    @Override
    public void destroy() {
        if (entityData != null) entityData.close();
        super.destroy();
    }

    private EntityId spawnCube(float x, float y, float z) {
        EntityId id = entityData.createEntity();
        entityData.setComponents(id,
                new PositionComponent(x, y, z),
                new ModelComponent("cube"));
        return id;
    }

    public EntityData getEntityData() {
        return entityData;
    }
}
