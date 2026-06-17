package com.ourbreak.ecs.systems;

import com.ourbreak.ecs.components.BlockComponent;
import com.ourbreak.ecs.components.BlockComponent.BlockType;
import com.ourbreak.ecs.components.PhaseComponent;
import com.ourbreak.ecs.components.PhaseComponent.Phase;
import com.ourbreak.ecs.components.PositionComponent;
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
 * Headless block special-effect triggers (GDD §Mechanics): the Coral proximity slow, the Drone 3x3
 * neighborhood expansion, and Jellyfish placement flicker triggers. (Shell's behaviour is the split
 * mechanic in {@code WeaponSystem}; there is no player health.)
 */
public class BlockEffectSystem {

    /** Movement multiplier while within range of a Coral block — half speed (GDD §Mechanics). */
    public static final float CORAL_SLOW_FACTOR = 0.5f;
    /** Coral slow range in grid cells (GDD: 1.5). */
    public static final float CORAL_RANGE = 1.5f;
    /** Jellyfish vision-flicker duration in seconds — reserved for the flicker visual (GDD §Mechanics). */
    public static final float JELLYFISH_FLICKER_SECONDS = 2.0f;

    private final EntityData ed;
    private final EntityId gameStateId;

    private final EntitySet positionedBlocks;
    private final EntitySet trackedBlocks;

    private int flickerTriggerCount;

    public BlockEffectSystem(EntityData ed, EntityId gameStateId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.gameStateId = Objects.requireNonNull(gameStateId, "gameStateId");
        this.positionedBlocks = ed.getEntities(BlockComponent.class, PositionComponent.class);
        this.trackedBlocks = ed.getEntities(BlockComponent.class);
        // Drain the initial set so pre-existing blocks are not counted as placement flickers.
        trackedBlocks.applyChanges();
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
        return droneAreaTargets(centerBlockId, 1);
    }

    /** Occupied blocks in the (2·radius+1)² grid square (same height) centred on the block. */
    public List<EntityId> droneAreaTargets(EntityId centerBlockId, int radius) {
        Objects.requireNonNull(centerBlockId, "centerBlockId");
        if (radius < 1) {
            throw new IllegalArgumentException("radius must be at least 1");
        }

        PositionComponent center = ed.getComponent(centerBlockId, PositionComponent.class);
        if (center == null) {
            return List.of();
        }

        positionedBlocks.applyChanges();
        Map<PositionComponent, EntityId> byPosition = new HashMap<>();
        for (Entity block : positionedBlocks) {
            byPosition.put(block.get(PositionComponent.class), block.getId());
        }

        List<EntityId> targets = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
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

    /** Block entities in a 3-cell horizontal row around the center along the chosen axis (X or Z). */
    public List<EntityId> rowTargets(EntityId centerBlockId, boolean alongX) {
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

        List<EntityId> targets = new ArrayList<>(3);
        for (int d = -1; d <= 1; d++) {
            PositionComponent cell = new PositionComponent(
                    center.x() + (alongX ? d : 0),
                    center.y(),
                    center.z() + (alongX ? 0 : d));
            EntityId id = byPosition.get(cell);
            if (id != null) {
                targets.add(id);
            }
        }
        return targets;
    }

    /** Number of Jellyfish placement flicker triggers recorded so far (consumed by the HUD). */
    public int flickerTriggerCount() {
        return flickerTriggerCount;
    }

    /** Records a flicker trigger for each newly placed Jellyfish block. */
    public void update(float tpf) {
        trackedBlocks.applyChanges();
        for (Entity added : trackedBlocks.getAddedEntities()) {
            if (added.get(BlockComponent.class).blockType() == BlockType.JELLYFISH) {
                flickerTriggerCount++;
            }
        }
    }

    public void close() {
        positionedBlocks.release();
        trackedBlocks.release();
    }
}
