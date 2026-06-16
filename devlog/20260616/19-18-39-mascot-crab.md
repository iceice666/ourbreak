# Devlog — 2026-06-16 19:18:39 — `mascot-crab`

> **Author**: ceil
> **Build / Version**: M8 art (mascot)
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

The mascot the NPC walls off is now visible: a cute cartoon crab built from primitives,
and it scuttles away in a panic once the player breaks a line of sight to it.

---

## What I worked on

### The crab (`MascotState`)

- Programmer-art crab from jME primitives at the mascot's origin, facing the player's
  spawn: a big rounded chibi shell + belly, short stalks with big shiny kawaii eyes
  (white + pupil + sparkle), little antennae, rosy blush, a small smile, three legs per
  side, and two chunky claws on pivot nodes. Lit by the scene sun.
- Idle animation: gentle vertical bob + a slow claw wave so it feels alive.

### Flee-on-discovery

- During ATTACK the state casts a grid ray from the crab to the player every 0.25s; the
  moment nothing occludes it (the player has opened a hole through to the centre, or the
  wall is fully down) the crab **panics and bolts**: scuttles away from the player with
  frantic claw-flailing, a wobble, hopping, then shrinks and vanishes.
- Purely cosmetic — the round still advances by clearing the wall (co-designer's call).
  On the next round's BUILD the crab resets to the centre, full size, idling again.

### Wiring

- `GameplayState` constructs `MascotState(ed, gameStateId)` after the game-state entity
  exists and attaches/detaches it with the other gameplay states.

## Next session

- [ ] Stretch: a real 3D model / sprite, a little "poof" or dust puff on the vanish, and
  a scared squeak SFX on flee.
