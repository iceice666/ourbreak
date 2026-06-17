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
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class NpcBuilderSystem {

    // Onboarding ramp (rounds 1–4): a flat +8 blocks per round while the mechanics are introduced.
    public static final int BASE_BLOCKS = 16;
    public static final int BLOCKS_STEP = 8;
    /** Round at which the endless required-clear-rate ramp takes over (continuous with round-5 = 48). */
    public static final int ENDLESS_FROM_ROUND = 5;
    /** Required clear-rate (blocks/sec) at the start of the endless ramp. */
    private static final double RATE_BASE = 0.80;
    /** Asymptotic required clear-rate the ramp approaches but never reaches (kept below the human max). */
    private static final double RATE_MAX = 1.20;
    /** Per-round decay of the remaining rate margin — shrinking increments, so difficulty never spikes. */
    private static final double RATE_DECAY = 0.85;
    private final EntityData ed;
    private final RoundSystem roundSystem;
    private final EntityId mascotId;
    /** Base seed so each run's villages differ; XORed with the round for per-round variety. */
    private final long villageSeedBase = new Random().nextLong();

    private int activeRound = -1;
    private int placementsThisRound;
    private List<VillageGenerator.PlacedBlock> village = List.of();

    public NpcBuilderSystem(EntityData ed, RoundSystem roundSystem, EntityId mascotId) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.roundSystem = Objects.requireNonNull(roundSystem, "roundSystem");
        this.mascotId = Objects.requireNonNull(mascotId, "mascotId");
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
            // New round → generate a fresh village (a different seed each round and run).
            activeRound = round.currentRound();
            placementsThisRound = 0;
            village = VillageGenerator.generate(
                    blocksForRound(activeRound), blockScript(activeRound), villageSeedBase ^ activeRound);
        }
        int quota = village.size();
        if (placementsThisRound >= quota) {
            return;
        }

        PositionComponent mascotPosition = ed.getComponent(mascotId, PositionComponent.class);
        if (mascotPosition == null) {
            throw new IllegalStateException("mascot must have a PositionComponent");
        }

        // Place the next village block this frame (so the village visibly rises), translated onto the mascot.
        VillageGenerator.PlacedBlock next = village.get(placementsThisRound);
        PositionComponent placement = new PositionComponent(
                mascotPosition.x() + next.cell().x(),
                mascotPosition.y() + next.cell().y(),
                mascotPosition.z() + next.cell().z());
        BlockType blockType = next.type();

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
        // No retained EntitySet — the village is generated on the fly.
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

    /**
     * Blocks the NPC builds for a round. Rounds 1–4 are the onboarding ramp (16/24/32/40). From round 5
     * the wall grows without bound: the <em>required clear-rate</em> {@code ρ(r)} rises asymptotically
     * toward {@link #RATE_MAX} with shrinking increments (so it never spikes and is always theoretically
     * survivable), the attack time grows slowly ({@link RoundSystem#attackSecondsForRound}), and the
     * count is {@code round(ρ(r) × time(r))} — so the wall keeps getting bigger every round forever.
     */
    public static int blocksForRound(int round) {
        if (round < 1) {
            throw new IllegalStateException("no block quota for round " + round);
        }
        if (round < ENDLESS_FROM_ROUND) {
            return BASE_BLOCKS + (round - 1) * BLOCKS_STEP;
        }
        double rate = RATE_MAX - (RATE_MAX - RATE_BASE) * Math.pow(RATE_DECAY, round - ENDLESS_FROM_ROUND);
        return (int) Math.round(rate * RoundSystem.attackSecondsForRound(round));
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
}
