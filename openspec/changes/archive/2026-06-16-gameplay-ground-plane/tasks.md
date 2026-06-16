## 1. Environment state

- [x] 1.1 Add `EnvironmentState` (`BaseAppState`) that builds an environment `Node` on initialize: a large `Quad` floor at the block base level (y = -0.5) with a lit material in a distinct ground colour, plus a `Grid` overlay just above it
- [x] 1.2 Attach the environment node to the scene root on enable and remove it on disable

## 2. Gameplay wiring

- [x] 2.1 In `GameplayState`, attach `EnvironmentState` during world construction and detach it on cleanup

## 3. Verification

- [x] 3.1 Run `./gradlew test`; the headless suite stays green (no logic change)
- [x] 3.2 Launch the app and confirm a ground plane + grid is visible under the blocks during gameplay, and absent on the menu/end screen
