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

public class RoundSystem {

    public static final double ATTACK_DURATION = 60.0;

    private final EntityData ed;
    private EntityId gameStateId;

    public RoundSystem(EntityData ed) {
        this.ed = Objects.requireNonNull(ed, "ed");
    }

    public void initialize() {
        if (gameStateId != null) {
            return;
        }
        gameStateId = ed.createEntity();
        ed.setComponents(gameStateId,
                new RoundComponent(1, 4, ATTACK_DURATION),
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
                new RoundComponent(round.currentRound(), round.maxRounds(), ATTACK_DURATION));
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

        if (newRemaining <= 0.0 && round.currentRound() < round.maxRounds()) {
            ed.setComponents(gameStateId,
                    new RoundComponent(round.currentRound() + 1, round.maxRounds(), ATTACK_DURATION),
                    new PhaseComponent(Phase.BUILD));
        } else {
            ed.setComponent(gameStateId,
                    new RoundComponent(round.currentRound(), round.maxRounds(), newRemaining));
        }
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
