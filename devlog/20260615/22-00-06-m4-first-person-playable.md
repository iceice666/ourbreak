# Devlog — 2026-06-15 22:00:06 — `m4-first-person-playable`

> **Author**: ceil
> **Build / Version**: M4 — first Minimum Shippable Game
> **Branch / Commit**: main

---

## Summary

Wired the headless M1–M3 systems into a runnable jME3 game via an AppState
machine, reaching M4 — the first completable build. Planned and tracked through
OpenSpec (`m4-first-person-playable`, now archived).

---

## Goals for this session

- [x] Plan M4 with OpenSpec (proposal → design → specs → tasks)
- [x] Implement the AppState machine + player control + raycast wiring
- [x] Keep the headless test suite green

---

## What I worked on

### Feature / System: `app-state-machine`

- `MainMenuState`, `GameplayState`, `GameEndState` as `BaseAppState`
  subclasses; transitions are detach-current / attach-next.
- `GameplayState` owns a fresh `EntityData` per match: initializes `RoundSystem`,
  spawns mascot + player, constructs `VictorySystem` / `NpcBuilderSystem`,
  attaches `PlayerControlState`, runs them in order each frame, and tears them
  all down on exit. WIN/LOSS on `GameResultComponent` drives the end screen.
- Placeholder `BitmapText` + key-mapped menus (no Lemur — that's M6).

### Feature / System: `player-control`

- `PlayerControlState`: fly-cam WASD + mouse-look with cursor capture, 1/2/3
  weapon switch writing `WeaponComponent`, left-click attack.
- Attack ray-picks the block under the crosshair and calls
  `WeaponSystem.attack(...)`; destroyed entities disappear via the existing
  `ModelViewSynchronizer`.

### Refactors / cleanup

- `ModelViewSynchronizer` now tags each spatial with its `EntityId`
  (`ENTITY_ID_USER_DATA`) so ray hits map back to entities — the single source
  of truth for picking.
- `OurbreakGame` drops the placeholder cubes and just attaches `MainMenuState`.

---

## Technical notes

- M4 is pure composition: all decision logic stays in the already-tested M1–M3
  systems, so no new automated tests — the headless suite is the gate and the
  M4 runtime is verified by manual smoke test.
- Update order in `GameplayState`: `NpcBuilderSystem` → `RoundSystem` →
  `VictorySystem`, matching the headless test ordering.

---

## Decisions made

- **Decision**: Per-match `EntityData` owned by `GameplayState`.
  **Reason**: Restart becomes "drop the state, attach a new one" — no stale
  entities or unreleased `EntitySet`s.
  **Alternatives considered**: one app-wide `EntityData` reset between matches.

- **Decision**: Camera-ray picking against the scene over bullet physics.
  **Reason**: Reuses existing damage/removal paths, no new dependency.

---

## Open questions / blockers

- [ ] Manual smoke tests (win path, loss path, cursor handoff) still pending —
  need an OpenGL display; this session ran headless only.

---

## Next session

- [ ] Run the M4 smoke test locally; confirm a full 4-round match to win + loss
- [ ] Start M5 — block special effects (`BlockEffectSystem`, `EffectComponent`)

---

## References

- OpenSpec change: `openspec/changes/archive/2026-06-15-m4-first-person-playable/`
- Milestone breakdown: `design/milestones.md` (M4)
