package com.ourbreak;

import com.ourbreak.ecs.components.PositionComponent;
import com.ourbreak.ecs.systems.CoralGrowthSystem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Coral regrowth resolver: a living Coral heals one adjacent empty footprint cell per tick, the new
 * Coral stays inside the original footprint, surrounded Coral does nothing, and two Corals can't both
 * grow into the same cell.
 */
class CoralGrowthTest {

    private static PositionComponent cell(int x, int y, int z) {
        return new PositionComponent(x, y, z);
    }

    @Test
    void growsIntoAnAdjacentEmptyFootprintCell() {
        PositionComponent coral = cell(0, 0, 0);
        PositionComponent hole = cell(1, 0, 0);
        Set<PositionComponent> footprint = Set.of(coral, hole);
        Set<PositionComponent> occupied = Set.of(coral); // the player cleared the hole

        List<PositionComponent> targets =
                CoralGrowthSystem.growthTargets(List.of(coral), occupied, footprint);

        assertEquals(List.of(hole), targets);
    }

    @Test
    void surroundedCoralDoesNotGrow() {
        PositionComponent coral = cell(0, 0, 0);
        // every face neighbour is occupied → no empty cell to heal
        Set<PositionComponent> occupied = Set.of(
                coral,
                cell(-1, 0, 0), cell(1, 0, 0),
                cell(0, -1, 0), cell(0, 1, 0),
                cell(0, 0, -1), cell(0, 0, 1));
        Set<PositionComponent> footprint = occupied;

        assertTrue(CoralGrowthSystem.growthTargets(List.of(coral), occupied, footprint).isEmpty());
    }

    @Test
    void doesNotGrowOutsideTheFootprint() {
        PositionComponent coral = cell(0, 0, 0);
        // the only empty adjacent cell is NOT part of the original wall → coral can't expand the wall
        Set<PositionComponent> footprint = Set.of(coral);
        Set<PositionComponent> occupied = Set.of(coral);

        assertTrue(CoralGrowthSystem.growthTargets(List.of(coral), occupied, footprint).isEmpty());
    }

    @Test
    void twoCoralsCannotGrowIntoTheSameCell() {
        PositionComponent left = cell(0, 0, 0);
        PositionComponent right = cell(2, 0, 0);
        PositionComponent shared = cell(1, 0, 0); // adjacent to both
        Set<PositionComponent> footprint = Set.of(left, right, shared);
        Set<PositionComponent> occupied = Set.of(left, right);

        List<PositionComponent> targets =
                CoralGrowthSystem.growthTargets(List.of(left, right), occupied, footprint);

        assertEquals(1, targets.size());
        assertEquals(shared, targets.get(0));
    }
}
