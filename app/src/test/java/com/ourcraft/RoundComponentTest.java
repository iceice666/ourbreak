package com.ourcraft;

import com.ourcraft.ecs.components.RoundComponent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoundComponentTest {

    @ParameterizedTest
    @MethodSource("invalidRoundStates")
    void invalidRoundStateIsRejected(int currentRound, int maxRounds, double remainingSeconds) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RoundComponent(currentRound, maxRounds, remainingSeconds));
    }

    @ParameterizedTest
    @MethodSource("validRoundBoundaries")
    void validRoundBoundariesAreAccepted(int currentRound, int maxRounds, double remainingSeconds) {
        RoundComponent round = new RoundComponent(currentRound, maxRounds, remainingSeconds);

        assertEquals(currentRound, round.currentRound());
        assertEquals(maxRounds, round.maxRounds());
        assertEquals(remainingSeconds, round.remainingSeconds());
    }

    private static Stream<Arguments> invalidRoundStates() {
        return Stream.of(
                Arguments.of(1, 0, 60.0),
                Arguments.of(1, -1, 60.0),
                Arguments.of(0, 4, 60.0),
                Arguments.of(-1, 4, 60.0),
                Arguments.of(5, 4, 60.0),
                Arguments.of(1, 4, -1.0),
                Arguments.of(1, 4, Double.NaN),
                Arguments.of(1, 4, Double.POSITIVE_INFINITY),
                Arguments.of(1, 4, Double.NEGATIVE_INFINITY));
    }

    private static Stream<Arguments> validRoundBoundaries() {
        return Stream.of(
                Arguments.of(1, 4, 60.0),
                Arguments.of(4, 4, 0.0));
    }
}
