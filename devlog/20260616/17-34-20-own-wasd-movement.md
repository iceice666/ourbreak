# Devlog — 2026-06-16 17:34:20 — `own-wasd-movement`

> **Author**: ceil
> **Build / Version**: M4–M5 playable effects, controls
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

WASD movement is now handled in `PlayerControlState` instead of the fly-cam, fixing
movement on the native Windows build (where the fly-cam's WASD didn't fire).

---

## What I worked on

- The native Windows distribution couldn't move with WASD: movement was delegated to
  jME's FlyByCamera, which behaved inconsistently across platforms (worked under WSLg,
  dead on the native captured-cursor build). Rather than chase the fly-cam quirk, moved
  WASD into our own code so both platforms behave identically.
- `onEnable` now strips the fly-cam's movement bindings (STRAFELEFT/RIGHT, FORWARD/
  BACKWARD, RISE/LOWER) on both platforms; the fly-cam is only kept for the native
  captured-cursor look. WASD are mapped to KEY_W/S/A/D; `onAction` tracks press+release
  into held-state flags.
- `updateMovement(tpf, factor)` moves the camera on the horizontal plane along its
  facing (forward/left flattened to Y=0), at `BASE_MOVE_SPEED * coralFactor * tpf` — so
  the Coral slow still applies and the player stays at a fixed height.

### Verified

- Built on WSL, ran the distribution on native Windows via a portable Temurin 21 JDK
  (only Java 17 was installed on Windows) — boots clean on the RTX 5070 (OpenGL 3.2
  NVIDIA), no exceptions.

## Next session

- [ ] Beach floor: make the ground feel like sand (texture + tone) — to be planned.
