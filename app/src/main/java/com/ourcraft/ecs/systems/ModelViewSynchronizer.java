package com.ourcraft.ecs.systems;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.ourcraft.ecs.components.ModelComponent;
import com.ourcraft.ecs.components.PositionComponent;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class ModelViewSynchronizer {

    private final Node sceneRoot;
    private final SpatialFactory spatialFactory;
    private final EntitySet entities;
    private final Map<EntityId, TrackedSpatial> trackedSpatials = new HashMap<>();

    private boolean cleanedUp;

    ModelViewSynchronizer(
            EntityData entityData,
            Node sceneRoot,
            SpatialFactory spatialFactory
    ) {
        this.sceneRoot = Objects.requireNonNull(sceneRoot, "sceneRoot");
        this.spatialFactory = Objects.requireNonNull(spatialFactory, "spatialFactory");
        this.entities = Objects.requireNonNull(entityData, "entityData")
                .getEntities(PositionComponent.class, ModelComponent.class);
    }

    void synchronize() {
        if (cleanedUp) {
            return;
        }

        entities.applyChanges();
        Set<EntityId> currentEntityIds = new HashSet<>();
        for (Entity entity : entities) {
            currentEntityIds.add(entity.getId());
            synchronize(entity);
        }

        trackedSpatials.entrySet().removeIf(entry -> {
            if (currentEntityIds.contains(entry.getKey())) {
                return false;
            }
            entry.getValue().spatial().removeFromParent();
            return true;
        });
    }

    void cleanup() {
        if (cleanedUp) {
            return;
        }

        cleanedUp = true;
        entities.release();
        trackedSpatials.values().forEach(tracked -> tracked.spatial().removeFromParent());
        trackedSpatials.clear();
    }

    private void synchronize(Entity entity) {
        ModelComponent model = entity.get(ModelComponent.class);
        PositionComponent position = entity.get(PositionComponent.class);
        TrackedSpatial tracked = trackedSpatials.get(entity.getId());

        if (tracked == null || !tracked.model().equals(model)) {
            if (tracked != null) {
                tracked.spatial().removeFromParent();
            }
            Spatial spatial = Objects.requireNonNull(
                    spatialFactory.create(model),
                    "spatialFactory returned null");
            spatial.setLocalTranslation(position.x(), position.y(), position.z());
            sceneRoot.attachChild(spatial);
            tracked = new TrackedSpatial(model, spatial);
            trackedSpatials.put(entity.getId(), tracked);
        } else {
            tracked.spatial().setLocalTranslation(position.x(), position.y(), position.z());
        }
    }

    @FunctionalInterface
    interface SpatialFactory {
        Spatial create(ModelComponent model);
    }

    private record TrackedSpatial(ModelComponent model, Spatial spatial) {
    }
}
