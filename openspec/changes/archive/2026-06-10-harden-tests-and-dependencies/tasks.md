## 1. Component Integrity

- [x] 1.1 Add parameterized tests for invalid `BlockComponent` construction, invalid damage, zero damage, and durability
  boundaries.
- [x] 1.2 Add component tests for null enum values, null or blank model identifiers, non-finite positions, vector
  conversion, and invalid round ranges and durations.
- [x] 1.3 Implement compact-constructor validation for `GameResultComponent`, `PhaseComponent`, `ModelComponent`,
  `PositionComponent`, and `RoundComponent`, preserving existing `WeaponComponent` and `BlockComponent` behavior.

## 2. Round And Victory Boundaries

- [x] 2.1 Add round-system tests for repeated initialization, repeated attack-phase signals, completed-game signals,
  invalid elapsed time, existing zero-time advancement, and missing required game-state components.
- [x] 2.2 Implement idempotent round initialization, explicit game-state invariant checks, elapsed-time validation, legal
  BUILD-to-ATTACK gating, and one-time advancement from an existing zero timer.
- [x] 2.3 Add and pass the victory precedence test proving that simultaneous final-round expiry and block clearance
  resolves to WIN.

## 3. Weapon And NPC Boundaries

- [x] 3.1 Add weapon-system tests for WIN and LOSS gating, empty targets, null target IDs, null collections, and missing
  phase or result components.
- [x] 3.2 Implement result-aware weapon eligibility and explicit game-state invariant checks while preserving tolerant
  processing of duplicate, null, missing, and non-block target IDs.
- [x] 3.3 Add NPC-builder tests for missing mascot position, missing round/phase/result state, unsupported rounds, and
  second-ring placement when the first ring is full.
- [x] 3.4 Implement explicit NPC invariant checks and ensure unsupported scripts fail before any entity is created.

## 4. Headless Model-View Synchronization

- [x] 4.1 Extract package-private renderer-independent entity-to-spatial synchronization with an injected spatial
  factory, and delegate `ModelViewState` lifecycle calls to it.
- [x] 4.2 Implement position-only updates, model-change spatial replacement, entity or component removal, entity-set
  release, and idempotent cleanup.
- [x] 4.3 Add headless tests for incomplete entities, addition, position updates, model replacement, removal of entities
  and required components, and cleanup with multiple spatials.

## 5. Dependency Health

- [x] 5.1 Recheck authoritative release metadata immediately before editing versions and confirm approval for the direct
  Guava override required by repository dependency policy.
- [x] 5.2 Update JUnit to the latest compatible stable 6.1 patch and verify all JUnit modules and the launcher resolve to
  one version family.
- [x] 5.3 Add the approved Guava 33.6.0-jre or newer compatible stable override and verify Guava 19.0 is absent from
  `:app:runtimeClasspath`.
- [x] 5.4 Exercise Zay-ES entity creation, component access, entity-set changes, and filtering against the overridden
  Guava version; leave Gson and SLF4J transitives unmanaged unless new evidence requires action.
- [x] 5.5 Verify jMonkeyEngine, Zay-ES, Gradle wrapper, and Foojay resolver remain on the latest stable releases and record
  why any newer prerelease is excluded.

## 6. Final Verification

- [x] 6.1 Run `nix develop -c ./gradlew test --rerun-tasks` and resolve every failure or error.
- [x] 6.2 Run dependency reports for application and test runtime classpaths and repeat the OSV check for resolved
  dependency versions.
- [x] 6.3 Run `nix develop -c openspec validate harden-tests-and-dependencies` and confirm every task maps to the approved
  design and requirement deltas.
