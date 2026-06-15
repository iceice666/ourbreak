package com.ourcraft;

import com.ourcraft.ecs.components.EffectComponent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.ourcraft.ecs.components.BlockComponent.BlockType.CORAL;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.JELLYFISH;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.ROCK;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.SAND;
import static com.ourcraft.ecs.components.BlockComponent.BlockType.SHELL;
import static com.ourcraft.ecs.components.EffectComponent.EffectType.FLICKER;
import static com.ourcraft.ecs.components.EffectComponent.EffectType.REFLECT;
import static com.ourcraft.ecs.components.EffectComponent.EffectType.SLOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectComponentTest {

    @Test
    void nullEffectTypeIsRejected() {
        assertThrows(NullPointerException.class, () -> new EffectComponent(null));
    }

    @Test
    void effectBearingBlocksMapToTheirEffect() {
        assertEquals(Optional.of(new EffectComponent(SLOW)), EffectComponent.forBlockType(CORAL));
        assertEquals(Optional.of(new EffectComponent(REFLECT)), EffectComponent.forBlockType(SHELL));
        assertEquals(Optional.of(new EffectComponent(FLICKER)), EffectComponent.forBlockType(JELLYFISH));
    }

    @Test
    void plainBlocksHaveNoEffect() {
        assertTrue(EffectComponent.forBlockType(SAND).isEmpty());
        assertTrue(EffectComponent.forBlockType(ROCK).isEmpty());
    }
}
