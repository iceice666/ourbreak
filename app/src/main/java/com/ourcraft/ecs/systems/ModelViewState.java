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
import com.ourcraft.ecs.components.PositionComponent;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.HashMap;
import java.util.Map;

public class ModelViewState extends BaseAppState {

    private final EntityData entityData;
    private final Node sceneRoot;

    private AssetManager assetManager;
    private EntitySet entities;
    private final Map<EntityId, Spatial> spatials = new HashMap<>();

    public ModelViewState(EntityData entityData, Node sceneRoot) {
        this.entityData = entityData;
        this.sceneRoot = sceneRoot;
    }

    @Override
    protected void initialize(Application app) {
        this.assetManager = app.getAssetManager();
        this.entities = entityData.getEntities(PositionComponent.class, ModelComponent.class);
    }

    @Override
    protected void cleanup(Application app) {
        entities.release();
        spatials.values().forEach(Spatial::removeFromParent);
        spatials.clear();
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        if (entities.applyChanges()) {
            for (Entity e : entities.getAddedEntities()) addSpatial(e);
            for (Entity e : entities.getChangedEntities()) updateSpatial(e);
            for (Entity e : entities.getRemovedEntities()) removeSpatial(e);
        }
    }

    private void addSpatial(Entity e) {
        Spatial sp = createSpatial(e.get(ModelComponent.class));
        spatials.put(e.getId(), sp);
        sceneRoot.attachChild(sp);
        updateSpatial(e);
    }

    private void updateSpatial(Entity e) {
        Spatial sp = spatials.get(e.getId());
        if (sp == null) return;
        PositionComponent pos = e.get(PositionComponent.class);
        sp.setLocalTranslation(pos.x(), pos.y(), pos.z());
    }

    private void removeSpatial(Entity e) {
        Spatial sp = spatials.remove(e.getId());
        if (sp != null) sp.removeFromParent();
    }

    private Spatial createSpatial(ModelComponent model) {
        Geometry geom = new Geometry(model.modelId(), new Box(0.5f, 0.5f, 0.5f));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Cyan);
        geom.setMaterial(mat);
        return geom;
    }
}
