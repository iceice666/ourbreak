package com.ourbreak.ecs.components;

import com.ourbreak.ecs.components.BlockComponent.BlockType;
import com.simsilica.es.EntityComponent;

import java.util.Objects;
import java.util.Optional;

/** Marker placed on effect-bearing block entities; the effect kind is derived from the block type. */
public record EffectComponent(EffectType effectType) implements EntityComponent {

    public EffectComponent {
        Objects.requireNonNull(effectType, "effectType");
    }

    public enum EffectType {
        SLOW,
        REFLECT,
        FLICKER
    }

    /** Coral→SLOW, Shell→REFLECT, Jellyfish→FLICKER; Sand and Rock have no effect. */
    public static Optional<EffectComponent> forBlockType(BlockType blockType) {
        Objects.requireNonNull(blockType, "blockType");
        return switch (blockType) {
            case CORAL -> Optional.of(new EffectComponent(EffectType.SLOW));
            case SHELL -> Optional.of(new EffectComponent(EffectType.REFLECT));
            case JELLYFISH -> Optional.of(new EffectComponent(EffectType.FLICKER));
            case SAND, ROCK -> Optional.empty();
        };
    }
}
