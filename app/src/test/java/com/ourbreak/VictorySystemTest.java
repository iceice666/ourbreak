package com.ourbreak;

import com.ourbreak.ecs.components.BlockComponent;
import com.ourbreak.ecs.components.GameResultComponent;
import com.ourbreak.ecs.components.GameResultComponent.Result;
import com.ourbreak.ecs.components.PhaseComponent;
import com.ourbreak.ecs.components.PhaseComponent.Phase;
import com.ourbreak.ecs.components.RoundComponent;
import com.ourbreak.ecs.systems.RoundSystem;
import com.ourbreak.ecs.systems.VictorySystem;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ourbreak.ecs.components.BlockComponent.BlockType.ROCK;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VictorySystemTest {

    private EntityData ed;
    private RoundSystem roundSystem;
    private VictorySystem victorySystem;
    private EntityId gsId;

    @BeforeEach
    void setUp() {
        ed = new DefaultEntityData();
        roundSystem = new RoundSystem(ed);
        roundSystem.initialize();
        gsId = roundSystem.getGameStateId();
        victorySystem = new VictorySystem(ed, gsId, roundSystem);
    }

    @AfterEach
    void tearDown() {
        victorySystem.close();
    }

    @Test
    void survivingClearsAdvancesToNextRound() {
        roundSystem.beginAttackPhase();
        victorySystem.update(1.0f);
        assertEquals(2, round().currentRound());
        assertEquals(Phase.BUILD, phase());
        assertEquals(Result.IN_PROGRESS, result());
    }

    @Test
    void noAdvanceInBuildPhaseEvenWithNoBlocks() {
        victorySystem.update(1.0f);
        assertEquals(1, round().currentRound());
        assertEquals(Result.IN_PROGRESS, result());
    }

    @Test
    void noAdvanceWhenBlocksRemainInAttackPhase() {
        addBlock();
        roundSystem.beginAttackPhase();
        victorySystem.update(1.0f);
        assertEquals(1, round().currentRound());
        assertEquals(Result.IN_PROGRESS, result());
    }

    @Test
    void gameOverWhenTimerExpiresWithBlocksAtAnyRound() {
        addBlock();
        roundSystem.beginAttackPhase();
        ed.setComponent(gsId, new RoundComponent(3, 0.0)); // not a "final" round — any round

        victorySystem.update(0.0f);

        assertEquals(Result.LOSS, result());
    }

    @Test
    void survivalTakesPrecedenceOverSimultaneousTimeout() {
        roundSystem.beginAttackPhase();
        ed.setComponent(gsId, new RoundComponent(2, 0.0)); // timer 0 but no blocks

        victorySystem.update(0.0f);

        assertEquals(3, round().currentRound()); // survived → advanced, not game over
        assertEquals(Result.IN_PROGRESS, result());
    }

    @Test
    void noGameOverWhileTimeRemains() {
        addBlock();
        roundSystem.beginAttackPhase(); // timer = 60
        victorySystem.update(1.0f);
        assertEquals(Result.IN_PROGRESS, result());
        assertEquals(1, round().currentRound());
    }

    @Test
    void survivalAdvancesOncePerClear() {
        roundSystem.beginAttackPhase();
        victorySystem.update(1.0f); // advance to round 2 (BUILD)
        victorySystem.update(1.0f); // BUILD phase now → no further advance
        assertEquals(2, round().currentRound());
    }

    @Test
    void idempotentGameOver() {
        addBlock();
        roundSystem.beginAttackPhase();
        ed.setComponent(gsId, new RoundComponent(2, 0.0));
        victorySystem.update(0.0f); // writes LOSS
        victorySystem.update(0.0f); // must not change it
        assertEquals(Result.LOSS, result());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void addBlock() {
        EntityId block = ed.createEntity();
        ed.setComponents(block, new BlockComponent(ROCK));
    }

    private RoundComponent round() {
        return ed.getComponent(gsId, RoundComponent.class);
    }

    private Phase phase() {
        return ed.getComponent(gsId, PhaseComponent.class).phase();
    }

    private Result result() {
        return ed.getComponent(gsId, GameResultComponent.class).result();
    }
}
