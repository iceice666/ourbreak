package com.ourcraft;

import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.ModelComponent;
import com.ourcraft.ecs.components.PositionComponent;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.Filters;
import com.simsilica.es.base.DefaultEntityData;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static com.ourcraft.ecs.components.BlockComponent.BlockType.ROCK;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.SAND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityDataTest {

    @Test
    void componentsRoundTripThroughEntityData() {
        EntityData data = new DefaultEntityData();
        EntityId id = data.createEntity();
        data.setComponents(id,
                new PositionComponent(1f, 2f, 3f),
                new ModelComponent("cube"));

        PositionComponent pos = data.getComponent(id, PositionComponent.class);
        ModelComponent model = data.getComponent(id, ModelComponent.class);

        assertNotNull(pos);
        assertEquals(2f, pos.y());
        assertEquals("cube", model.modelId());
    }

    @Test
    void entitySetReportsNewlyAddedEntities() {
        EntityData data = new DefaultEntityData();
        EntitySet set = data.getEntities(PositionComponent.class, ModelComponent.class);

        EntityId id = data.createEntity();
        data.setComponents(id,
                new PositionComponent(0f, 0f, 0f),
                new ModelComponent("cube"));

        assertTrue(set.applyChanges());
        assertEquals(1, set.getAddedEntities().size());
        assertEquals(id, set.getAddedEntities().iterator().next().getId());
    }

    @Test
    void filteredEntitySetTracksMatchingComponentChanges() {
        EntityData data = new DefaultEntityData();
        EntitySet rocks = data.getEntities(
                Filters.fieldEquals(BlockComponent.class, "blockType", ROCK),
                BlockComponent.class);
        try {
            EntityId sandId = data.createEntity();
            data.setComponent(sandId, new BlockComponent(SAND));
            EntityId rockId = data.createEntity();
            data.setComponent(rockId, new BlockComponent(ROCK));

            assertTrue(rocks.applyChanges());
            assertEquals(Set.of(rockId), entityIds(rocks));
            assertEquals(ROCK, data.getComponent(rockId, BlockComponent.class).blockType());

            data.setComponent(sandId, new BlockComponent(ROCK));
            assertTrue(rocks.applyChanges());
            assertEquals(Set.of(sandId, rockId), entityIds(rocks));

            data.setComponent(rockId, new BlockComponent(SAND));
            assertTrue(rocks.applyChanges());
            assertEquals(Set.of(sandId), entityIds(rocks));
        } finally {
            rocks.release();
            data.close();
        }
    }

    private static Set<EntityId> entityIds(EntitySet entities) {
        return entities.stream().map(Entity::getId).collect(Collectors.toSet());
    }
}
