package com.ourcraft;

import com.ourcraft.ecs.components.GameResultComponent;
import com.ourcraft.ecs.components.GameResultComponent.Result;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.RoundComponent;
import com.ourcraft.ecs.systems.RoundSystem;
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

import static com.ourcraft.ecs.systems.RoundSystem.ATTACK_DURATION;
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
        assertEquals(4, round().maxRounds());
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
    void timerClampsToZeroAtFinalRound() {
        // Put game at final round so no advancement fires, just the clamp
        system.beginAttackPhase();
        ed.setComponent(gsId, new RoundComponent(4, 4, 10.0));
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
    void nonFinalRoundAdvancesOnTimerExpiry() {
        system.beginAttackPhase();
        system.update(61.0f);
        assertEquals(2, round().currentRound());
        assertEquals(Phase.BUILD, phase().phase());
        assertEquals(Result.IN_PROGRESS, result().result());
        assertEquals(ATTACK_DURATION, round().remainingSeconds(), 0.001);
    }

    @Test
    void noOverwriteWhenResultAlreadySet() {
        // Mark WIN before the final-round timer can write LOSS
        system.beginAttackPhase();
        ed.setComponent(gsId, new RoundComponent(4, 4, 60.0));
        ed.setComponent(gsId, new GameResultComponent(Result.WIN));

        system.update(61.0f);

        assertEquals(Result.WIN, result().result());
    }

    @Test
    void repeatedInitializationPreservesOriginalEntityAndState() {
        ed.setComponents(gsId,
                new RoundComponent(3, 4, 12.5),
                new PhaseComponent(Phase.ATTACK),
                new GameResultComponent(Result.WIN));

        system.initialize();

        assertEquals(gsId, system.getGameStateId());
        assertEquals(new RoundComponent(3, 4, 12.5), round());
        assertEquals(new PhaseComponent(Phase.ATTACK), phase());
        assertEquals(new GameResultComponent(Result.WIN), result());
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

    @Test
    void existingZeroTimerAdvancesExactlyOnce() {
        ed.setComponents(gsId,
                new RoundComponent(1, 4, 0.0),
                new PhaseComponent(Phase.ATTACK));

        system.update(0.0f);
        system.update(0.0f);

        assertEquals(new RoundComponent(2, 4, ATTACK_DURATION), round());
        assertEquals(Phase.BUILD, phase().phase());
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
