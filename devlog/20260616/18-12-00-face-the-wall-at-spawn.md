# Devlog — 2026-06-16 18:12:00 — `face-the-wall-at-spawn`

> **Author**: ceil
> **Build / Version**: M4–M5 playable effects, controls
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

The player now spawns facing the block wall instead of having it behind them.

---

## What I worked on

- `PlayerControlState.onEnable` now points the camera at the wall centre (the mascot at
  the origin) with `camera.lookAt(WALL_CENTRE, UNIT_Y)` on both platforms, instead of
  hard-setting `yaw = 0` (an absolute rotation that didn't reliably point at the wall
  given the look calibration, so the blocks ended up behind the player at spawn).
- For the WSLg delta-look, yaw/pitch are now seeded from that look-at orientation
  (`camera.getRotation().toAngles`) so the free-look continues smoothly from facing the
  wall — convention-proof, no magic spawn angle.

## Next session

- [ ] —
