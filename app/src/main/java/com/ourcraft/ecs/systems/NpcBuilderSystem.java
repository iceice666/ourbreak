package com.ourcraft.ecs.systems;

import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.BlockComponent.BlockType;
import com.ourcraft.ecs.components.EffectComponent;
import com.ourcraft.ecs.components.GameResultComponent;
import com.ourcraft.ecs.components.GameResultComponent.Result;
import com.ourcraft.ecs.components.ModelComponent;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.PositionComponent;
import com.ourcraft.ecs.components.RoundComponent;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class NpcBuilderSystem {

    // Endless escalation: the wall grows each round, then plateaus at a 60 s-survivable ceiling.
    public static final int BASE_BLOCKS = 16;
    public static final int BLOCKS_STEP = 8;
    public static final int MAX_BLOCKS = 48;

    private final EntityData ed;
    private final RoundSystem roundSystem;
    private final EntityId mascotId;
    private final EntitySet positionedBlocks;

    private int activeRound = -1;
    private int placementsThisRound;

    public NpcBuilderSystem(EntityData ed, RoundSystem roundSystem, EntityId mascotId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.roundSystem = Objects.requireNonNull(roundSystem, "roundSystem");
        this.mascotId = Objects.requireNonNull(mascotId, "mascotId");
        this.positionedBlocks = ed.getEntities(BlockComponent.class, PositionComponent.class);
    }

    public void update(float tpf) {
        EntityId gameStateId = roundSystem.getGameStateId();
        if (gameStateId == null) {
            throw new IllegalStateException("round system must be initialized");
        }

        RoundComponent round = requireGameStateComponent(gameStateId, RoundComponent.class);
        PhaseComponent phase = requireGameStateComponent(gameStateId, PhaseComponent.class);
        GameResultComponent result = requireGameStateComponent(gameStateId, GameResultComponent.class);
        if (result.result() != Result.IN_PROGRESS || phase.phase() != Phase.BUILD) {
            return;
        }

        if (round.currentRound() != activeRound) {
            activeRound = round.currentRound();
            placementsThisRound = 0;
        }
        int quota = blocksForRound(activeRound);
        if (placementsThisRound >= quota) {
            return;
        }

        List<BlockType> script = blockScript(activeRound);
        positionedBlocks.applyChanges();
        PositionComponent mascotPosition = ed.getComponent(mascotId, PositionComponent.class);
        if (mascotPosition == null) {
            throw new IllegalStateException("mascot must have a PositionComponent");
        }

        PositionComponent placement = findFirstAvailablePosition(mascotPosition);
        BlockType blockType = script.get(placementsThisRound % script.size());

        EntityId blockId = ed.createEntity();
        ed.setComponents(blockId,
                placement,
                new BlockComponent(blockType),
                new ModelComponent(modelId(blockType)));
        EffectComponent.forBlockType(blockType)
                .ifPresent(effect -> ed.setComponent(blockId, effect));

        placementsThisRound++;
        if (placementsThisRound == quota) {
            roundSystem.beginAttackPhase();
        }
    }

    public void close() {
        positionedBlocks.release();
    }

    private <T extends EntityComponent> T requireGameStateComponent(
            EntityId gameStateId,
            Class<T> componentType
    ) {
        T component = ed.getComponent(gameStateId, componentType);
        if (component == null) {
            throw new IllegalStateException(
                    "game-state entity must have a " + componentType.getSimpleName());
        }
        return component;
    }

    private PositionComponent findFirstAvailablePosition(PositionComponent mascotPosition) {
        Set<PositionComponent> occupied = new HashSet<>();
        for (Entity block : positionedBlocks) {
            occupied.add(block.get(PositionComponent.class));
        }

        int candidatesToCheck = occupied.size() + 1;
        int checked = 0;
        for (int radius = 1; checked < candidatesToCheck; radius++) {
            for (GridOffset offset : ringOffsets(radius)) {
                PositionComponent candidate = new PositionComponent(
                        mascotPosition.x() + offset.x(),
                        mascotPosition.y(),
                        mascotPosition.z() + offset.z());
                if (!occupied.contains(candidate)) {
                    return candidate;
                }
                checked++;
                if (checked == candidatesToCheck) {
                    break;
                }
            }
        }

        throw new IllegalStateException("unable to find an unoccupied block position");
    }

    private List<GridOffset> ringOffsets(int radius) {
        List<GridOffset> offsets = new ArrayList<>(radius * 8);
        offsets.add(new GridOffset(0, radius));
        offsets.add(new GridOffset(-radius, 0));
        offsets.add(new GridOffset(radius, 0));

        for (int x = 1; x <= radius; x++) {
            offsets.add(new GridOffset(-x, radius));
            offsets.add(new GridOffset(x, radius));
        }

        for (int z = radius - 1; z >= -radius; z--) {
            if (z == 0) {
                continue;
            }
            offsets.add(new GridOffset(-radius, z));
            offsets.add(new GridOffset(radius, z));
        }

        for (int x = radius - 1; x >= 1; x--) {
            offsets.add(new GridOffset(-x, -radius));
            offsets.add(new GridOffset(x, -radius));
        }
        offsets.add(new GridOffset(0, -radius));
        return offsets;
    }

    public static int blocksForRound(int round) {
        if (round < 1) {
            throw new IllegalStateException("no block quota for round " + round);
        }
        return Math.min(BASE_BLOCKS + (round - 1) * BLOCKS_STEP, MAX_BLOCKS);
    }

    private List<BlockType> blockScript(int round) {
        return switch (round) {
            case 1 -> List.of(BlockType.SAND);
            case 2 -> List.of(BlockType.SAND, BlockType.CORAL);
            case 3 -> List.of(BlockType.ROCK, BlockType.SHELL);
            case 4 -> List.of(BlockType.ROCK, BlockType.JELLYFISH);
            default -> {
                if (round < 1) {
                    throw new IllegalStateException("no NPC build script for round " + round);
                }
                // Round 5+: the full gauntlet — tanky rock plus every effect block.
                yield List.of(BlockType.ROCK, BlockType.SHELL, BlockType.JELLYFISH, BlockType.CORAL);
            }
        };
    }

    private String modelId(BlockType blockType) {
        return blockType.name().toLowerCase(Locale.ROOT) + "-block";
    }

    private record GridOffset(int x, int z) {
    }
}
