## Context

The project has 40 passing JUnit tests covering the main M1-M3 flows, but several public methods and ECS records have
undefined behavior at invalid-state and lifecycle boundaries. `ModelViewState` also combines entity synchronization with
jME material creation, which makes its state-transition logic difficult to test headlessly. Dependency resolution is
mostly current, but JUnit is behind the latest stable release and Zay-ES 1.6.0 exposes Guava 19.0 on the runtime classpath.

The implementation must stay inside the existing Java 21, Gradle, jME 3.9, and Zay-ES architecture. New test libraries or
runtime dependencies are out of scope without separate approval.

## Goals / Non-Goals

**Goals:**

- Turn ambiguous edge behavior into explicit, testable contracts.
- Cover invalid values, missing state, repeated calls, completed-game calls, and boundary transitions.
- Test model-view synchronization without requiring a display or renderer.
- Update JUnit and remove known Guava findings while preserving Zay-ES compatibility.
- Keep public APIs and normal gameplay behavior stable.

**Non-Goals:**

- Implement M4 first-person gameplay or change game balance.
- Introduce property-testing, mocking, mutation-testing, or dependency-update plugins.
- Upgrade to prerelease jME versions or broadly override transitive dependencies that have no demonstrated issue.
- Add UI, networking, persistence, or SQL-backed entity data.

## Decisions

### Validate component state at construction

Records will reject invalid state in compact constructors. Required references will use `Objects.requireNonNull`; model
identifiers will also reject blank values; position coordinates and numeric round fields will require finite values and
valid ranges. `BlockComponent` retains its existing validation and gains exhaustive boundary tests.

This keeps invalid state out of `EntityData` rather than requiring every system to defend against impossible component
values. The alternative was system-local validation, which duplicates checks and still permits corrupted ECS state.

### Make system preconditions explicit

Systems will distinguish caller input errors from corrupted game state:

- Null method arguments are rejected immediately.
- Invalid elapsed time is rejected with `IllegalArgumentException`.
- Missing required components are reported with `IllegalStateException`.
- Calls that are valid but ineligible because of phase or completed game state are no-ops.

Repeated `RoundSystem.initialize()` calls will preserve the original game-state entity. Repeated attack-phase signals
outside an active BUILD phase will be no-ops so they cannot reset a running timer.

### Process zero-time boundaries deterministically

`RoundSystem.update()` will process a non-final ATTACK round whose timer is already zero, advancing exactly once to the
next BUILD phase. Negative and non-finite elapsed time will never increase or corrupt the timer.

`VictorySystem` will continue to evaluate block clearance before final-round timeout. Therefore simultaneous clearance
and expiry resolves to WIN, while remaining blocks resolve to LOSS.

### Keep target processing tolerant inside a valid attack request

`WeaponSystem.attack()` will require non-null player and target collection references, but an empty collection is a no-op
and null, missing, non-block, or duplicate target IDs are ignored. The system will require valid game-state phase and
result components, and completed games will reject damage through a no-op.

### Extract renderer-independent model synchronization

Entity-set polling and the `EntityId`-to-`Spatial` lifecycle will move into a package-private synchronizer that accepts a
spatial factory. `ModelViewState` will own jME initialization and provide the production factory that creates geometry and
materials.

When only position changes, the existing spatial will be moved. When `ModelComponent` changes, the old spatial will be
detached and replaced so its model identity cannot become stale. Removal and cleanup will detach all owned spatials and
release the entity set. This extraction is preferred over starting a headless jME application in unit tests because it
keeps tests deterministic and avoids renderer lifecycle complexity.

### Use parameterized tests for input matrices

JUnit parameterized tests will cover invalid floating-point values, constructor ranges, completed outcomes, and matchup
matrices. Focused scenario tests will remain for state transitions and entity lifecycle. No additional test framework is
needed.

### Pin only justified dependency updates

The version catalog will update JUnit to 6.1.0 and explicitly select Guava 33.6.0-jre over Zay-ES's transitive Guava 19.0.
The resolved runtime graph and all tests will verify compatibility. jME 3.9.0-stable, Zay-ES 1.6.0, Gradle 9.5.1, and the
Foojay resolver 1.0.0 remain pinned because they are current stable releases; jME 3.10.0-beta1 is excluded as prerelease.

Directly overriding Gson or SLF4J is deferred because the audit found no applicable OSV advisory and no project use
requiring a newer API. Broad transitive modernization would increase compatibility risk without advancing this change.

## Risks / Trade-offs

- [Stricter constructors expose previously hidden invalid state] -> Add focused tests and update all construction sites in
  the same change.
- [A modern Guava release could be binary-incompatible with Zay-ES] -> Run the complete suite and exercise Zay-ES entity
  filtering used by the project; remove the override if compatibility cannot be established.
- [Model synchronization extraction adds one internal class] -> Keep it package-private and limited to entity/spatial
  lifecycle ownership.
- [Dependency freshness changes over time] -> Record the verified versions in tasks and use stable-release policy rather
  than automatically accepting prereleases.
- [Fail-fast missing-state behavior may terminate a game loop] -> Reserve exceptions for violated invariants; ordinary
  phase and completed-game ineligibility remains a no-op.

## Migration Plan

1. Add edge-case tests that express the new contracts.
2. Add component validation and system guards until those tests pass.
3. Extract and test model-view synchronization, then delegate from `ModelViewState`.
4. Update JUnit and add the Guava constraint or direct dependency override.
5. Run dependency resolution, OSV verification, and the complete Gradle test suite.

Rollback is a normal source revert: no persisted data or external API migration is involved.

## Open Questions

- None. The dependency versions will be rechecked immediately before implementation in case a newer stable patch is
  published.
