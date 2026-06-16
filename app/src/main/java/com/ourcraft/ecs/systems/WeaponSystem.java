package com.ourcraft.ecs.systems;

import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.BlockComponent.BlockType;
import com.ourcraft.ecs.components.GameResultComponent;
import com.ourcraft.ecs.components.GameResultComponent.Result;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.WeaponComponent;
import com.ourcraft.ecs.components.WeaponComponent.WeaponType;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class WeaponSystem {

    // Per-weapon base damage (M7 tuning): each weapon has its own cadence/power, modulated by the
    // counter matrix. See design/gdd.md §Mechanics.
    public static final float SWORD_BASE_DAMAGE = 1.0f;
    public static final float GUN_BASE_DAMAGE = 2.0f;
    public static final float DRONE_BASE_DAMAGE = 1.0f;

    public static final float STRONG_MULTIPLIER = 2.0f;
    public static final float WEAK_MULTIPLIER = 0.5f;
    public static final float NEUTRAL_MULTIPLIER = 1.0f;

    private final EntityData ed;
    private final EntityId gameStateId;

    public WeaponSystem(EntityData ed, EntityId gameStateId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.gameStateId = Objects.requireNonNull(gameStateId, "gameStateId");
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

        for (EntityId targetId : new HashSet<>(targetIds)) {
            if (targetId == null) {
                continue;
            }

            BlockComponent block = ed.getComponent(targetId, BlockComponent.class);
            if (block == null) {
                continue;
            }

            float damage = baseDamage(weapon.weaponType())
                    * multiplier(weapon.weaponType(), block.blockType());
            BlockComponent damagedBlock = block.applyDamage(damage);
            if (damagedBlock.durability() == 0.0f) {
                ed.removeEntity(targetId);
            } else {
                ed.setComponent(targetId, damagedBlock);
            }
        }
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
