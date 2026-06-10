package com.ourcraft;

import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.GameResultComponent;
import com.ourcraft.ecs.components.GameResultComponent.Result;
import com.ourcraft.ecs.components.RoundComponent;
import com.ourcraft.ecs.systems.RoundSystem;
import com.ourcraft.ecs.systems.VictorySystem;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ourcraft.ecs.components.BlockComponent.BlockType.ROCK;
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
        victorySystem = new VictorySystem(ed, gsId);
    }

    @AfterEach
    void tearDown() {
        victorySystem.close();
    }

    @Test
    void winWhenNoBlocksInAttackPhase() {
        roundSystem.beginAttackPhase();
        victorySystem.update(1.0f);
        assertEquals(Result.WIN, result());
    }

    @Test
    void noWinInBuildPhaseEvenWithNoBlocks() {
        // Phase is BUILD at start — no blocks exist
        victorySystem.update(1.0f);
        assertEquals(Result.IN_PROGRESS, result());
    }

    @Test
    void noWinWhenBlocksRemainInAttackPhase() {
        EntityId block = ed.createEntity();
        ed.setComponents(block, new BlockComponent(ROCK));
        roundSystem.beginAttackPhase();
        victorySystem.update(1.0f);
        assertEquals(Result.IN_PROGRESS, result());
    }

    @Test
    void lossAtFinalRoundWhenTimerExpiredWithBlocksRemaining() {
        EntityId block = ed.createEntity();
        ed.setComponents(block, new BlockComponent(ROCK));

        // Jump to final round and expire the timer
        ed.setComponent(gsId, new RoundComponent(4, 4, 60.0));
        roundSystem.beginAttackPhase();
        roundSystem.update(61.0f); // timer → 0, no advancement at final round

        victorySystem.update(0.0f);

        assertEquals(Result.LOSS, result());
    }

    @Test
    void blockClearanceWinsAtSimultaneousFinalRoundExpiry() {
        roundSystem.beginAttackPhase();
        ed.setComponent(gsId, new RoundComponent(4, 4, 0.0));

        victorySystem.update(0.0f);

        assertEquals(Result.WIN, result());
    }

    @Test
    void noLossOnNonFinalRoundTimerExpiry() {
        EntityId block = ed.createEntity();
        ed.setComponents(block, new BlockComponent(ROCK));

        roundSystem.beginAttackPhase();
        roundSystem.update(61.0f); // advances to round 2 (BUILD)

        victorySystem.update(0.0f);

        assertEquals(Result.IN_PROGRESS, result());
    }

    @Test
    void idempotentWin() {
        roundSystem.beginAttackPhase();
        victorySystem.update(1.0f); // writes WIN (no blocks)
        victorySystem.update(1.0f); // must not change it
        assertEquals(Result.WIN, result());
    }

    @Test
    void idempotentLoss() {
        EntityId block = ed.createEntity();
        ed.setComponents(block, new BlockComponent(ROCK));

        ed.setComponent(gsId, new RoundComponent(4, 4, 60.0));
        roundSystem.beginAttackPhase();
        roundSystem.update(61.0f);
        victorySystem.update(0.0f); // writes LOSS
        victorySystem.update(0.0f); // must not change it
        assertEquals(Result.LOSS, result());
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private Result result() {
        return ed.getComponent(gsId, GameResultComponent.class).result();
    }
}
