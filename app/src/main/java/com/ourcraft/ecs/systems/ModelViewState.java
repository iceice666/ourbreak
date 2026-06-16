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
        // Placeholder art: colour each block by type so the counter-matrix is readable (real models = M8).
        ColorRGBA color = colorFor(model.modelId());
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color);
        geom.setMaterial(mat);
        return geom;
    }

    private static ColorRGBA colorFor(String modelId) {
        return switch (modelId) {
            case "sand-block" -> new ColorRGBA(0.85f, 0.78f, 0.45f, 1f);      // sandy tan
            case "coral-block" -> new ColorRGBA(0.95f, 0.42f, 0.6f, 1f);      // coral pink
            case "shell-block" -> new ColorRGBA(0.95f, 0.92f, 0.85f, 1f);     // shell cream
            case "rock-block" -> new ColorRGBA(0.38f, 0.4f, 0.43f, 1f);       // slate grey
            case "jellyfish-block" -> new ColorRGBA(0.45f, 0.5f, 0.95f, 1f);  // jelly blue
            default -> ColorRGBA.Cyan;
        };
    }
}
