package com.ourcraft.ecs.components;

import com.simsilica.es.EntityComponent;

/**
 * Player hit points reduced by Shell reflect damage. Decoupled from win/loss — per the GDD the match
 * is decided by buildings and the round timer, so depleting this does not end the game.
 */
public record PlayerHealthComponent(float current, float max) implements EntityComponent {

    public PlayerHealthComponent {
        if (!Float.isFinite(max) || max <= 0.0f) {
            throw new IllegalArgumentException("max must be finite and positive");
        }
        if (!Float.isFinite(current) || current < 0.0f) {
            throw new IllegalArgumentException("current must be finite and nonnegative");
        }
        if (current > max) {
            throw new IllegalArgumentException("current cannot exceed max");
        }
    }

    public PlayerHealthComponent(float max) {
        this(max, max);
    }

    public PlayerHealthComponent applyDamage(float amount) {
        if (!Float.isFinite(amount) || amount < 0.0f) {
            throw new IllegalArgumentException("amount must be finite and nonnegative");
        }
        return new PlayerHealthComponent(Math.max(0.0f, current - amount), max);
    }
}
