# Devlog — 2026-06-17 13:53:47 — `fx-pick-and-grid-fixes`

> **Author**: ceil
> **Build / Version**: feel — destruction FX fixes
> **Branch / Commit**: feat/destruction-juice

---

## Summary

Two fixes for issues the new destruction FX exposed: the crosshair pick being blocked by
debris/explosion geometry, and the ground grid flashing into view during a blast.

---

## What I worked on

### Pick raycast skips FX (`PlayerControlState`)

- `pickBlockUnderCrosshair` took only the *closest* collision. The FX geometry (flying debris,
  fireball, smoke, shockwave ring) lives under the root node, so right after a blast it sat in front
  of the wall and the pick hit it instead of a block → returned no target for ~1s, so you couldn't
  re-bomb the same spot. Now it walks all collisions nearest-first and returns the first that resolves
  to a block entity, skipping FX / debris / ground / mascot.

### Remove the placement grid (`EnvironmentState`)

- The faint Unshaded sand-tinted grid was carefully tuned to be near-invisible under the warm sun, but
  the explosion's PointLight brightens the *lit* ground while the *unlit* grid keeps its fixed colour —
  so the whole square grid popped into view around each blast. The grid was only a placeholder
  alignment aid, so it's removed (and its Grid / RenderState / RenderQueue imports + constants).

## Next session

- [ ] Drone upgrade: blast radius grows with round (Lv = 1 + (round-1)/3, uncapped), bigger explosion
  FX, and a "Lv.N" readout on the weapon HUD when the drone is equipped.
