package com.ourcraft.ecs.components;

import com.simsilica.es.EntityComponent;

import java.util.Objects;

public record GameResultComponent(Result result) implements EntityComponent {
    public GameResultComponent {
        Objects.requireNonNull(result, "result");
    }

    public enum Result { IN_PROGRESS, WIN, LOSS }
}
