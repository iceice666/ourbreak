# Devlog — 2026-06-16 13:58:48 — `3d-stacked-wall`

> **Author**: ceil
> **Build / Version**: playtest polish
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Blocks used to sit in one flat layer at the mascot's Y — the "wall" was a 1-tall
carpet. Made the NPC build a 3D concentric castle wall: fill a ring, stack it up
to `WALL_HEIGHT` (3), then expand outward.

---

## What I worked on

- `NpcBuilderSystem.findFirstAvailablePosition`: candidate order is now ring
  radius → Y layer (mascot's Y upward, 0..WALL_HEIGHT-1) → within-ring order, so
  each ring is filled and stacked before the wall widens. `WALL_HEIGHT = 3`.
- The mascot is now enclosed by a wall with real height and thickness that grows
  with the round's block count.

---

## Technical notes

- The first ring's ground layer (8 cells) is unchanged, so the placement-order
  test still holds; the "fully occupied first ring" test now expects the wall to
  stack up (front-centre at y=1) instead of jumping to the second ring.
- npc-building spec + GDD updated to describe the 3D wall.

---

## Next session

- [ ] Co-design a balance pass over the block mechanics (player request).
- [ ] Tune WALL_HEIGHT if 3 reads too short/tall in play.
