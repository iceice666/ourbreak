# Devlog — 2026-06-17 20:23:00 — `held-weapon-viewmodel`

> **Author**: ceil
> **Build / Version**: feel — first-person viewmodel
> **Branch / Commit**: feat/destruction-juice

---

## Summary

A first-person held-weapon viewmodel: the equipped sword / gun / drone is shown as a procedural 3D
model in the player's hand, bobbing while you move and animating when you attack. Cosmetic only.

---

## What I worked on

- **Procedural weapon models** (`WeaponModels`): low-poly sword (steel blade + fuller + diamond tip,
  gold guard/pommel, maroon grip), pistol (gold receiver + round barrel + maroon pump/grip/guard) and
  quad-rotor drone (red body, glowing blue eye, four spinning rotors, landing legs) — built from jME
  primitives to match the icon art, the same way the mascot crab is built. Each is pre-posed for the
  hand.
- **Viewmodel viewport** (`HeldWeaponState`): a dedicated main view with a static camera looking down
  -Z that clears depth only, so the weapon is locked to the screen and never clips into walls. Its own
  key + ambient lights (lights don't cross viewports). The scene is advanced manually
  (`updateGeometricState`) since it isn't under the app rootNode.
- **Animation**: figure-of-eight walk-bob (wider/faster while the camera moves), and an attack swing —
  sword/drone chop down-and-forward, the gun recoils muzzle-up and punches back toward the eye. Drone
  rotors whir continuously. Overall scale `0.78`.
- **Wiring**: `GameplayState` attaches/detaches the state with the match; `PlayerControlState` fires
  `swing()` on every left-click (even a miss, like Minecraft).

## Why

The player wanted the weapons to feel held — like Minecraft showing the tool/block in hand. A first
attempt with a 2D icon sprite looked wrong directionally, so this builds real 3D models instead.

## Tests / docs

- Cosmetic; no headless logic. `./gradlew test` green. Verified clean launch on the Windows build.

## Watch

- `BASE_POS` / `VIEWMODEL_SCALE` / per-weapon pose rotations are the tuning knobs if placement needs
  nudging.
