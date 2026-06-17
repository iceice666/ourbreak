# Devlog — 2026-06-17 13:38:56 — `drone-explosion-fx`

> **Author**: ceil
> **Build / Version**: feel — "Teardown-ify" pass 1
> **Branch / Commit**: feat/destruction-juice

---

## Summary

The drone attack now detonates a proper explosion at the blast centre.

---

## What I worked on

- `DestructionFxState.explosion(center)`: a layered blast built from a generic expanding/rising/
  fading "puff" plus a fading point light — a white core flash, an orange fireball (additive glow),
  five rising smoke puffs, and an expanding ground shockwave ring (Torus), plus a brief orange
  PointLight that lights up the surrounding debris/blocks. Transparent FX use additive/alpha blend
  with depth-write off and live in the Transparent bucket; all auto-recycled by lifetime.
- `PlayerControlState.attack()`: on a DRONE hit, captures the crosshair block's position before the
  blocks are destroyed and calls `DestructionFxState.explosion(...)` there (looked up via the state
  manager, no constructor change). Combined with the 3×3 textured debris, the drone reads as a real
  bomb.

## Known issue (follow-up)

- The FX geometries (debris/fireball/smoke/ring) are children of the root node, so the crosshair
  pick raycast (`pickBlockUnderCrosshair` → `getClosestCollision`) can hit them instead of a block
  for ~0.4–1s after a blast — making it feel like you can't re-bomb the exact same spot. Fix: have
  the pick walk all collisions nearest-first and return the first one resolving to a block entity
  (also fixes ground/mascot interception). Not yet applied.

## Next session

- [ ] Apply the pick-raycast fix above.
- [ ] Optional screen shake on the blast; material-specific break SFX; ② grid structural collapse.
