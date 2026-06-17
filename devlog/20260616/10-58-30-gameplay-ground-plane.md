# Devlog — 2026-06-16 10:58:30 — `gameplay-ground-plane`

> **Author**: ceil
> **Build / Version**: post-M5 playtest polish
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Added a minimal gameplay ground — a flat floor plus a grid at the block base
level — so the scene has a spatial reference instead of floating placeholder
blocks. Planned and archived via OpenSpec (`gameplay-ground-plane`).

---

## What I worked on

### Feature / System: `scene-environment`

- `EnvironmentState` (`BaseAppState`) builds an environment node: a large `Quad`
  floor at y = -0.5 (the block base) with a lit sea-floor material, plus a
  `Grid` overlay just above it for motion/standing cues.
- `GameplayState` attaches it during world construction and detaches it on
  cleanup, so the ground shows only during a match and leaves nothing behind.

---

## Technical notes

- Found during a WSLg playtest: the build runs here (WSLg provides a display +
  GPU passthrough, falling back to llvmpipe software GL 4.5), so this and later
  visual changes were verified by actually launching the app, not just tests.
- Pure scene decoration — no logic, no components, no new deps; the headless
  suite is unaffected.

---

## Decisions made

- **Decision**: dedicated `EnvironmentState` attached by `GameplayState`.
  **Reason**: matches the per-state lifecycle; menu/end screens stay empty.
  **Alternatives considered**: build the ground in `OurcraftGame.simpleInitApp`
  (rejected — would show under the menu and persist across matches).

---

## Next session

- [ ] Consider grounded movement + block collision (player currently flies and
  clips through everything; no gravity/collision is in any spec yet).
