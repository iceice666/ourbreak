package com.ourcraft;

import com.jme3.math.Vector3f;
import com.ourcraft.ecs.components.PositionComponent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionComponentTest {

    @ParameterizedTest
    @MethodSource("nonFinitePositions")
    void nonFiniteCoordinatesAreRejected(float x, float y, float z) {
        assertThrows(IllegalArgumentException.class, () -> new PositionComponent(x, y, z));
    }

    @ParameterizedTest
    @MethodSource("finiteVectors")
    void vectorConversionRoundTrips(Vector3f vector) {
        assertEquals(vector, PositionComponent.of(vector).toVector3f());
    }

    private static Stream<Arguments> nonFinitePositions() {
        return Stream.of(
                Arguments.of(Float.NaN, 0.0f, 0.0f),
                Arguments.of(Float.POSITIVE_INFINITY, 0.0f, 0.0f),
                Arguments.of(Float.NEGATIVE_INFINITY, 0.0f, 0.0f),
                Arguments.of(0.0f, Float.NaN, 0.0f),
                Arguments.of(0.0f, Float.POSITIVE_INFINITY, 0.0f),
                Arguments.of(0.0f, Float.NEGATIVE_INFINITY, 0.0f),
                Arguments.of(0.0f, 0.0f, Float.NaN),
                Arguments.of(0.0f, 0.0f, Float.POSITIVE_INFINITY),
                Arguments.of(0.0f, 0.0f, Float.NEGATIVE_INFINITY));
    }

    private static Stream<Vector3f> finiteVectors() {
        return Stream.of(
                Vector3f.ZERO,
                new Vector3f(1.25f, -2.5f, 3.75f),
                new Vector3f(Float.MAX_VALUE, -Float.MAX_VALUE, Float.MIN_VALUE));
    }
}
