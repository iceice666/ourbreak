package com.ourbreak;

import com.ourbreak.ecs.components.WeaponComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class WeaponComponentTest {

    @Test
    void nullWeaponTypeIsRejected() {
        assertThrows(NullPointerException.class, () -> new WeaponComponent(null));
    }
}
