package com.ourcraft;

import com.ourcraft.ecs.components.PlayerHealthComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerHealthComponentTest {

    @Test
    void fullHealthConstructorSetsCurrentToMax() {
        PlayerHealthComponent health = new PlayerHealthComponent(10f);
        assertEquals(10f, health.current());
        assertEquals(10f, health.max());
    }

    @Test
    void invalidValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerHealthComponent(0f, 0f));
        assertThrows(IllegalArgumentException.class, () -> new PlayerHealthComponent(-1f, 10f));
        assertThrows(IllegalArgumentException.class, () -> new PlayerHealthComponent(11f, 10f));
    }

    @Test
    void applyDamageReducesCurrent() {
        PlayerHealthComponent health = new PlayerHealthComponent(10f).applyDamage(3f);
        assertEquals(7f, health.current());
        assertEquals(10f, health.max());
    }

    @Test
    void applyDamageClampsAtZero() {
        PlayerHealthComponent health = new PlayerHealthComponent(2f).applyDamage(5f);
        assertEquals(0f, health.current());
    }

    @Test
    void negativeDamageIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerHealthComponent(10f).applyDamage(-1f));
    }
}
