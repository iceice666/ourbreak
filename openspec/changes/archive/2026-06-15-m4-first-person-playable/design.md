## Context

M1–M3 produced four headless systems — `RoundSystem`, `VictorySystem`, `NpcBuilderSystem`, `WeaponSystem` — plus `ModelViewState`/`ModelViewSynchronizer` for entity→spatial rendering. They are fully unit-tested but unreachable: `OurbreakGame.simpleInitApp()` only spawns two static cubes and positions a fixed camera. M4 is the wiring milestone that turns this into a playable, completable game using jME's `AppState` machinery and input mapping. No new game logic is introduced; the work is composition, lifecycle, and input plumbing.

Constraints carried from `design/milestones.md` and `CLAUDE.md`:
- No new Gradle dependency (Lemur and real UI are deferred to M6; menus use placeholder rendering only).
- The existing headless JUnit suite must stay green; M4's runtime behavior is validated by a manual smoke test (a full 4-round match to both a win and a loss).
- Java 21, `com.ourbreak` package root, placeholder cubes acceptable.

## Goals / Non-Goals

**Goals:**
- A jME `AppState` machine with three screens — main menu, gameplay, game end — and clean transitions between them.
- `GameplayState` constructs the ECS world and owns the lifecycle of the M1–M3 systems: create on enter, update each frame in the correct order, close/detach on exit.
- First-person control: WASD movement, mouse-look, weapon switch (1/2/3), left-click attack against the block under the crosshair via a camera ray.
- WIN/LOSS observed from `GameResultComponent` drives the transition to the end screen; Restart returns to the menu with a fresh world.

**Non-Goals:**
- Lemur or any polished UI widgets (M6); menus are minimal placeholder rendering.
- HUD (round counter, timer, building count) — M6.
- Block special effects (M5), balancing of placeholder constants (M7), real 3D art (M8).
- Automated tests of jME runtime behavior; M4 verification is manual.

## Decisions

### D1 — `BaseAppState` subclasses, not a hand-rolled state machine
Use jME's `AppStateManager` with `BaseAppState` subclasses (`MainMenuState`, `GameplayState`, `GameEndState`). Transitions are "detach current, attach next" performed inside `update`/input callbacks. Rationale: jME already provides the lifecycle (`initialize`/`onEnable`/`onDisable`/`cleanup`) and ordered per-frame `update`; a custom machine would duplicate it. Alternative considered: a single monolithic state with an internal screen enum — rejected because it muddles teardown of the ECS world between matches.

### D2 — `GameplayState` owns the `EntityData` and system lifecycle
`GameplayState.initialize` creates a fresh `DefaultEntityData`, attaches a `ModelViewState` for that data, calls `RoundSystem.initialize()`, spawns the mascot (with `MascotComponent` + `PositionComponent`) and the player (with `WeaponComponent` + `PositionComponent`), then constructs `VictorySystem`, `NpcBuilderSystem`, and `PlayerControlState`. `cleanup` closes the `EntitySet`-holding systems (`VictorySystem.close()`, `NpcBuilderSystem.close()`) and the `EntityData`, and detaches child states. Rationale: a self-contained world per match makes Restart trivial (drop the state, attach a new one). Alternative: a single app-wide `EntityData` reset between matches — rejected as error-prone (stale entities, unreleased `EntitySet`s).

### D3 — Per-frame update order
Within `GameplayState.update(tpf)`: `NpcBuilderSystem.update` (BUILD placement) → `RoundSystem.update` (phase/timer advance) → `VictorySystem.update` (win/loss check), with `PlayerControlState` running as an attached child state for input. `WeaponSystem.attack` is invoked event-style from the player's left-click, not polled. Rationale: matches the headless test ordering and keeps the authoritative phase/result transitions where M1–M3 already put them.

### D4 — Attack target via camera ray over scene collision
`PlayerControlState` casts a `Ray` from the camera origin along its direction, collides against the rendered scene root (`CollisionResults`), and maps the hit `Geometry` back to its `EntityId`. The mapping is recovered from the synchronizer's spatial-per-entity bookkeeping (or a `Geometry` user-data tag set at spawn). The resolved `EntityId` is passed to `WeaponSystem.attack(playerId, List.of(targetId))`; `WeaponSystem` already gates on ATTACK phase and applies durability/counter-matrix, and `ModelViewSynchronizer` already removes spatials for entities `WeaponSystem` deletes. Rationale: reuses the existing damage and removal paths unchanged. Alternative: jME-bullet physics picking — rejected (adds a dependency, overkill for crosshair selection).

### D5 — Placeholder menu/end-screen rendering
Main menu and end screen render with attached text/geometry on the GUI node and map keyboard actions (e.g., Enter = Start/Restart, Esc = Exit/menu) rather than clickable widgets. Rationale: avoids the Lemur dependency until M6 while keeping screens functional and smoke-testable.

## Risks / Trade-offs

- **No automated coverage of M4 wiring** → Mitigation: keep all decision logic in the already-tested M1–M3 systems; restrict M4 code to composition and input so the manual smoke test (win path + loss path) is sufficient. Document the smoke-test steps in `tasks.md`.
- **Geometry→EntityId mapping is fragile if spawn and view diverge** → Mitigation: establish a single source of truth (tag the `Geometry` with its `EntityId`, or query the synchronizer), and never let `PlayerControlState` create spatials itself.
- **Mouse-look/cursor capture leaking across screens** → Mitigation: enable fly-cam/cursor capture only while `GameplayState` is enabled; restore the cursor on `onDisable` so menu and end screens are usable.
- **Restart leaving stale entities or unreleased `EntitySet`s** → Mitigation: D2's per-match `EntityData` plus explicit `close()` in `cleanup`; a new `GameplayState` instance per match.
- **Running a headless/CI environment with no display** → Mitigation: M4 classes are only exercised at runtime, never from the JUnit suite, so CI stays headless and green; the smoke test is run locally.
