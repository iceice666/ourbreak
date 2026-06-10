## Why

M2 establishes the core destruction mechanic that makes the existing round and victory loop playable. Blocks need typed durability and weapons need deterministic, headless damage resolution before later milestones add NPC placement and first-person targeting.

## What Changes

- Replace the M1 block marker stub with typed block state covering all five block types and their durability.
- Add mascot and weapon ECS components, including the three player weapon types.
- Add headless weapon damage resolution with base damage, strong and weak counter multipliers, durability clamping, and block destruction.
- Restrict attacks to the ATTACK phase and accept preselected entity targets so M4 can add raycast and area-selection adapters without coupling game logic to rendering.
- Add unit coverage for block durability, overkill damage, weapon counters, invalid targets, and phase restrictions.

## Capabilities

### New Capabilities

- `block-durability`: Defines block types, initial durability, damage updates, and removal when durability reaches zero.
- `weapon-damage`: Defines weapon types, attack eligibility, target handling, base damage, and the weapon-to-block counter matrix.

### Modified Capabilities

None.

## Impact

- Affects ECS components under `app/src/main/java/com/ourcraft/ecs/components/`.
- Adds a headless `WeaponSystem` under `app/src/main/java/com/ourcraft/ecs/systems/`.
- Replaces construction sites that use the empty `BlockComponent` stub.
- Extends the JUnit 5 test suite with `BlockTest` and `WeaponTest`.
- Adds no dependencies and does not involve jME rendering, physics, input, or raycasting.
