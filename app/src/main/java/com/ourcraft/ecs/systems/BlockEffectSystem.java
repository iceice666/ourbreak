package com.ourcraft.ecs.systems;

import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.BlockComponent.BlockType;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.PlayerHealthComponent;
import com.ourcraft.ecs.components.PositionComponent;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Headless block special-effect triggers (GDD §Mechanics). Computes the Coral proximity slow,
 * applies Shell on-destroy reflect to the player's health, records Jellyfish placement flickers,
 * and expands a Drone hit into its 3x3 neighborhood. All magnitudes are placeholders (tuned in M7).
 */
public class BlockEffectSystem {

    /** Movement multiplier while within range of a Coral block — half speed (GDD §Mechanics). */
    public static final float CORAL_SLOW_FACTOR = 0.5f;
    /** Coral slow range in grid cells (GDD: 1.5). */
    public static final float CORAL_RANGE = 1.5f;
    /** Health removed per destroyed Shell block (GDD §Mechanics). */
    public static final float SHELL_REFLECT_DAMAGE = 20.0f;
    /** Jellyfish vision-flicker duration in seconds — reserved for the flicker visual (GDD §Mechanics). */
    public static final float JELLYFISH_FLICKER_SECONDS = 2.0f;

    private final EntityData ed;
    private final EntityId playerId;
    private final EntityId gameStateId;

    private final EntitySet positionedBlocks;
    private final EntitySet trackedBlocks;
    private final Map<EntityId, BlockType> trackedTypes = new HashMap<>();

    private int flickerTriggerCount;

    public BlockEffectSystem(EntityData ed, EntityId playerId, EntityId gameStateId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.gameStateId = Objects.requireNonNull(gameStateId, "gameStateId");
        this.positionedBlocks = ed.getEntities(BlockComponent.class, PositionComponent.class);
        this.trackedBlocks = ed.getEntities(BlockComponent.class);

        // Seed the type map so blocks already present can still reflect on destruction; pre-existing
        // blocks are intentionally not counted as placement flickers.
        trackedBlocks.applyChanges();
        for (Entity block : trackedBlocks) {
            trackedTypes.put(block.getId(), block.get(BlockComponent.class).blockType());
        }
    }

    /** Strongest Coral slow factor for the player position during ATTACK; 1.0 when none applies. */
    public float coralSlowFactor(PositionComponent playerPosition) {
        Objects.requireNonNull(playerPosition, "playerPosition");

        PhaseComponent phase = ed.getComponent(gameStateId, PhaseComponent.class);
        if (phase == null || phase.phase() != Phase.ATTACK) {
            return 1.0f;
        }

        positionedBlocks.applyChanges();
        for (Entity block : positionedBlocks) {
            if (block.get(BlockComponent.class).blockType() != BlockType.CORAL) {
                continue;
            }
            PositionComponent p = block.get(PositionComponent.class);
            float dx = p.x() - playerPosition.x();
            float dz = p.z() - playerPosition.z();
            if (Math.sqrt(dx * dx + dz * dz) <= CORAL_RANGE) {
                return CORAL_SLOW_FACTOR;
            }
        }
        return 1.0f;
    }

    /** Block entities in the 3x3 grid neighborhood around the center block (center + occupied neighbors). */
    public List<EntityId> droneAreaTargets(EntityId centerBlockId) {
        Objects.requireNonNull(centerBlockId, "centerBlockId");

        PositionComponent center = ed.getComponent(centerBlockId, PositionComponent.class);
        if (center == null) {
            return List.of();
        }

        positionedBlocks.applyChanges();
        Map<PositionComponent, EntityId> byPosition = new HashMap<>();
        for (Entity block : positionedBlocks) {
            byPosition.put(block.get(PositionComponent.class), block.getId());
        }

        List<EntityId> targets = new ArrayList<>(9);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                PositionComponent candidate =
                        new PositionComponent(center.x() + dx, center.y(), center.z() + dz);
                EntityId id = byPosition.get(candidate);
                if (id != null) {
                    targets.add(id);
                }
            }
        }
        return targets;
    }

    /** Number of Jellyfish placement flicker triggers recorded so far (consumed by the HUD in M6). */
    public int flickerTriggerCount() {
        return flickerTriggerCount;
    }

    /** Advances destroy/placement detection: applies Shell reflect and records Jellyfish flickers. */
    public void update(float tpf) {
        trackedBlocks.applyChanges();

        for (Entity removed : trackedBlocks.getRemovedEntities()) {
            BlockType type = trackedTypes.remove(removed.getId());
            if (type == BlockType.SHELL) {
                applyReflect();
            }
        }
        for (Entity added : trackedBlocks.getAddedEntities()) {
            BlockType type = added.get(BlockComponent.class).blockType();
            trackedTypes.put(added.getId(), type);
            if (type == BlockType.JELLYFISH) {
                flickerTriggerCount++;
            }
        }
    }

    public void close() {
        positionedBlocks.release();
        trackedBlocks.release();
    }

    private void applyReflect() {
        PlayerHealthComponent health = ed.getComponent(playerId, PlayerHealthComponent.class);
        if (health != null) {
            ed.setComponent(playerId, health.applyDamage(SHELL_REFLECT_DAMAGE));
        }
    }
}
