# Devlog — 2026-06-17 15:28:49 — `gabled-roofs-hollow-houses`

> **Author**: ceil
> **Build / Version**: worldgen — village readability (Phase 1.1)
> **Branch / Commit**: feat/destruction-juice

---

## Summary

Village houses now read as houses: A-frame gabled roofs, a 2-tall doorway, and a 3×3
minimum so they're hollow instead of solid blocks.

---

## What I worked on

- **Gabled (A-frame) roof** replaces the flat cap: a ridge along the longer axis sloping down
  to the eaves on both sides, so houses have a peaked silhouette (the visual signature of a
  house) instead of looking like boxes.
- **Minimum 3×3 footprint** (towers too): a 2×2 footprint has no interior cell, so its walls
  filled every cell — a solid block that wasted budget and didn't read as a house. 3×3+ is
  hollow with a recognizable wall ring.
- **2-tall doorway** so the entrance reads as a door.

Exact-budget generation is unchanged, so balance and all tests are untouched.

## Known limitation

- Still strict-budget, so early rounds whose block budget is smaller than one full house
  (R1 16, R2 24 vs a ~23+ block gabled cottage) show a partial house; complete gabled houses
  appear reliably from ~R3. Optional follow-up (B2): build whole houses (round the budget up to
  finish the last house) so even R1 shows one complete house.

## Next (Phase 2)

- [ ] Big houses, wells, windows, layout tuning; optional B2 for early-round completeness.
