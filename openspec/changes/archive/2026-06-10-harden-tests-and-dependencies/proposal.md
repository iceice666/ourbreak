## Why

The current headless gameplay suite covers core flows well but leaves invalid state, lifecycle misuse, unusual time input,
completed-game behavior, and model-view synchronization either untested or undefined. The dependency audit also found an
outdated JUnit release and a vulnerable legacy Guava version inherited transitively from Zay-ES.

## What Changes

- Define and test fail-fast component invariants so invalid ECS state cannot silently enter the game.
- Make round lifecycle and timer behavior deterministic for duplicate initialization, invalid elapsed time, repeated phase
  signals, and already completed games.
- Prevent attacks after the game has ended and define handling for empty, duplicate, null, missing, and non-block targets.
- Define victory precedence when block clearance and final-round timer expiry occur in the same update.
- Harden NPC construction around missing required state and placement beyond a fully occupied first ring.
- Add headless coverage for model-view entity addition, position updates, model replacement, removal, and cleanup.
- Upgrade JUnit from 6.0.1 to 6.1.0.
- Override Zay-ES's transitive Guava 19.0 with a compatible, supported release that removes known OSV findings, while
  retaining current stable jMonkeyEngine, Zay-ES, Gradle, and Foojay versions.

## Capabilities

### New Capabilities

- `ecs-component-integrity`: Shared construction invariants for required enum, numeric, identifier, and position component
  state.
- `model-view-sync`: Synchronization and lifecycle behavior between renderable ECS entities and scene spatials.
- `dependency-health`: Supported dependency freshness, stable-release selection, compatibility verification, and known
  vulnerability handling.

### Modified Capabilities

- `block-durability`: Reject invalid block durability state and invalid damage values while preserving zero-damage
  semantics.
- `round-system`: Define initialization idempotence, elapsed-time validation, legal phase signaling, and completed-game
  no-op behavior.
- `victory-system`: Define deterministic WIN precedence when all blocks are cleared at final-round expiry.
- `weapon-damage`: Disallow attacks after game completion and define null and empty target handling.
- `npc-building`: Define failure behavior when required mascot or game-state components are absent and verify outward
  placement after the first ring is occupied.

## Impact

Affected code includes ECS component constructors, `RoundSystem`, `VictorySystem`, `WeaponSystem`, `NpcBuilderSystem`,
`ModelViewState`, their JUnit tests, the Gradle version catalog, and dependency resolution. Public method signatures are
expected to remain unchanged, but previously tolerated invalid calls may now throw documented exceptions or become
explicit no-ops.
