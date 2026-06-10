package com.ourcraft.ecs.components;

import com.simsilica.es.EntityComponent;

import java.util.Objects;

public record BlockComponent(
        BlockType blockType,
        float durability,
        float maxDurability
) implements EntityComponent {

    public BlockComponent {
        Objects.requireNonNull(blockType, "blockType");
        if (!Float.isFinite(durability) || durability < 0.0f) {
            throw new IllegalArgumentException("durability must be finite and nonnegative");
        }
        if (!Float.isFinite(maxDurability) || maxDurability <= 0.0f) {
            throw new IllegalArgumentException("maxDurability must be finite and positive");
        }
        if (durability > maxDurability) {
            throw new IllegalArgumentException("durability cannot exceed maxDurability");
        }
    }

    public BlockComponent(BlockType blockType) {
        this(blockType, blockType.standardDurability(), blockType.standardDurability());
    }

    public BlockComponent applyDamage(float damage) {
        if (!Float.isFinite(damage) || damage < 0.0f) {
            throw new IllegalArgumentException("damage must be finite and nonnegative");
        }
        return new BlockComponent(blockType, Math.max(0.0f, durability - damage), maxDurability);
    }

    public enum BlockType {
        SAND(1.0f),
        CORAL(2.0f),
        SHELL(1.0f),
        ROCK(4.0f),
        JELLYFISH(1.0f);

        private final float standardDurability;

        BlockType(float standardDurability) {
            this.standardDurability = standardDurability;
        }

        public float standardDurability() {
            return standardDurability;
        }
    }
}
