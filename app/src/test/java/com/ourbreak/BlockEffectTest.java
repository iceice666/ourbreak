package com.ourbreak;

import com.ourbreak.ecs.components.BlockComponent;
import com.ourbreak.ecs.components.BlockComponent.BlockType;
import com.ourbreak.ecs.components.EffectComponent;
import com.ourbreak.ecs.components.PhaseComponent;
import com.ourbreak.ecs.components.PositionComponent;
import com.ourbreak.ecs.systems.BlockEffectSystem;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ourbreak.ecs.components.BlockComponent.BlockType.CORAL;
import static com.ourbreak.ecs.components.BlockComponent.BlockType.JELLYFISH;
import static com.ourbreak.ecs.components.BlockComponent.BlockType.SAND;
import static com.ourbreak.ecs.components.PhaseComponent.Phase.ATTACK;
import static com.ourbreak.ecs.components.PhaseComponent.Phase.BUILD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockEffectTest {

    private EntityData ed;
    private EntityId gameStateId;
    private BlockEffectSystem system;

    @BeforeEach
    void setUp() {
        ed = new DefaultEntityData();

        gameStateId = ed.createEntity();
        ed.setComponent(gameStateId, new PhaseComponent(ATTACK));

        // Construct before any blocks so placements register as additions.
        system = new BlockEffectSystem(ed, gameStateId);
    }

    @AfterEach
    void tearDown() {
        system.close();
    }

    // ── Coral proximity ──────────────────────────────────────────────────────

    @Test
    void coralWithinRangeSlowsPlayer() {
        createBlock(CORAL, 1f, 0f);
        float factor = system.coralSlowFactor(new PositionComponent(0f, 0f, 0f));
        assertTrue(factor < 1.0f, "expected a slow factor below 1.0");
    }

    @Test
    void coralOutOfRangeDoesNotSlow() {
        createBlock(CORAL, 10f, 0f);
        assertEquals(1.0f, system.coralSlowFactor(new PositionComponent(0f, 0f, 0f)));
    }

    @Test
    void coralDoesNotSlowOutsideAttackPhase() {
        ed.setComponent(gameStateId, new PhaseComponent(BUILD));
        createBlock(CORAL, 1f, 0f);
        assertEquals(1.0f, system.coralSlowFactor(new PositionComponent(0f, 0f, 0f)));
    }

    // ── Jellyfish flicker ────────────────────────────────────────────────────

    @Test
    void placingJellyfishEmitsFlickerTrigger() {
        createBlock(JELLYFISH, 1f, 0f);
        system.update(0f);
        assertEquals(1, system.flickerTriggerCount());
    }

    @Test
    void placingNonJellyfishEmitsNoFlicker() {
        createBlock(SAND, 1f, 0f);
        createBlock(CORAL, 2f, 0f);
        system.update(0f);
        assertEquals(0, system.flickerTriggerCount());
    }

    // ── Drone 3x3 expansion ──────────────────────────────────────────────────

    @Test
    void droneExpandsToFullNeighborhood() {
        EntityId center = null;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                EntityId id = createBlock(SAND, dx, dz);
                if (dx == 0 && dz == 0) {
                    center = id;
                }
            }
        }
        assertEquals(9, system.droneAreaTargets(center).size());
    }

    @Test
    void droneExpandsToOccupiedNeighborsOnly() {
        EntityId center = createBlock(SAND, 0f, 0f);
        createBlock(SAND, 1f, 0f);
        createBlock(SAND, 0f, 1f);
        List<EntityId> targets = system.droneAreaTargets(center);
        assertEquals(3, targets.size());
        assertTrue(targets.contains(center));
    }

    @Test
    void droneIsolatedBlockReturnsOnlyCenter() {
        EntityId center = createBlock(SAND, 0f, 0f);
        assertEquals(List.of(center), system.droneAreaTargets(center));
    }

    @Test
    void droneRadiusExpandsToTheFullSquare() {
        EntityId center = null;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                EntityId id = createBlock(SAND, dx, dz);
                if (dx == 0 && dz == 0) {
                    center = id;
                }
            }
        }
        // radius 2 = a 5×5 square = 25 cells (Lv2 drone); radius 1 still only the inner 3×3 = 9.
        assertEquals(25, system.droneAreaTargets(center, 2).size());
        assertEquals(9, system.droneAreaTargets(center, 1).size());
    }

    // ── Sword row expansion ──────────────────────────────────────────────────

    @Test
    void swordRowExpandsToFullRow() {
        EntityId center = createBlock(SAND, 0f, 0f);
        createBlock(SAND, 1f, 0f);
        createBlock(SAND, -1f, 0f);
        assertEquals(3, system.rowTargets(center, true).size());
    }

    @Test
    void swordRowExpandsToOccupiedSideOnly() {
        EntityId center = createBlock(SAND, 0f, 0f);
        createBlock(SAND, 1f, 0f);
        List<EntityId> targets = system.rowTargets(center, true);
        assertEquals(2, targets.size());
        assertTrue(targets.contains(center));
    }

    @Test
    void swordRowIsolatedReturnsOnlyCenter() {
        EntityId center = createBlock(SAND, 0f, 0f);
        assertEquals(List.of(center), system.rowTargets(center, true));
    }

    @Test
    void swordRowAlongZAxis() {
        EntityId center = createBlock(SAND, 0f, 0f);
        createBlock(SAND, 0f, 1f);
        createBlock(SAND, 0f, -1f);
        assertEquals(3, system.rowTargets(center, false).size());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private EntityId createBlock(BlockType type, float x, float z) {
        EntityId id = ed.createEntity();
        ed.setComponents(id, new BlockComponent(type), new PositionComponent(x, 0f, z));
        EffectComponent.forBlockType(type).ifPresent(effect -> ed.setComponent(id, effect));
        return id;
    }
}
