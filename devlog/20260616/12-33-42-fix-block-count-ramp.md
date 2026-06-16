# Devlog — 2026-06-16 12:33:42 — `fix-block-count-ramp`

> **Author**: ceil
> **Build / Version**: M7 balance follow-up
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Balance fix after playtest: the flat 8 blocks/round was trivially easy against
the 60 s timer (cleared in ~10 s). Replaced it with a per-round ramp — 16 / 24 /
32 / 40 for rounds 1–4 — so later rounds are a real time-pressured wall.

---

## Bugs fixed

| ID | Description | Cause | Fix |
|----|-------------|-------|-----|
| — | Every round trivially easy | 8 blocks vs a 60 s timer — I tuned weapon matchups but never checked the time budget | Per-round escalating block quota (16/24/32/40); `NpcBuilderSystem.blocksForRound(round)` |

---

## Technical notes

- `BLOCKS_PER_ROUND` (flat 8) → per-round constants `ROUND_1_BLOCKS`…`ROUND_4_BLOCKS`
  and a public `blocksForRound(int)`. `NpcBuilderTest` now derives quotas from it;
  the first-ring order tests place exactly the first ring (8) via `placeFirstRing()`.
- Numbers are first-pass — the *structure* (escalation) is the real fix; exact
  values still need playtest dial-in.

---

## Open questions / blockers

- [ ] Even with more blocks, the player can fly + clip through everything, so
  positioning has no cost. Grounded movement (no-fly + block collision) is the
  complementary fix that makes the volume actually bite — strongly recommended next.

---

## Next session

- [ ] Playtest the ramp; dial the per-round counts.
- [ ] grounded-movement (no-fly + AABB block collision) so back-rank blocks cost time.
