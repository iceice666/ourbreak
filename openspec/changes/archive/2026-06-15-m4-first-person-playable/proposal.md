## Why

M1–M3 deliver the full game loop — round progression, victory checks, block durability, weapon counter-matrix, and deterministic NPC building — but all of it runs headless. `OurbreakGame` still only spawns two placeholder cubes, so none of the logic is reachable by a player. M4 wires those systems into a runnable jME3 application so the project becomes a completable first-person game: the **first Minimum Shippable Game**.

## What Changes

- Add a jME `AppState` machine that owns screen lifecycle and transitions: main menu → gameplay → end screen → back to menu.
- Add `MainMenuState`: Start Game and Exit using minimal placeholder UI (no Lemur — Lemur arrives in M6).
- Add `GameplayState`: the root state that constructs the ECS world (game-state entity, mascot, player) and attaches/updates `RoundSystem`, `VictorySystem`, `NpcBuilderSystem`, and `PlayerControlState`, and detaches/closes them on exit; watches `GameResultComponent` and transitions to the end screen on WIN/LOSS.
- Add `GameEndState`: shows Win or Lose plus a Restart action that returns to the main menu.
- Add `PlayerControlState`: WASD movement, mouse-look capture, weapon switch on the 1/2/3 keys (writes `WeaponComponent`), and left-click attack that triggers an attack against the block under the crosshair.
- Add camera-ray attack wiring: a ray from the camera selects the block entity under the crosshair and feeds it to `WeaponSystem.attack(...)`; destroyed entities are removed by the existing model-view synchronizer.
- Keep the entire existing headless test suite green; verify M4 itself through a manual smoke test — a full 4-round match driven to both a win and a loss.

## Capabilities

### New Capabilities

- `app-state-machine`: Defines the screen-state lifecycle (main menu, gameplay, game end), how the gameplay state wires and tears down the M1–M3 systems, and how a finished `GameResultComponent` drives the transition to the end screen and back to the menu.
- `player-control`: Defines first-person input — movement, mouse-look, weapon switching across the three weapon types, and the attack trigger — and how attack input resolves a target block via a camera ray and applies it through `WeaponSystem`.

### Modified Capabilities

None. (`model-view-sync` already removes spatials for destroyed entities; `weapon-damage`, `round-system`, `victory-system`, and `npc-building` are consumed unchanged.)

## Impact

- Replaces the placeholder body of `OurbreakGame.simpleInitApp()` with attachment of the state machine starting at `MainMenuState`.
- Adds `MainMenuState`, `GameplayState`, `GameEndState`, and `PlayerControlState` under `app/src/main/java/com/ourbreak/ecs/systems/` (or an `app`/`state` sibling package).
- Consumes existing `RoundSystem`, `VictorySystem`, `NpcBuilderSystem`, `WeaponSystem`, `ModelViewState`, and the game-state / `MascotComponent` / `PositionComponent` / `WeaponComponent` components without changing their APIs.
- Adds no new Gradle dependencies (Lemur deferred to M6); relies on jME `BaseAppState`, input, and collision/ray APIs already provided by the `jme3` bundle.
- Runtime/visual behavior is validated by manual smoke test; the headless JUnit suite remains the automated gate and stays green.
