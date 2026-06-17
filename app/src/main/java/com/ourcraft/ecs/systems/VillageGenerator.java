package com.ourcraft.ecs.systems;

import com.ourcraft.ecs.components.BlockComponent.BlockType;
import com.ourcraft.ecs.components.PositionComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Procedural "crab village" generator. Instead of one concentric wall, the NPC raises a little village
 * of box-houses scattered around the mascot (the origin in village space): each house has walls, a
 * doorway and a flat roof. Most houses are <em>themed</em> — built almost entirely from one block type
 * (a sand house, a shell house, a rock house…) so a single weapon clears them — while some are
 * <em>mixed</em> (every block a different type from the round's palette), which are nastier because no
 * one weapon counters them. Block types are drawn from the round's palette, so the type progression
 * (and the counter-matrix) is preserved.
 *
 * <p>Pure and deterministic for a given seed (so it's unit-testable). The total block count is bounded
 * by {@code budget} (the difficulty curve's per-round block quota), so balance is unchanged — the same
 * number of blocks, shaped into a village. All cells are in village space centred on the origin; the
 * caller translates them onto the mascot.
 */
public final class VillageGenerator {

    /** Grid spacing between house plots (> max house footprint, so houses never touch). */
    private static final int PLOT_SPACING = 6;
    /** How far out plots may sit from the centre (enough anchors for any realistic budget). */
    private static final int PLOT_REACH = 42;
    /** Chance a house is "mixed" (when the palette has ≥2 types) rather than single-themed. */
    private static final double MIXED_CHANCE = 0.28;

    private VillageGenerator() {
    }

    /** One block of the village: a grid cell (village space) and its block type. */
    public record PlacedBlock(PositionComponent cell, BlockType type) {
    }

    public static List<PlacedBlock> generate(int budget, List<BlockType> palette, long seed) {
        Objects.requireNonNull(palette, "palette");
        if (budget <= 0 || palette.isEmpty()) {
            return List.of();
        }

        Random rng = new Random(seed);
        List<PlacedBlock> village = new ArrayList<>(budget);
        Set<PositionComponent> used = new HashSet<>();

        for (int[] anchor : anchorsByDistance()) {
            if (village.size() >= budget) {
                break;
            }
            for (PlacedBlock block : buildHouse(anchor[0], anchor[1], palette, rng)) {
                if (village.size() >= budget) {
                    break;
                }
                if (used.add(block.cell())) {
                    village.add(block);
                }
            }
        }
        return village;
    }

    /** House-plot anchors on a spaced grid (excluding the centre, which the crab keeps), nearest-first. */
    private static List<int[]> anchorsByDistance() {
        List<int[]> anchors = new ArrayList<>();
        for (int x = -PLOT_REACH; x <= PLOT_REACH; x += PLOT_SPACING) {
            for (int z = -PLOT_REACH; z <= PLOT_REACH; z += PLOT_SPACING) {
                if (x != 0 || z != 0) {
                    anchors.add(new int[]{x, z});
                }
            }
        }
        anchors.sort((a, b) -> {
            int da = a[0] * a[0] + a[1] * a[1];
            int db = b[0] * b[0] + b[1] * b[1];
            if (da != db) {
                return Integer.compare(da, db);
            }
            if (a[1] != b[1]) {
                return Integer.compare(b[1], a[1]); // prefer +z (toward the player) so a house blocks LOS
            }
            return Integer.compare(a[0], b[0]);
        });
        return anchors;
    }

    private static List<PlacedBlock> buildHouse(int ax, int az, List<BlockType> palette, Random rng) {
        boolean tower = rng.nextInt(4) == 0; // ~25% are tall towers
        int w;
        int d;
        int h;
        if (tower) {
            w = 2;
            d = 2;
            h = 4 + rng.nextInt(3); // 4..6
        } else {
            w = 2 + rng.nextInt(2); // 2..3
            d = 2 + rng.nextInt(2);
            h = 2 + rng.nextInt(2); // 2..3
        }

        boolean mixed = palette.size() >= 2 && rng.nextDouble() < MIXED_CHANCE;
        BlockType theme = mixed ? null : palette.get(rng.nextInt(palette.size()));
        int doorSide = rng.nextInt(4);

        List<PlacedBlock> blocks = new ArrayList<>();
        // Hollow walls.
        for (int y = 0; y < h; y++) {
            for (int dx = 0; dx < w; dx++) {
                for (int dz = 0; dz < d; dz++) {
                    boolean border = dx == 0 || dx == w - 1 || dz == 0 || dz == d - 1;
                    if (!border) {
                        continue;
                    }
                    if (y == 0 && isDoor(dx, dz, w, d, doorSide)) {
                        continue; // leave a doorway at ground level
                    }
                    blocks.add(cell(ax + dx, y, az + dz, theme, palette, rng));
                }
            }
        }
        // Flat roof.
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                blocks.add(cell(ax + dx, h, az + dz, theme, palette, rng));
            }
        }
        return blocks;
    }

    private static PlacedBlock cell(int x, int y, int z, BlockType theme, List<BlockType> palette, Random rng) {
        BlockType type = theme != null ? theme : palette.get(rng.nextInt(palette.size()));
        return new PlacedBlock(new PositionComponent(x, y, z), type);
    }

    private static boolean isDoor(int dx, int dz, int w, int d, int side) {
        return switch (side) {
            case 0 -> dz == 0 && dx == w / 2;
            case 1 -> dz == d - 1 && dx == w / 2;
            case 2 -> dx == 0 && dz == d / 2;
            default -> dx == w - 1 && dz == d / 2;
        };
    }
}
