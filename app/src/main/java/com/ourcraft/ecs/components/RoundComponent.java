package com.ourcraft.ecs.components;

import com.simsilica.es.EntityComponent;

public record RoundComponent(int currentRound, int maxRounds, double remainingSeconds) implements EntityComponent {

    public RoundComponent {
        if (maxRounds <= 0) {
            throw new IllegalArgumentException("maxRounds must be positive");
        }
        if (currentRound < 1 || currentRound > maxRounds) {
            throw new IllegalArgumentException("currentRound must be between 1 and maxRounds");
        }
        if (!Double.isFinite(remainingSeconds) || remainingSeconds < 0.0) {
            throw new IllegalArgumentException("remainingSeconds must be finite and nonnegative");
        }
    }
}
