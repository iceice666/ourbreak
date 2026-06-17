## Context

The gameplay scene renders only the NPC's placeholder block cubes (all cyan, via `ModelViewState`) against a solid background colour. Blocks sit on the mascot's grid at y≈0; the player camera looks in from y≈1.5. With nothing under the blocks the scene has no horizon or ground reference, so movement reads as floating. This change adds a thin, deferred-art-free ground so the build is legible. It is runtime-visual only, so — like the M4 AppStates — it carries no headless tests and is verified by launching the app.

## Goals / Non-Goals

**Goals:**
- A visible horizontal ground plane beneath the play area, distinct from the background.
- A grid overlay so motion and "standing on the ground" are perceptible.
- Ground present only during gameplay; cleanly removed on exit (no leaked geometry).

**Non-Goals:**
- Per-block-type colours/models, mascot/player/weapon models, skybox, textures — all M8 art.
- Collision/physics with the ground (movement stays fly-cam; no walking simulation).
- Any gameplay, component, or balancing change.

## Decisions

### D1 — A dedicated `EnvironmentState`, attached by `GameplayState`
The ground is a gameplay-scene concern, so a `BaseAppState` builds it on enable and removes it on disable, and `GameplayState` attaches/detaches it alongside `ModelViewState` and `PlayerControlState`. Rationale: matches the existing per-state lifecycle and keeps `GameplayState` from hand-managing raw geometry. Alternative: attach the ground in `OurbreakGame.simpleInitApp` — rejected because it would also show under the menu and persist across matches.

### D2 — `Quad` floor + `Grid` overlay using existing lights
A large `Quad` rotated flat at the block base level (y = -0.5, the bottom of the unit blocks) with a lit material gives a solid ground; a `com.jme3.scene.debug.Grid` slightly above it gives line cues for motion. Both reuse the `DirectionalLight`/`AmbientLight` already added in `OurbreakGame`. Rationale: cheapest possible "floor + reference lines" with stock jME shapes, no assets. Alternative: a textured terrain — rejected as M8-scale art.

## Risks / Trade-offs

- **Quad/Grid orientation can be wrong by 90°** → Mitigation: use the standard jME flat-floor transform (rotate −90° about X) and verify visually by launching, which is now possible in this WSLg environment.
- **Software (llvmpipe) rendering here** → Accepted: the floor is a couple of triangles plus lines; negligible cost even without GPU acceleration.
