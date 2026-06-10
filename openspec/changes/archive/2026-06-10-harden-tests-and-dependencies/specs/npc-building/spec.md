## MODIFIED Requirements

### Requirement: Mascot-relative placement priority
The NPC builder SHALL require a valid mascot position, place blocks on the mascot's XZ grid plane, preserve the mascot's
Y coordinate, define positive Z as front, and search deterministic concentric rings from the mascot outward.

Within each ring, the builder SHALL prioritize front center, left and right centers, the remaining front edge in
symmetric pairs, the remaining side edges from front to rear in symmetric pairs, and the remaining rear edge ending at
rear center.

#### Scenario: First-ring placement order
- **WHEN** the mascot is at `(0, 0, 0)` and the first ring is unoccupied
- **THEN** the first eight positions are `(0, 0, 1)`, `(-1, 0, 0)`, `(1, 0, 0)`, `(-1, 0, 1)`, `(1, 0, 1)`,
  `(-1, 0, -1)`, `(1, 0, -1)`, and `(0, 0, -1)` in that order

#### Scenario: Translate positions from the mascot
- **WHEN** the mascot is at a non-origin grid position
- **THEN** every generated offset is added to the mascot position and every placed block uses the mascot's Y coordinate

#### Scenario: Missing mascot position
- **WHEN** an eligible BUILD update runs while the mascot lacks `PositionComponent`
- **THEN** the update fails with an invariant error and creates no block

### Requirement: Occupied-position handling and survivor persistence
The NPC builder SHALL treat positions containing an existing block as occupied, SHALL skip occupied candidate positions,
SHALL continue searching outward through concentric rings, and SHALL leave all existing blocks unchanged.

#### Scenario: Skip a priority position
- **WHEN** the highest-priority candidate position already contains a block
- **THEN** the next new block is placed at the first unoccupied candidate position

#### Scenario: Fully occupied first ring
- **WHEN** all eight first-ring positions are occupied
- **THEN** the next new block is placed at front center of the second ring

#### Scenario: Preserve surviving defenses
- **WHEN** a new BUILD phase starts with blocks surviving from an earlier round
- **THEN** the surviving entities retain their type, durability, and position while eight additional blocks are placed in
  unoccupied positions

#### Scenario: Refill a destroyed priority position
- **WHEN** a previously occupied high-priority position is empty at the start of a later BUILD phase
- **THEN** that position is selected before lower-priority free positions

## ADDED Requirements

### Requirement: Required NPC game state
The NPC builder SHALL require round, phase, and result components on the round system's game-state entity before
evaluating a build update.

#### Scenario: Missing required game-state component
- **WHEN** an NPC update runs while required game-state data is absent
- **THEN** the update fails with an invariant error and creates no block

### Requirement: Supported NPC round script
The NPC builder SHALL fail before creating a block when the active BUILD round has no defined block script.

#### Scenario: Unsupported round
- **WHEN** an eligible BUILD update refers to a round outside the configured scripts
- **THEN** the update fails with an invariant error and creates no block
