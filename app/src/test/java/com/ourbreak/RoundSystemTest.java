package com.ourbreak;

import com.ourbreak.ecs.components.GameResultComponent;
import com.ourbreak.ecs.components.GameResultComponent.Result;
import com.ourbreak.ecs.components.PhaseComponent;
import com.ourbreak.ecs.components.PhaseComponent.Phase;
import com.ourbreak.ecs.components.RoundComponent;
import com.ourbreak.ecs.systems.RoundSystem;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.ourbreak.ecs.systems.RoundSystem.ATTACK_DURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoundSystemTest {

    private EntityData ed;
    private RoundSystem system;
    private EntityId gsId;

    @BeforeEach
    void setUp() {
        ed = new DefaultEntityData();
        system = new RoundSystem(ed);
        system.initialize();
        gsId = system.getGameStateId();
    }

    @Test
    void initialState() {
        assertEquals(1, round().currentRound());
        assertEquals(Phase.BUILD, phase().phase());
        assertEquals(Result.IN_PROGRESS, result().result());
        assertEquals(ATTACK_DURATION, round().remainingSeconds(), 0.001);
    }

    @Test
    void beginAttackPhaseSetsAttackAndResetsTimer() {
        system.beginAttackPhase();
        assertEquals(Phase.ATTACK, phase().phase());
        assertEquals(ATTACK_DURATION, round().remainingSeconds(), 0.001);
    }

    @Test
    void timerDecrementsInAttackPhase() {
        system.beginAttackPhase();
        system.update(30.0f);
        assertEquals(30.0, round().remainingSeconds(), 0.001);
    }

    @Test
    void timerClampsToZero() {
        system.beginAttackPhase();
        ed.setComponent(gsId, new RoundComponent(1, 10.0));
        system.update(15.0f);
        assertEquals(0.0, round().remainingSeconds(), 0.001);
    }

    @Test
    void timerDoesNotDecrementInBuildPhase() {
        double before = round().remainingSeconds();
        system.update(30.0f);
        assertEquals(before, round().remainingSeconds(), 0.001);
    }

    @Test
    void timerExpiryDoesNotAdvanceTheRound() {
        // Endless: the timer only ticks; surviving (clearing) advances the round, not the clock.
        system.beginAttackPhase();
        system.update(61.0f);
        assertEquals(1, round().currentRound());
        assertEquals(Phase.ATTACK, phase().phase());
        assertEquals(0.0, round().remainingSeconds(), 0.001);
    }

    @Test
    void advanceRoundIncrementsRoundResetsTimerAndEntersBuild() {
        system.beginAttackPhase();
        system.update(20.0f);

        system.advanceRound();

        assertEquals(2, round().currentRound());
        assertEquals(Phase.BUILD, phase().phase());
        assertEquals(ATTACK_DURATION, round().remainingSeconds(), 0.001);
        assertEquals(Result.IN_PROGRESS, result().result());
    }

    @Test
    void advanceRoundIsUnbounded() {
        ed.setComponent(gsId, new RoundComponent(7, 5.0));
        system.advanceRound();
        assertEquals(8, round().currentRound());
    }

    @Test
    void advanceRoundIsNoOpAfterGameOver() {
        ed.setComponent(gsId, new GameResultComponent(Result.LOSS));
        RoundComponent before = round();

        system.advanceRound();

        assertEquals(before, round());
        assertEquals(Phase.BUILD, phase().phase());
    }

    @Test
    void repeatedInitializationPreservesOriginalEntityAndState() {
        ed.setComponents(gsId,
                new RoundComponent(3, 12.5),
                new PhaseComponent(Phase.ATTACK),
                new GameResultComponent(Result.LOSS));

        system.initialize();

        assertEquals(gsId, system.getGameStateId());
        assertEquals(new RoundComponent(3, 12.5), round());
        assertEquals(new PhaseComponent(Phase.ATTACK), phase());
        assertEquals(new GameResultComponent(Result.LOSS), result());
    }

    @Test
    void repeatedAttackPhaseSignalDoesNotResetRunningTimer() {
        system.beginAttackPhase();
        system.update(10.0f);

        system.beginAttackPhase();

        assertEquals(Phase.ATTACK, phase().phase());
        assertEquals(50.0, round().remainingSeconds(), 0.001);
    }

    @ParameterizedTest
    @EnumSource(value = Result.class, names = {"WIN", "LOSS"})
    void completedGameAttackPhaseSignalIsNoOp(Result completedResult) {
        ed.setComponent(gsId, new GameResultComponent(completedResult));
        RoundComponent before = round();

        system.beginAttackPhase();

        assertEquals(Phase.BUILD, phase().phase());
        assertEquals(before, round());
    }

    @ParameterizedTest
    @ValueSource(floats = {-1.0f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    void invalidElapsedTimeIsRejectedWithoutStateChange(float tpf) {
        system.beginAttackPhase();
        GameState before = gameState();

        assertThrows(IllegalArgumentException.class, () -> system.update(tpf));

        assertEquals(before, gameState());
    }

    @ParameterizedTest
    @MethodSource("requiredComponentTypes")
    void updateRejectsMissingRequiredGameStateComponent(
            Class<? extends EntityComponent> componentType
    ) {
        ed.removeComponent(gsId, componentType);

        assertThrows(IllegalStateException.class, () -> system.update(0.0f));
    }

    @ParameterizedTest
    @MethodSource("requiredComponentTypes")
    void attackPhaseSignalRejectsMissingRequiredGameStateComponent(
            Class<? extends EntityComponent> componentType
    ) {
        ed.removeComponent(gsId, componentType);

        assertThrows(IllegalStateException.class, system::beginAttackPhase);
    }

    private static Stream<Class<? extends EntityComponent>> requiredComponentTypes() {
        return Stream.of(
                RoundComponent.class,
                PhaseComponent.class,
                GameResultComponent.class);
    }

    private GameState gameState() {
        return new GameState(round(), phase(), result());
    }

    private RoundComponent round() {
        return ed.getComponent(gsId, RoundComponent.class);
    }

    private PhaseComponent phase() {
        return ed.getComponent(gsId, PhaseComponent.class);
    }

    private GameResultComponent result() {
        return ed.getComponent(gsId, GameResultComponent.class);
    }

    private record GameState(
            RoundComponent round,
            PhaseComponent phase,
            GameResultComponent result
    ) {
    }
}
