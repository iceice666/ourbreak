package com.ourbreak.ecs.components;

import com.simsilica.es.EntityComponent;

import java.util.Objects;

public record PhaseComponent(Phase phase) implements EntityComponent {
    public PhaseComponent {
        Objects.requireNonNull(phase, "phase");
    }

    public enum Phase { BUILD, ATTACK }
}
