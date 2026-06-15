## 1. App state machine scaffolding

- [x] 1.1 Add a `MainMenuState` (`BaseAppState`) that renders placeholder Start Game / Exit prompts on the GUI node and maps keys (Start, Exit)
- [x] 1.2 Add a `GameEndState` (`BaseAppState`) that takes a WIN/LOSS outcome, renders the corresponding placeholder text, and maps a Restart action
- [x] 1.3 Wire menu transitions: Start Game detaches `MainMenuState` and attaches `GameplayState`; Exit stops the app; Restart detaches `GameEndState` and attaches `MainMenuState`
- [x] 1.4 Replace `OurcraftGame.simpleInitApp()` placeholder cube spawning with attaching `MainMenuState` as the initial screen

## 2. Gameplay state and system orchestration

- [x] 2.1 In `GameplayState.initialize`, create a fresh `DefaultEntityData`, attach a `ModelViewState` bound to it, and call `RoundSystem.initialize()`
- [x] 2.2 Spawn the mascot (`MascotComponent` + `PositionComponent`) and the player (`WeaponComponent` + `PositionComponent`) entities
- [x] 2.3 Construct `VictorySystem`, `NpcBuilderSystem`, and attach `PlayerControlState` as a child state
- [x] 2.4 In `GameplayState.update`, run `NpcBuilderSystem.update` → `RoundSystem.update` → `VictorySystem.update` each frame
- [x] 2.5 Observe `GameResultComponent`; on WIN/LOSS detach gameplay and attach `GameEndState` with the outcome
- [x] 2.6 In `GameplayState.cleanup`, release the victory and NPC builder entity sets, close the `EntityData`, and detach `PlayerControlState`

## 3. Player control and input

- [x] 3.1 Add `PlayerControlState` (`BaseAppState`) that captures the mouse for first-person look and releases it on disable/detach
- [x] 3.2 Map WASD to player/camera movement
- [x] 3.3 Map keys 1/2/3 to write SWORD/GUN/DRONE into the player's `WeaponComponent`
- [x] 3.4 Map left-click to the attack action

## 4. Attack raycast wiring

- [x] 4.1 Tag each rendered block `Geometry` with its `EntityId` (or expose a synchronizer lookup) so a hit can be mapped back to an entity
- [x] 4.2 On attack, cast a `Ray` from the camera, collide against the scene root, and resolve the nearest block `EntityId` under the crosshair
- [x] 4.3 Invoke `WeaponSystem.attack(playerId, List.of(targetId))` for a hit; do nothing on a miss (let `WeaponSystem`'s phase gate handle BUILD-phase clicks)
- [x] 4.4 Confirm destroyed entities disappear via the existing `ModelViewSynchronizer` (no view-side removal code in `PlayerControlState`)

## 5. Verification

- [x] 5.1 Run `./gradlew test`; the existing headless suite stays green (zero failures, zero errors)
- [ ] 5.2 Manual smoke test — win path: launch app, Start Game, destroy all blocks within the timer, confirm the end screen shows WIN and Restart returns to the menu
- [ ] 5.3 Manual smoke test — loss path: launch app, let round 4 expire with blocks remaining, confirm the end screen shows LOSS and Restart returns to the menu
- [ ] 5.4 Confirm cursor capture/release behaves across menu → gameplay → end screen, and a second match starts fresh at round 1

> Note: 5.2–5.4 require an OpenGL display and were deferred — verify locally in a dev shell. 5.1 passed via a throwaway portable JDK 21 (BUILD SUCCESSFUL, full headless suite green).
