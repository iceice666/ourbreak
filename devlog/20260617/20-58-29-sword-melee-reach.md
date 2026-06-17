# Devlog — 2026-06-17 20:58:29 — `sword-melee-reach`

> **Author**: ceil
> **Build / Version**: balance — weapon reach
> **Branch / Commit**: feat/destruction-juice

---

## Summary

The sword is now melee: it only breaks blocks within arm's reach, so you have to close the distance.
The gun and drone stay ranged.

---

## What I worked on

- **Per-weapon reach** (`PlayerControlState`): the crosshair pick had no distance limit, so the sword
  could destroy blocks from anywhere across the arena — there was no sense of distance. The pick now
  takes a `maxReach` and only counts the nearest block hit if it's within range. Sword = `4.5` units
  (melee), gun and drone = effectively unlimited (`200`). You still play the swing animation on a
  whiff; it just doesn't connect when out of range.

## Why

Player feedback: "距離感根本不存在 — 劍可以在超遠的地方揮." Melee reach restores spatial tension and
sharpens the weapon roles: the sword is a get-in-close tool (poison-free jellyfish clearing), while
the gun is ranged-precise and the drone is ranged-AoE.

## Tests / docs

- Input/feel change; headless logic untouched. `./gradlew test` green. Verified on Windows.

## Watch

- `SWORD_REACH = 4.5f` is the tuning knob if melee should feel longer/shorter.
