# Devlog — 2026-06-17 13:21:44 — `destruction-debris-juice`

> **Author**: ceil
> **Build / Version**: feel — "Teardown-ify" pass 1
> **Branch / Commit**: feat/destruction-juice

---

## Summary

Destroying a block now bursts it into textured debris + a dust puff instead of just vanishing
— the first step toward Teardown-grade destruction feel.

---

## What I worked on

### Debris + dust (`DestructionFxState`)

- New `BaseAppState` that watches the block `EntitySet` for removals (tracking each live block's
  position + model id) and, on destruction, spawns a burst: **7 chunky debris cubes** with random
  outward+upward velocity, tumble (angular velocity), gravity, a ground bounce + friction, and a
  shrink-to-nothing fade; plus **3 expanding, fading dust puffs**.
- Debris use the **same textured material as the block** (sand chunks look like sand, etc.), so they
  read as real fragments — not flat-coloured cubes.
- Purely visual (jME geometries animated here, not ECS entities); capped at 300 active debris so a
  Drone AoE clearing many blocks stays cheap.

### Shared block material (`ModelViewState`)

- Extracted `ModelViewState.blockMaterial(assetManager, modelId)` (texture-or-flat-colour Lighting
  material) and reused it for both the live blocks and the debris (DRY). `createSpatial` now reads the
  base Diffuse back from the material for its restore user-data.

### Wiring

- `GameplayState` attaches/detaches `DestructionFxState` alongside the other gameplay states.

## Next session

- [ ] ② Grid structural collapse (unsupported blocks fall when their support is destroyed) — the most
  Teardown-like change.
- [ ] Screen shake (needs decoupling player position from the camera so it doesn't drift movement) +
  material-specific break SFX.
