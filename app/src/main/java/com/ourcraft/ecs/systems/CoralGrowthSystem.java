package com.ourcraft.ecs.systems;

import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.BlockComponent.BlockType;
import com.ourcraft.ecs.components.EffectComponent;
import com.ourcraft.ecs.components.ModelComponent;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.PositionComponent;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Coral regrowth (GDD §Mechanics). During ATTACK every living Coral periodically "heals" the wall:
 * it grows a fresh Coral into an adjacent empty cell, but only within the wall's original footprint
 * (snapshotted at the start of the attack). So as the player clears holes near a Coral, the Coral
 * fills them back in — you can never finish clearing the wall while a Coral lives, no matter how far
 * away you stand. The newly-grown Coral also spreads, snowballing, but the footprint caps the total
 * (it can at most regrow to the full original wall, never beyond it).
 *
 * <p>The counter is the Gun: a single precise shot deletes each Coral, so the answer is to pick the
 * Coral "sources" off first rather than chip the soft blocks and let the wall grow back.
 */
public class CoralGrowthSystem {

    /** Seconds between regrowth ticks: every living Coral grows one new Coral each interval. */
    public static final float GROWTH_INTERVAL = 7.0f;

    private static final Comparator<PositionComponent> CELL_ORDER =
            Comparator.comparingDouble(PositionComponent::x)
                    .thenComparingDouble(PositionComponent::y)
                    .thenComparingDouble(PositionComponent::z);

    private final EntityData ed;
    private final EntityId gameStateId;
    private final EntitySet blocks;

    private final Set<PositionComponent> wallRegion = new HashSet<>();
    private boolean regionCaptured;
    private float timer;

    public CoralGrowthSystem(EntityData ed, EntityId gameStateId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.gameStateId = Objects.requireNonNull(gameStateId, "gameStateId");
        this.blocks = ed.getEntities(BlockComponent.class, PositionComponent.class);
    }

    public void update(float tpf) {
        PhaseComponent phase = ed.getComponent(gameStateId, PhaseComponent.class);
        if (phase == null || phase.phase() != Phase.ATTACK) {
            // Between attacks (BUILD / resolved): forget the footprint so the next round re-snapshots.
            regionCaptured = false;
            wallRegion.clear();
            timer = 0f;
            return;
        }

        blocks.applyChanges();
        if (!regionCaptured) {
            wallRegion.clear();
            for (Entity block : blocks) {
                wallRegion.add(block.get(PositionComponent.class));
            }
            regionCaptured = true;
        }

        timer += tpf;
        while (timer >= GROWTH_INTERVAL) {
            timer -= GROWTH_INTERVAL;
            grow();
        }
    }

    private void grow() {
        Set<PositionComponent> occupied = new HashSet<>();
        List<PositionComponent> coral = new ArrayList<>();
        for (Entity block : blocks) {
            PositionComponent cell = block.get(PositionComponent.class);
            occupied.add(cell);
            if (block.get(BlockComponent.class).blockType() == BlockType.CORAL) {
                coral.add(cell);
            }
        }
        for (PositionComponent target : growthTargets(coral, occupied, wallRegion)) {
            spawnCoral(target);
        }
    }

    private void spawnCoral(PositionComponent position) {
        EntityId id = ed.createEntity();
        ed.setComponents(id,
                new BlockComponent(BlockType.CORAL),
                position,
                new ModelComponent("coral-block"));
        EffectComponent.forBlockType(BlockType.CORAL).ifPresent(effect -> ed.setComponent(id, effect));
    }

    public void close() {
        blocks.release();
    }

    /**
     * Pure regrowth resolver: for each living Coral, the first adjacent (face-neighbour) cell that is
     * inside the wall footprint and currently empty becomes a new-Coral target. Cells are reserved as
     * they're chosen, so two Corals competing for the same empty cell only spawn one. Deterministic.
     */
    public static List<PositionComponent> growthTargets(List<PositionComponent> coralCells,
                                                        Set<PositionComponent> occupied,
                                                        Set<PositionComponent> wallRegion) {
        List<PositionComponent> targets = new ArrayList<>();
        Set<PositionComponent> reserved = new HashSet<>(occupied);

        List<PositionComponent> ordered = new ArrayList<>(coralCells);
        ordered.sort(CELL_ORDER);

        for (PositionComponent coral : ordered) {
            for (PositionComponent neighbour : faceNeighbours(coral)) {
                if (wallRegion.contains(neighbour) && !reserved.contains(neighbour)) {
                    targets.add(neighbour);
                    reserved.add(neighbour);
                    break;
                }
            }
        }
        return targets;
    }

    private static List<PositionComponent> faceNeighbours(PositionComponent c) {
        return List.of(
                new PositionComponent(c.x() - 1, c.y(), c.z()),
                new PositionComponent(c.x() + 1, c.y(), c.z()),
                new PositionComponent(c.x(), c.y() - 1, c.z()),
                new PositionComponent(c.x(), c.y() + 1, c.z()),
                new PositionComponent(c.x(), c.y(), c.z() - 1),
                new PositionComponent(c.x(), c.y(), c.z() + 1));
    }
}
