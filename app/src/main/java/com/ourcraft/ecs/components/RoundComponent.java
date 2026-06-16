package com.ourcraft.ecs.components;

import com.simsilica.es.EntityComponent;

/** Endless survival: the round number is unbounded — there is no maximum-round cap. */
public record RoundComponent(int currentRound, double remainingSeconds) implements EntityComponent {

    public RoundComponent {
        if (currentRound < 1) {
            throw new IllegalArgumentException("currentRound must be at least 1");
        }
        if (!Double.isFinite(remainingSeconds) || remainingSeconds < 0.0) {
            throw new IllegalArgumentException("remainingSeconds must be finite and nonnegative");
        }
    }
}
