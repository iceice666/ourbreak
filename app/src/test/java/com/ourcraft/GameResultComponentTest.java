package com.ourcraft;

import com.ourcraft.ecs.components.GameResultComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GameResultComponentTest {

    @Test
    void nullResultIsRejected() {
        assertThrows(NullPointerException.class, () -> new GameResultComponent(null));
    }
}
