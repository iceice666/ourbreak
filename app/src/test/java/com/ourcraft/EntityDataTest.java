package com.ourcraft;

import com.ourcraft.ecs.components.ModelComponent;
import com.ourcraft.ecs.components.PositionComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.base.DefaultEntityData;
import org.junit.jupiter.api.Test;

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
}
