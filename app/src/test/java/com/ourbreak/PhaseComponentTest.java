package com.ourbreak;

import com.ourbreak.ecs.components.PhaseComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PhaseComponentTest {

    @Test
    void nullPhaseIsRejected() {
        assertThrows(NullPointerException.class, () -> new PhaseComponent(null));
    }
}
