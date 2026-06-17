package com.ourbreak;

import com.ourbreak.ecs.components.WeaponComponent.WeaponType;
import com.ourbreak.ecs.systems.HudText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HudTextTest {

    @Test
    void weaponText() {
        assertEquals("Weapon: SWORD", HudText.weapon(WeaponType.SWORD));
        assertEquals("Weapon: DRONE", HudText.weapon(WeaponType.DRONE));
    }

    @Test
    void roundText() {
        assertEquals("Round 1", HudText.round(1));
        assertEquals("Round 12", HudText.round(12));
    }

    @Test
    void countdownFormatsMinutesAndSeconds() {
        assertEquals("1:00", HudText.countdown(60.0));
        assertEquals("0:59", HudText.countdown(59.0));
        assertEquals("0:05", HudText.countdown(5.0));
    }

    @Test
    void countdownRoundsUp() {
        assertEquals("0:01", HudText.countdown(0.4));
        assertEquals("1:00", HudText.countdown(59.1));
    }

    @Test
    void countdownClampsAtZero() {
        assertEquals("0:00", HudText.countdown(0.0));
        assertEquals("0:00", HudText.countdown(-3.0));
    }

    @Test
    void buildingsText() {
        assertEquals("Buildings: 8", HudText.buildings(8));
        assertEquals("Buildings: 0", HudText.buildings(0));
    }
}
