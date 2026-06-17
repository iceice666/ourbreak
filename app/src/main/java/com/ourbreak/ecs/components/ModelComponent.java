package com.ourbreak.ecs.components;

import com.simsilica.es.EntityComponent;

import java.util.Objects;

public record ModelComponent(String modelId) implements EntityComponent {
    public ModelComponent {
        Objects.requireNonNull(modelId, "modelId");
        if (modelId.isBlank()) {
            throw new IllegalArgumentException("modelId must not be blank");
        }
    }
}
