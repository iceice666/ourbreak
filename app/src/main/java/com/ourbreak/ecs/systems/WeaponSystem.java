package com.ourbreak.ecs.systems;

import com.ourbreak.ecs.components.BlockComponent;
import com.ourbreak.ecs.components.BlockComponent.BlockType;
import com.ourbreak.ecs.components.EffectComponent;
import com.ourbreak.ecs.components.GameResultComponent;
import com.ourbreak.ecs.components.GameResultComponent.Result;
import com.ourbreak.ecs.components.ModelComponent;
import com.ourbreak.ecs.components.PhaseComponent;
import com.ourbreak.ecs.components.PhaseComponent.Phase;
import com.ourbreak.ecs.components.PositionComponent;
import com.ourbreak.ecs.components.WeaponComponent;
import com.ourbreak.ecs.components.WeaponComponent.WeaponType;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WeaponSystem {

    // Per-weapon base damage (M7 tuning): each weapon has its own cadence/power, modulated by the
    // counter matrix. See design/gdd.md §Mechanics.
    public static final float SWORD_BASE_DAMAGE = 1.0f;
    // Gun is the single-target deleter: huge burst that one-shots even a Rock (durability 4) at its
    // weak ×0.5 multiplier. Its only weakness is having no AoE (slow against a whole wall).
    public static final float GUN_BASE_DAMAGE = 8.0f;
    public static final float DRONE_BASE_DAMAGE = 1.0f;

    public static final float STRONG_MULTIPLIER = 2.0f;
    public static final float WEAK_MULTIPLIER = 0.5f;
    public static final float NEUTRAL_MULTIPLIER = 1.0f;

    /** A Shell destroyed by the wrong weapon (Sword/Drone) splits into this many new Shells (uncapped). */
    public static final int SHELL_SPLIT_COUNT = 2;

    /** Rounds per drone level — the drone's blast grows one ring every this many rounds. */
    public static final int DRONE_ROUNDS_PER_LEVEL = 3;

    /**
     * The drone's upgrade level for a round (1-based, uncapped): +1 every {@link #DRONE_ROUNDS_PER_LEVEL}
     * rounds. The blast is a (2·level+1)² grid — Lv1 = 3×3, Lv2 = 5×5, Lv3 = 7×7, … — so it clears more
     * as the survival run escalates (and more easily catches Jellyfish/Shells, raising the risk too).
     */
    public static int droneLevelForRound(int round) {
        if (round < 1) {
            throw new IllegalArgumentException("round must be at least 1");
        }
        return 1 + (round - 1) / DRONE_ROUNDS_PER_LEVEL;
    }

    private final EntityData ed;
    private final EntityId gameStateId;
    private final EntitySet positionedBlocks;

    public WeaponSystem(EntityData ed, EntityId gameStateId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.gameStateId = Objects.requireNonNull(gameStateId, "gameStateId");
        this.positionedBlocks = ed.getEntities(BlockComponent.class, PositionComponent.class);
    }

    public void attack(EntityId playerId, Collection<EntityId> targetIds) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(targetIds, "targetIds");

        WeaponComponent weapon = ed.getComponent(playerId, WeaponComponent.class);
        if (weapon == null) {
            throw new IllegalStateException("attacking player must have a WeaponComponent");
        }

        PhaseComponent phase = requireGameStateComponent(PhaseComponent.class);
        GameResultComponent result = requireGameStateComponent(GameResultComponent.class);
        if (result.result() != Result.IN_PROGRESS || phase.phase() != Phase.ATTACK) {
            return;
        }

        WeaponType weaponType = weapon.weaponType();
        Set<PositionComponent> occupied = occupiedCells();

        for (EntityId targetId : new HashSet<>(targetIds)) {
            if (targetId == null) {
                continue;
            }

            BlockComponent block = ed.getComponent(targetId, BlockComponent.class);
            if (block == null) {
                continue;
            }

            if (block.blockType() == BlockType.SHELL) {
                resolveShell(targetId, weaponType, occupied);
                continue;
            }

            float damage = baseDamage(weaponType) * multiplier(weaponType, block.blockType());
            BlockComponent damagedBlock = block.applyDamage(damage);
            if (damagedBlock.durability() == 0.0f) {
                ed.removeEntity(targetId);
            } else {
                ed.setComponent(targetId, damagedBlock);
            }
        }
    }

    public void close() {
        positionedBlocks.release();
    }

    /**
     * Shell defends itself by multiplying: the Gun destroys it cleanly, but a Sword or Drone (which
     * the counter-matrix marks weak vs Shell) shatters it into {@link #SHELL_SPLIT_COUNT} new Shells.
     */
    private void resolveShell(EntityId shellId, WeaponType weaponType, Set<PositionComponent> occupied) {
        PositionComponent position = ed.getComponent(shellId, PositionComponent.class);
        ed.removeEntity(shellId);
        if (position != null) {
            occupied.remove(position);
        }

        boolean splits = weaponType == WeaponType.SWORD || weaponType == WeaponType.DRONE;
        if (!splits || position == null) {
            return;
        }
        for (PositionComponent cell : findEmptyCells(position, SHELL_SPLIT_COUNT, occupied)) {
            spawnShell(cell);
            occupied.add(cell);
        }
    }

    private Set<PositionComponent> occupiedCells() {
        positionedBlocks.applyChanges();
        Set<PositionComponent> occupied = new HashSet<>();
        for (Entity block : positionedBlocks) {
            occupied.add(block.get(PositionComponent.class));
        }
        return occupied;
    }

    /** Nearest unoccupied grid cells around {@code origin}, searching outward and upward. */
    private List<PositionComponent> findEmptyCells(PositionComponent origin, int count, Set<PositionComponent> occupied) {
        List<PositionComponent> found = new ArrayList<>(count);
        for (int radius = 1; found.size() < count; radius++) {
            for (int dy = 0; dy <= radius && found.size() < count; dy++) {
                for (int dx = -radius; dx <= radius && found.size() < count; dx++) {
                    for (int dz = -radius; dz <= radius && found.size() < count; dz++) {
                        if (Math.max(Math.abs(dx), Math.max(dy, Math.abs(dz))) != radius) {
                            continue; // only the surface of the current cube shell
                        }
                        PositionComponent cell = new PositionComponent(
                                origin.x() + dx, origin.y() + dy, origin.z() + dz);
                        if (!occupied.contains(cell) && !found.contains(cell)) {
                            found.add(cell);
                        }
                    }
                }
            }
        }
        return found;
    }

    private void spawnShell(PositionComponent position) {
        EntityId id = ed.createEntity();
        ed.setComponents(id,
                new BlockComponent(BlockType.SHELL),
                position,
                new ModelComponent("shell-block"));
        EffectComponent.forBlockType(BlockType.SHELL).ifPresent(effect -> ed.setComponent(id, effect));
    }

    private <T extends EntityComponent> T requireGameStateComponent(Class<T> componentType) {
        T component = ed.getComponent(gameStateId, componentType);
        if (component == null) {
            throw new IllegalStateException(
                    "game-state entity must have a " + componentType.getSimpleName());
        }
        return component;
    }

    private float baseDamage(WeaponType weaponType) {
        return switch (weaponType) {
            case SWORD -> SWORD_BASE_DAMAGE;
            case GUN -> GUN_BASE_DAMAGE;
            case DRONE -> DRONE_BASE_DAMAGE;
        };
    }

    private float multiplier(WeaponType weaponType, BlockType blockType) {
        return switch (weaponType) {
            case SWORD -> switch (blockType) {
                case SAND -> STRONG_MULTIPLIER;
                case SHELL, CORAL -> WEAK_MULTIPLIER;
                case ROCK, JELLYFISH -> NEUTRAL_MULTIPLIER;
            };
            case GUN -> switch (blockType) {
                case CORAL, JELLYFISH -> STRONG_MULTIPLIER;
                case ROCK -> WEAK_MULTIPLIER;
                case SAND, SHELL -> NEUTRAL_MULTIPLIER;
            };
            case DRONE -> switch (blockType) {
                case ROCK, SAND -> STRONG_MULTIPLIER;
                case JELLYFISH, SHELL -> WEAK_MULTIPLIER;
                case CORAL -> NEUTRAL_MULTIPLIER;
            };
        };
    }
}
