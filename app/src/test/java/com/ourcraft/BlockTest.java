package com.ourcraft;

import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.BlockComponent.BlockType;
import org.junit.jupiter.api.Test;

import static com.ourcraft.ecs.components.BlockComponent.BlockType.CORAL;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.JELLYFISH;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.ROCK;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.SAND;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.SHELL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockTest {

    @Test
    void standardDurabilityForEveryBlockType() {
        assertEquals(1.0f, SAND.standardDurability());
        assertEquals(2.0f, CORAL.standardDurability());
        assertEquals(1.0f, SHELL.standardDurability());
        assertEquals(4.0f, ROCK.standardDurability());
        assertEquals(1.0f, JELLYFISH.standardDurability());
    }

    @Test
    void blockTypeConstructorCreatesFullHealthBlock() {
        for (BlockType blockType : BlockType.values()) {
            BlockComponent block = new BlockComponent(blockType);

            assertEquals(blockType, block.blockType());
            assertEquals(block.maxDurability(), block.durability());
            assertEquals(blockType.standardDurability(), block.maxDurability());
        }
    }

    @Test
    void nonlethalDamageReducesDurability() {
        BlockComponent block = new BlockComponent(ROCK);

        BlockComponent damaged = block.applyDamage(1.0f);

        assertEquals(3.0f, damaged.durability());
        assertEquals(4.0f, damaged.maxDurability());
    }

    @Test
    void overkillDamageClampsDurabilityToZero() {
        BlockComponent block = new BlockComponent(SAND);

        BlockComponent damaged = block.applyDamage(2.0f);

        assertEquals(0.0f, damaged.durability());
    }
}
