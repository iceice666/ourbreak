## Why

After M4/M5 the gameplay scene is just placeholder block cubes floating against a flat background — there is no ground, so the player has no spatial reference for where they stand or move. A minimal ground plane (with a grid for motion cues) makes the playable build legible without pulling forward the M8 art work.

## What Changes

- Add an `EnvironmentState` that renders a simple ground: a large horizontal plane at the block base level plus a grid overlay for depth/motion perception, visually distinct from the background.
- `GameplayState` attaches `EnvironmentState` while a match runs and detaches it on exit, so the ground appears only during gameplay and leaves no geometry behind afterward.
- No gameplay logic, components, or balancing change; this is scene decoration only. Real per-type block art and full environment art remain M8.

## Capabilities

### New Capabilities

- `scene-environment`: Defines the gameplay ground plane — a horizontal floor with a grid at the block base level, present during gameplay and removed on exit — giving the player a spatial reference.

## Impact

- Adds `EnvironmentState` under `ecs/systems/`; attached/detached by `GameplayState`.
- Uses jME `Quad`, `Grid`, `Material`, and the existing scene lights; adds no Gradle dependencies and no new components.
- Purely visual — the headless test suite is unaffected; verified by launching the app (a floor is visible under the blocks during gameplay).
