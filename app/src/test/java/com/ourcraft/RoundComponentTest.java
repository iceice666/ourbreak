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
    void invalidRoundStateIsRejected(int currentRound, double remainingSeconds) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RoundComponent(currentRound, remainingSeconds));
    }

    @ParameterizedTest
    @MethodSource("validRoundStates")
    void validRoundStatesAreAccepted(int currentRound, double remainingSeconds) {
        RoundComponent round = new RoundComponent(currentRound, remainingSeconds);

        assertEquals(currentRound, round.currentRound());
        assertEquals(remainingSeconds, round.remainingSeconds());
    }

    private static Stream<Arguments> invalidRoundStates() {
        return Stream.of(
                Arguments.of(0, 60.0),
                Arguments.of(-1, 60.0),
                Arguments.of(1, -1.0),
                Arguments.of(1, Double.NaN),
                Arguments.of(1, Double.POSITIVE_INFINITY),
                Arguments.of(1, Double.NEGATIVE_INFINITY));
    }

    private static Stream<Arguments> validRoundStates() {
        return Stream.of(
                Arguments.of(1, 60.0),
                Arguments.of(4, 0.0),
                Arguments.of(99, 12.5)); // unbounded — high rounds are valid
    }
}
