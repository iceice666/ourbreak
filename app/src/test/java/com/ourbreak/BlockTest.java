package com.ourbreak;

import com.ourbreak.ecs.components.BlockComponent;
import com.ourbreak.ecs.components.BlockComponent.BlockType;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.ourbreak.ecs.components.BlockComponent.BlockType.CORAL;
import static com.ourbreak.ecs.components.BlockComponent.BlockType.JELLYFISH;
import static com.ourbreak.ecs.components.BlockComponent.BlockType.ROCK;
import static com.ourbreak.ecs.components.BlockComponent.BlockType.SAND;
import static com.ourbreak.ecs.components.BlockComponent.BlockType.SHELL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void nullBlockTypeIsRejected() {
        assertThrows(NullPointerException.class, () -> new BlockComponent(null, 0.0f, 1.0f));
    }

    @ParameterizedTest
    @MethodSource("invalidDurabilityStates")
    void invalidDurabilityStateIsRejected(float durability, float maxDurability) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BlockComponent(SAND, durability, maxDurability));
    }

    @ParameterizedTest
    @ValueSource(floats = {-1.0f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    void invalidDamageIsRejected(float damage) {
        BlockComponent block = new BlockComponent(ROCK);

        assertThrows(IllegalArgumentException.class, () -> block.applyDamage(damage));
        assertEquals(new BlockComponent(ROCK), block);
    }

    @Test
    void zeroDamagePreservesBlockState() {
        BlockComponent block = new BlockComponent(CORAL, 1.0f, 2.0f);

        assertEquals(block, block.applyDamage(0.0f));
    }

    @ParameterizedTest
    @MethodSource("validDurabilityBoundaries")
    void durabilityBoundariesAreAccepted(float durability, float maxDurability) {
        BlockComponent block = new BlockComponent(SAND, durability, maxDurability);

        assertEquals(durability, block.durability());
        assertEquals(maxDurability, block.maxDurability());
    }

    private static Stream<Arguments> invalidDurabilityStates() {
        return Stream.of(
                Arguments.of(Named.of("negative durability", -1.0f), 1.0f),
                Arguments.of(Named.of("NaN durability", Float.NaN), 1.0f),
                Arguments.of(Named.of("positive infinite durability", Float.POSITIVE_INFINITY), 1.0f),
                Arguments.of(Named.of("negative infinite durability", Float.NEGATIVE_INFINITY), 1.0f),
                Arguments.of(Named.of("durability above maximum", 2.0f), 1.0f),
                Arguments.of(0.0f, Named.of("zero maximum", 0.0f)),
                Arguments.of(0.0f, Named.of("negative maximum", -1.0f)),
                Arguments.of(0.0f, Named.of("NaN maximum", Float.NaN)),
                Arguments.of(0.0f, Named.of("positive infinite maximum", Float.POSITIVE_INFINITY)),
                Arguments.of(0.0f, Named.of("negative infinite maximum", Float.NEGATIVE_INFINITY)));
    }

    private static Stream<Arguments> validDurabilityBoundaries() {
        return Stream.of(
                Arguments.of(0.0f, 1.0f),
                Arguments.of(1.0f, 1.0f));
    }
}
