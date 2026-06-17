package com.ourbreak;

import com.ourbreak.ecs.systems.WeaponSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The drone's blast level climbs one ring every 3 rounds, uncapped. */
class DroneLevelTest {

    @Test
    void levelStepsUpEveryThreeRounds() {
        assertEquals(1, WeaponSystem.droneLevelForRound(1));
        assertEquals(1, WeaponSystem.droneLevelForRound(3));
        assertEquals(2, WeaponSystem.droneLevelForRound(4));
        assertEquals(2, WeaponSystem.droneLevelForRound(6));
        assertEquals(3, WeaponSystem.droneLevelForRound(7));
        assertEquals(4, WeaponSystem.droneLevelForRound(10));
    }

    @Test
    void levelIsUncappedAndStrictlyClimbing() {
        assertTrue(WeaponSystem.droneLevelForRound(100) > WeaponSystem.droneLevelForRound(50));
        for (int r = 1; r < 300; r++) {
            assertTrue(WeaponSystem.droneLevelForRound(r + 1) >= WeaponSystem.droneLevelForRound(r));
        }
    }

    @Test
    void rejectsRoundsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> WeaponSystem.droneLevelForRound(0));
    }
}
