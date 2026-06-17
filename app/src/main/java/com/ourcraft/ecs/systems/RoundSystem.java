package com.ourcraft.ecs.systems;

import com.ourcraft.ecs.components.GameResultComponent;
import com.ourcraft.ecs.components.GameResultComponent.Result;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.RoundComponent;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;

import java.util.Objects;

/**
 * Owns the game-state entity's round/phase/timer in endless survival. The round number is unbounded:
 * the attack timer only ticks down (it does not advance the round), the round advances when a wall is
 * survived ({@link #advanceRound()}), and a timeout with blocks remaining is a game over handled by the
 * survival ({@code VictorySystem}) logic.
 */
public class RoundSystem {

    /** Base attack seconds (rounds 1–5); higher rounds grow from here. */
    public static final double ATTACK_DURATION = 60.0;
    /** Extra attack seconds per round past round 5 — walls keep growing, so the clock grows slowly too. */
    private static final double ATTACK_SECONDS_PER_ROUND = 2.0;
    /** Where the endless ramp begins; rounds at/below this use the flat base time. */
    private static final int ENDLESS_FROM_ROUND = 5;

    private final EntityData ed;
    private EntityId gameStateId;

    /**
     * Attack-phase seconds for a round. Flat {@link #ATTACK_DURATION} through round 5, then +2s per round
     * (unbounded) so the ever-larger endless walls stay clearable while the required clear-rate still rises
     * (see {@code NpcBuilderSystem.blocksForRound}).
     */
    public static double attackSecondsForRound(int round) {
        if (round <= ENDLESS_FROM_ROUND) {
            return ATTACK_DURATION;
        }
        return ATTACK_DURATION + ATTACK_SECONDS_PER_ROUND * (round - ENDLESS_FROM_ROUND);
    }

    public RoundSystem(EntityData ed) {
        this.ed = Objects.requireNonNull(ed, "ed");
    }

    public void initialize() {
        if (gameStateId != null) {
            return;
        }
        gameStateId = ed.createEntity();
        ed.setComponents(gameStateId,
                new RoundComponent(1, attackSecondsForRound(1)),
                new PhaseComponent(Phase.BUILD),
                new GameResultComponent(Result.IN_PROGRESS));
    }

    public EntityId getGameStateId() {
        return gameStateId;
    }

    /** Transitions from BUILD to ATTACK, resetting the attack timer. */
    public void beginAttackPhase() {
        GameState gameState = requireGameState();
        if (gameState.result().result() != Result.IN_PROGRESS
                || gameState.phase().phase() != Phase.BUILD) {
            return;
        }

        RoundComponent round = gameState.round();
        ed.setComponents(gameStateId,
                new PhaseComponent(Phase.ATTACK),
                new RoundComponent(round.currentRound(), attackSecondsForRound(round.currentRound())));
    }

    /** Advances to the next (harder) round after the current wall is survived. Unbounded. */
    public void advanceRound() {
        GameState gameState = requireGameState();
        if (gameState.result().result() != Result.IN_PROGRESS) {
            return;
        }

        RoundComponent round = gameState.round();
        int nextRound = round.currentRound() + 1;
        ed.setComponents(gameStateId,
                new RoundComponent(nextRound, attackSecondsForRound(nextRound)),
                new PhaseComponent(Phase.BUILD));
    }

    public void update(float tpf) {
        if (!Float.isFinite(tpf) || tpf < 0.0f) {
            throw new IllegalArgumentException("tpf must be finite and nonnegative");
        }

        GameState gameState = requireGameState();
        if (gameState.result().result() != Result.IN_PROGRESS) {
            return;
        }
        if (gameState.phase().phase() != Phase.ATTACK) {
            return;
        }

        RoundComponent round = gameState.round();
        double newRemaining = Math.max(0.0, round.remainingSeconds() - tpf);
        ed.setComponent(gameStateId, new RoundComponent(round.currentRound(), newRemaining));
    }

    private GameState requireGameState() {
        if (gameStateId == null) {
            throw new IllegalStateException("round system is not initialized");
        }

        RoundComponent round = requireComponent(RoundComponent.class);
        PhaseComponent phase = requireComponent(PhaseComponent.class);
        GameResultComponent result = requireComponent(GameResultComponent.class);
        return new GameState(round, phase, result);
    }

    private <T extends EntityComponent> T requireComponent(Class<T> componentType) {
        T component = ed.getComponent(gameStateId, componentType);
        if (component == null) {
            throw new IllegalStateException(
                    "game-state entity must have a " + componentType.getSimpleName());
        }
        return component;
    }

    private record GameState(
            RoundComponent round,
            PhaseComponent phase,
            GameResultComponent result
    ) {
    }
}
