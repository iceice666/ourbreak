package com.ourcraft;

import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.BlockComponent.BlockType;
import com.ourcraft.ecs.components.EffectComponent;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PlayerHealthComponent;
import com.ourcraft.ecs.components.PositionComponent;
import com.ourcraft.ecs.systems.BlockEffectSystem;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ourcraft.ecs.components.BlockComponent.BlockType.CORAL;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.JELLYFISH;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.SAND;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.SHELL;
import static com.ourcraft.ecs.components.PhaseComponent.Phase.ATTACK;
import static com.ourcraft.ecs.components.PhaseComponent.Phase.BUILD;
import static com.ourcraft.ecs.systems.BlockEffectSystem.SHELL_REFLECT_DAMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockEffectTest {

    private static final float PLAYER_MAX_HEALTH = 10f;

    private EntityData ed;
    private EntityId gameStateId;
    private EntityId playerId;
    private BlockEffectSystem system;

    @BeforeEach
    void setUp() {
        ed = new DefaultEntityData();

        gameStateId = ed.createEntity();
        ed.setComponent(gameStateId, new PhaseComponent(ATTACK));

        playerId = ed.createEntity();
        ed.setComponents(playerId,
                new PlayerHealthComponent(PLAYER_MAX_HEALTH),
                new PositionComponent(0f, 0f, 0f));

        // Construct before any blocks so placements register as additions.
        system = new BlockEffectSystem(ed, playerId, gameStateId);
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

    // ── Shell reflect ────────────────────────────────────────────────────────

    @Test
    void destroyingShellReflectsToPlayer() {
        EntityId shell = createBlock(SHELL, 1f, 0f);
        system.update(0f);

        ed.removeEntity(shell);
        system.update(0f);

        assertEquals(PLAYER_MAX_HEALTH - SHELL_REFLECT_DAMAGE, playerHealth());
    }

    @Test
    void destroyingMultipleShellsChainsReflect() {
        EntityId a = createBlock(SHELL, 1f, 0f);
        EntityId b = createBlock(SHELL, 2f, 0f);
        EntityId c = createBlock(SHELL, 3f, 0f);
        system.update(0f);

        ed.removeEntity(a);
        ed.removeEntity(b);
        ed.removeEntity(c);
        system.update(0f);

        assertEquals(PLAYER_MAX_HEALTH - 3f * SHELL_REFLECT_DAMAGE, playerHealth());
    }

    @Test
    void destroyingNonShellDoesNotReflect() {
        EntityId sand = createBlock(SAND, 1f, 0f);
        system.update(0f);

        ed.removeEntity(sand);
        system.update(0f);

        assertEquals(PLAYER_MAX_HEALTH, playerHealth());
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

    // ── Helpers ──────────────────────────────────────────────────────────────

    private EntityId createBlock(BlockType type, float x, float z) {
        EntityId id = ed.createEntity();
        ed.setComponents(id, new BlockComponent(type), new PositionComponent(x, 0f, z));
        EffectComponent.forBlockType(type).ifPresent(effect -> ed.setComponent(id, effect));
        return id;
    }

    private float playerHealth() {
        return ed.getComponent(playerId, PlayerHealthComponent.class).current();
    }
}
