# Devlog — 2026-06-17 21:04:35 — `sync-design-docs`

> **Author**: ceil
> **Build / Version**: docs — design sync
> **Branch / Commit**: feat/destruction-juice

---

## Summary

Brought the README and design docs in line with everything this branch shipped: the procedural
crab village, sphere/levelling drone, sword melee reach, the lighting pass and the held-weapon
viewmodel.

---

## What I worked on

- **README**: NPC builds a *crab village* (not a flat wall); sword is melee, gun/drone ranged; drone
  is a growing 3D sphere (Lv on HUD). Expanded the systems mermaid with `VillageGenerator`,
  `BlockEffectSystem`, `DestructionFxState`, `HeldWeaponState`, `RoundBannerState`.
- **gdd.md**: wall → `VillageGenerator` village; sword reach 2 → 4.5 (melee) and a ranged note for
  gun/drone; added destruction FX, drone explosion, held viewmodel and the sun/sky/god-rays/shadows
  lighting to Special Effects; added round-clear banner + persistent high score to the UI section.
- **tdd.md**: added the new systems (`VillageGenerator`, `DestructionFxState`, `HeldWeaponState`,
  `RoundBannerState`, `MascotState`, env/audio/lighting) to §3.2; per-weapon reach in §6; rewrote §7
  NPC AI around the seeded village generator; updated §9 UI (viewmodel, banner, Best/NEW BEST,
  HowToPlay) and added `VillageGeneratorTest` to §10; added HowToPlay to the AppState diagram.
- **art_style.md**: §9 lighting now documents the implemented dynamic shadows + sun + sky + god rays
  + bloom + vignette (replacing the old "no dynamic shadows / blob shadow only" note); §5 notes the
  procedural 3D weapon viewmodel follows the icon art.

## Why

The branch changed core behavior (worldgen, weapon reach, drone shape) and added a lot of
presentation; the docs still described the old concentric wall, 3×3 drone and shadow-less lighting.

## Tests / docs

- Docs only. `./gradlew test` still green (hook gate).

## Notes

- `milestones.md` left as-is — it's the historical M0–M8 roadmap; this work is M8-stretch polish.
