package com.ourbreak;

import com.ourbreak.ecs.components.GameResultComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GameResultComponentTest {

    @Test
    void nullResultIsRejected() {
        assertThrows(NullPointerException.class, () -> new GameResultComponent(null));
    }
}
