package com.ourbreak;

import com.ourbreak.ecs.components.ModelComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelComponentTest {

    @Test
    void nullModelIdIsRejected() {
        assertThrows(NullPointerException.class, () -> new ModelComponent(null));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void blankModelIdIsRejected(String modelId) {
        assertThrows(IllegalArgumentException.class, () -> new ModelComponent(modelId));
    }
}
