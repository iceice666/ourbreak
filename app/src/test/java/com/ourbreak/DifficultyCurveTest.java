package com.ourbreak;

import com.ourbreak.ecs.systems.NpcBuilderSystem;
import com.ourbreak.ecs.systems.RoundSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The endless difficulty curve: an onboarding ramp through round 5, then an unbounded rise where the
 * wall keeps growing every round, increments never spike (fair), and the required clear-rate stays
 * below the asymptote so every wall is always theoretically survivable.
 */
class DifficultyCurveTest {

    private static final double RATE_MAX = 1.20; // mirrors NpcBuilderSystem.RATE_MAX

    @Test
    void onboardingRampIsTheClassicLinearGrowth() {
        assertEquals(16, NpcBuilderSystem.blocksForRound(1));
        assertEquals(24, NpcBuilderSystem.blocksForRound(2));
        assertEquals(32, NpcBuilderSystem.blocksForRound(3));
        assertEquals(40, NpcBuilderSystem.blocksForRound(4));
        // Continuity: round 5 still equals the old plateau value, so the new ramp grafts on seamlessly.
        assertEquals(48, NpcBuilderSystem.blocksForRound(5));
        assertEquals(60.0, RoundSystem.attackSecondsForRound(5), 1e-9);
    }

    @Test
    void wallGrowsEveryRoundForever() {
        for (int r = 1; r < 500; r++) {
            assertTrue(NpcBuilderSystem.blocksForRound(r + 1) > NpcBuilderSystem.blocksForRound(r),
                    "blocks must strictly increase at round " + (r + 1));
        }
    }

    @Test
    void attackTimeGrowsSlowlyPastRoundFive() {
        assertEquals(60.0, RoundSystem.attackSecondsForRound(1), 1e-9);
        assertTrue(RoundSystem.attackSecondsForRound(6) > 60.0);
        for (int r = 5; r < 500; r++) {
            assertTrue(RoundSystem.attackSecondsForRound(r + 1) >= RoundSystem.attackSecondsForRound(r));
        }
    }

    @Test
    void incrementsNeverSpike() {
        // Past the onboarding ramp each round adds at most the onboarding step (8) and always > 0,
        // so difficulty rises smoothly with no sudden brick wall.
        for (int r = 5; r < 500; r++) {
            int delta = NpcBuilderSystem.blocksForRound(r + 1) - NpcBuilderSystem.blocksForRound(r);
            assertTrue(delta >= 1 && delta <= 8, "round " + (r + 1) + " increment was " + delta);
        }
    }

    @Test
    void requiredClearRateStaysBelowTheAsymptote() {
        // blocks / time approaches but never meaningfully exceeds RATE_MAX (rounding tolerance of 1 block),
        // so the wall is always clearable in principle — you lose to your own skill, not an impossible wall.
        for (int r = 5; r < 500; r++) {
            int blocks = NpcBuilderSystem.blocksForRound(r);
            double time = RoundSystem.attackSecondsForRound(r);
            assertTrue(blocks <= RATE_MAX * time + 1.0,
                    "round " + r + " demanded rate " + (blocks / time));
        }
    }
}
