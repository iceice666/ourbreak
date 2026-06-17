package com.ourbreak.ecs.systems;

import com.ourbreak.ecs.components.BlockComponent;
import com.ourbreak.ecs.components.GameResultComponent;
import com.ourbreak.ecs.components.GameResultComponent.Result;
import com.ourbreak.ecs.components.PhaseComponent;
import com.ourbreak.ecs.components.PhaseComponent.Phase;
import com.ourbreak.ecs.components.RoundComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.Objects;

/**
 * Endless survival progression. During ATTACK: clearing the whole wall (no blocks left) means the
 * round was survived and the run advances to the next, harder round; the attack timer reaching zero
 * with blocks still standing is a game over (LOSS) at any round. There is no win state.
 */
public class VictorySystem {

    private final EntityData ed;
    private final EntityId gameStateId;
    private final RoundSystem roundSystem;
    private final EntitySet blocks;

    public VictorySystem(EntityData ed, EntityId gameStateId, RoundSystem roundSystem) {
        this.ed = Objects.requireNonNull(ed, "ed");
        this.gameStateId = Objects.requireNonNull(gameStateId, "gameStateId");
        this.roundSystem = Objects.requireNonNull(roundSystem, "roundSystem");
        this.blocks = ed.getEntities(BlockComponent.class);
    }

    public void update(float tpf) {
        GameResultComponent result = ed.getComponent(gameStateId, GameResultComponent.class);
        if (result.result() != Result.IN_PROGRESS) return;

        PhaseComponent phase = ed.getComponent(gameStateId, PhaseComponent.class);
        if (phase.phase() != Phase.ATTACK) return;

        blocks.applyChanges();
        if (blocks.size() == 0) {
            // Survived the round (precedence over a simultaneous timeout) → advance to a harder round.
            roundSystem.advanceRound();
            return;
        }

        RoundComponent round = ed.getComponent(gameStateId, RoundComponent.class);
        if (round.remainingSeconds() <= 0.0) {
            // Time ran out with blocks still standing — game over, at any round.
            ed.setComponent(gameStateId, new GameResultComponent(Result.LOSS));
        }
    }

    public void close() {
        blocks.release();
    }
}
