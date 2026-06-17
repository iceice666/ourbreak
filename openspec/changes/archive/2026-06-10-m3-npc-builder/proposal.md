## Why

M1 and M2 provide round progression, victory checks, block durability, and weapon damage, but no system creates the defenses that make those mechanics form a complete game loop. M3 adds deterministic NPC construction so a full BUILD-to-ATTACK round can run and be verified headlessly before renderer and input integration in M4.

## What Changes

- Add a headless `NpcBuilderSystem` that operates only during BUILD phases.
- Place a fixed placeholder number of new block entities around the mascot in a deterministic priority order: front, alternating sides, then expanding outer positions.
- Use round-specific block compositions: SAND in round 1, SAND/CORAL in round 2, ROCK/SHELL in round 3, and ROCK/JELLYFISH in round 4.
- Preserve surviving defenses between rounds and fill the highest-priority unoccupied positions first.
- Give each placed block the ECS components required for durability, world position, and later rendering.
- Trigger the existing BUILD-to-ATTACK transition after the round's placement script completes.
- Add headless tests for phase gating, placement order, block composition, occupied positions, survivor persistence, and phase completion.

## Capabilities

### New Capabilities

- `npc-building`: Defines deterministic BUILD-phase block placement around a mascot, per-round block compositions, occupied-position handling, and completion signaling.

### Modified Capabilities

None.

## Impact

- Adds `NpcBuilderSystem` under `app/src/main/java/com/ourbreak/ecs/systems/`.
- Uses the existing `MascotComponent`, `PositionComponent`, `BlockComponent`, `ModelComponent`, and game-state components.
- Integrates with the existing `RoundSystem.beginAttackPhase()` transition.
- Adds `NpcBuilderTest` under `app/src/test/java/com/ourbreak/`.
- Adds no dependencies and remains independent of jME rendering, physics, pathfinding, and input.
