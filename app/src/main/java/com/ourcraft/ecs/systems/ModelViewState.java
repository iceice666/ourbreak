package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.ourcraft.ecs.components.ModelComponent;
import com.simsilica.es.EntityData;

public class ModelViewState extends BaseAppState {

    private final EntityData entityData;
    private final Node sceneRoot;

    private AssetManager assetManager;
    private ModelViewSynchronizer synchronizer;

    public ModelViewState(EntityData entityData, Node sceneRoot) {
        this.entityData = entityData;
        this.sceneRoot = sceneRoot;
    }

    @Override
    protected void initialize(Application app) {
        this.assetManager = app.getAssetManager();
        this.synchronizer = new ModelViewSynchronizer(entityData, sceneRoot, this::createSpatial);
    }

    @Override
    protected void cleanup(Application app) {
        if (synchronizer != null) {
            synchronizer.cleanup();
            synchronizer = null;
        }
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        if (synchronizer != null) {
            synchronizer.synchronize();
        }
    }

    private Spatial createSpatial(ModelComponent model) {
        Geometry geom = new Geometry(model.modelId(), new Box(0.5f, 0.5f, 0.5f));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Cyan);
        geom.setMaterial(mat);
        return geom;
    }
}
