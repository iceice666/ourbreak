# Devlog — 2026-06-17 15:19:24 — `procedural-village-phase1`

> **Author**: ceil
> **Build / Version**: worldgen — procedural village (Phase 1)
> **Branch / Commit**: feat/destruction-juice

---

## Summary

The NPC now raises a procedural "crab village" of scattered themed houses around the mascot,
replacing the single concentric wall.

---

## What I worked on

### `VillageGenerator` (pure, tested)

- Generates the whole village for a round from `(budget, palette, seed)`: house plots on a spaced
  grid around the centre (nearest-first, +z preferred so a house blocks the player↔crab line), each
  plot a box-house (walls + doorway + flat roof). Towers (~25%) are tall and thin.
- **Themed vs mixed**: most houses are a single block type (sand house / shell house / rock house …
  → one weapon clears them); ~28% are "mixed" (every block a random palette type → no single
  counter, nastier). Types come from the round's palette, so the type progression / counter-matrix
  is preserved.
- Deterministic per seed; total blocks bounded by the difficulty-curve budget (balance unchanged —
  same block count, shaped into a village).

### Integration

- `NpcBuilderSystem` generates a fresh village each round (seed = base ^ round) and places one block
  per frame onto the mascot (so it visibly rises), then enters ATTACK. Removed the old concentric
  ring placement (`findFirstAvailablePosition` / `ringOffsets` / `GridOffset` / the blocks EntitySet).
- Player spawn moved back (z 8 → 13) so they no longer spawn inside the nearest house.

### Tests

- `VillageGeneratorTest` (budget filled exactly, palette-only, single-type = pure theme, deterministic,
  centre kept clear, builds up from ground). `NpcBuilderTest` rewritten: dropped the concentric
  position/order/round-robin assertions, kept the build lifecycle, and assert placed types ⊆ the
  round's palette.

## Next (Phase 1.1 — readability)

- [ ] Gabled (A-frame) roofs + min 3×3 footprint so houses read as houses (flat roofs look boxy; 2×2
  huts have no interior → effectively solid, wasting budget). Guarantee ≥1 complete house even at the
  small early-round budgets.
- [ ] Phase 2: big houses, wells, windows, layout tuning.
