package com.ourcraft.ecs.systems;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.ourcraft.ecs.components.ModelComponent;
import com.ourcraft.ecs.components.PositionComponent;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ModelViewSynchronizerTest {

    private EntityData entityData;
    private Node sceneRoot;
    private List<Spatial> createdSpatials;
    private ModelViewSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        entityData = new DefaultEntityData();
        sceneRoot = new Node("scene-root");
        createdSpatials = new ArrayList<>();
        synchronizer = new ModelViewSynchronizer(entityData, sceneRoot, model -> {
            Spatial spatial = new Node(model.modelId());
            createdSpatials.add(spatial);
            return spatial;
        });
    }

    @AfterEach
    void tearDown() {
        synchronizer.cleanup();
        entityData.close();
    }

    @Test
    void incompleteEntityIsIgnoredUntilItBecomesRenderable() {
        EntityId entityId = entityData.createEntity();
        entityData.setComponent(entityId, new PositionComponent(1.0f, 2.0f, 3.0f));

        synchronizer.synchronize();

        assertEquals(0, sceneRoot.getQuantity());

        entityData.setComponent(entityId, new ModelComponent("cube"));
        synchronizer.synchronize();

        assertEquals(1, sceneRoot.getQuantity());
    }

    @Test
    void renderableEntityCreatesOneSpatialAtItsPosition() {
        createRenderableEntity("cube", new PositionComponent(1.0f, 2.0f, 3.0f));

        synchronizer.synchronize();
        synchronizer.synchronize();

        assertEquals(1, sceneRoot.getQuantity());
        assertEquals(1, createdSpatials.size());
        assertEquals("cube", sceneRoot.getChild(0).getName());
        assertEquals(new Vector3f(1.0f, 2.0f, 3.0f), sceneRoot.getChild(0).getLocalTranslation());
    }

    @Test
    void positionUpdateMovesExistingSpatial() {
        EntityId entityId = createRenderableEntity(
                "cube",
                new PositionComponent(1.0f, 2.0f, 3.0f));
        synchronizer.synchronize();
        Spatial original = sceneRoot.getChild(0);

        entityData.setComponent(entityId, new PositionComponent(4.0f, 5.0f, 6.0f));
        synchronizer.synchronize();

        assertSame(original, sceneRoot.getChild(0));
        assertEquals(new Vector3f(4.0f, 5.0f, 6.0f), original.getLocalTranslation());
        assertEquals(1, createdSpatials.size());
    }

    @Test
    void modelUpdateReplacesSpatialAtCurrentPosition() {
        EntityId entityId = createRenderableEntity(
                "cube",
                new PositionComponent(1.0f, 2.0f, 3.0f));
        synchronizer.synchronize();
        Spatial original = sceneRoot.getChild(0);

        entityData.setComponents(entityId,
                new ModelComponent("rock"),
                new PositionComponent(4.0f, 5.0f, 6.0f));
        synchronizer.synchronize();

        Spatial replacement = sceneRoot.getChild(0);
        assertNull(original.getParent());
        assertEquals("rock", replacement.getName());
        assertEquals(new Vector3f(4.0f, 5.0f, 6.0f), replacement.getLocalTranslation());
        assertEquals(2, createdSpatials.size());
    }

    @Test
    void entityRemovalDetachesSpatial() {
        EntityId entityId = createRenderableEntity(
                "cube",
                new PositionComponent(1.0f, 2.0f, 3.0f));
        synchronizer.synchronize();
        Spatial spatial = sceneRoot.getChild(0);

        entityData.removeEntity(entityId);
        synchronizer.synchronize();

        assertEquals(0, sceneRoot.getQuantity());
        assertNull(spatial.getParent());
    }

    @ParameterizedTest
    @MethodSource("requiredComponentTypes")
    void requiredComponentRemovalDetachesSpatial(
            Class<? extends EntityComponent> componentType
    ) {
        EntityId entityId = createRenderableEntity(
                "cube",
                new PositionComponent(1.0f, 2.0f, 3.0f));
        synchronizer.synchronize();
        Spatial spatial = sceneRoot.getChild(0);

        entityData.removeComponent(entityId, componentType);
        synchronizer.synchronize();

        assertEquals(0, sceneRoot.getQuantity());
        assertNull(spatial.getParent());
    }

    @Test
    void cleanupDetachesAllSpatialsAndIsIdempotent() {
        createRenderableEntity("cube", new PositionComponent(1.0f, 2.0f, 3.0f));
        createRenderableEntity("rock", new PositionComponent(4.0f, 5.0f, 6.0f));
        synchronizer.synchronize();
        List<Spatial> attachedSpatials = List.copyOf(createdSpatials);

        synchronizer.cleanup();
        synchronizer.cleanup();
        createRenderableEntity("ignored", new PositionComponent(7.0f, 8.0f, 9.0f));
        synchronizer.synchronize();

        assertEquals(0, sceneRoot.getQuantity());
        assertEquals(2, createdSpatials.size());
        attachedSpatials.forEach(spatial -> assertNull(spatial.getParent()));
    }

    private static Stream<Class<? extends EntityComponent>> requiredComponentTypes() {
        return Stream.of(PositionComponent.class, ModelComponent.class);
    }

    private EntityId createRenderableEntity(String modelId, PositionComponent position) {
        EntityId entityId = entityData.createEntity();
        entityData.setComponents(entityId, new ModelComponent(modelId), position);
        return entityId;
    }
}
