package com.ourbreak;

import com.ourbreak.ecs.components.BlockComponent.BlockType;
import com.ourbreak.ecs.components.PositionComponent;
import com.ourbreak.ecs.systems.VillageGenerator;
import com.ourbreak.ecs.systems.VillageGenerator.PlacedBlock;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The procedural crab-village generator: budget-bounded, palette-constrained, deterministic, centred. */
class VillageGeneratorTest {

    private static final List<BlockType> PALETTE = List.of(BlockType.SAND, BlockType.CORAL, BlockType.ROCK);

    @Test
    void fillsExactlyTheBlockBudget() {
        assertEquals(50, VillageGenerator.generate(50, PALETTE, 1L).size());
        assertEquals(120, VillageGenerator.generate(120, PALETTE, 2L).size());
        assertTrue(VillageGenerator.generate(0, PALETTE, 1L).isEmpty());
    }

    @Test
    void usesOnlyPaletteTypes() {
        for (PlacedBlock b : VillageGenerator.generate(80, PALETTE, 3L)) {
            assertTrue(PALETTE.contains(b.type()), () -> b.type() + " not in palette");
        }
    }

    @Test
    void aSingleTypePaletteMakesPurelyThemedHouses() {
        for (PlacedBlock b : VillageGenerator.generate(60, List.of(BlockType.SHELL), 4L)) {
            assertEquals(BlockType.SHELL, b.type());
        }
    }

    @Test
    void isDeterministicForASeed() {
        assertEquals(
                VillageGenerator.generate(70, PALETTE, 9L),
                VillageGenerator.generate(70, PALETTE, 9L));
    }

    @Test
    void keepsTheCentrePlazaAndBuildsUpFromTheGround() {
        for (PlacedBlock b : VillageGenerator.generate(90, PALETTE, 5L)) {
            assertFalse(b.cell().equals(new PositionComponent(0, 0, 0)), "village must not bury the crab");
            assertTrue(b.cell().y() >= 0f, "houses build up from the ground");
        }
    }
}
