# NPC Building Spec

## Purpose

NPC building defines deterministic, headless construction of round-specific block defenses around the mascot.

---

## Requirements

### Requirement: BUILD-phase eligibility
The NPC builder SHALL place blocks only while the game result is IN_PROGRESS and the current phase is BUILD.

#### Scenario: Place during an active BUILD phase
- **WHEN** the game is in progress, the phase is BUILD, and the current round has placements remaining
- **THEN** one builder update creates one block

#### Scenario: Ignore ATTACK-phase updates
- **WHEN** the current phase is ATTACK
- **THEN** a builder update creates no blocks

#### Scenario: Ignore updates after game completion
- **WHEN** the game result is WIN or LOSS
- **THEN** a builder update creates no blocks

---

### Requirement: Per-round placement quota
The NPC builder SHALL place a per-round number of new blocks given by `min(16 + (round - 1) * 8, 48)` — 16, 24, 32, 40, then 48 from round 5 onward — and SHALL place at most one block per update.

#### Scenario: Incremental construction
- **WHEN** an active BUILD phase has the round's placements remaining
- **THEN** that many consecutive builder updates each create one new block

#### Scenario: Escalating then capped quota
- **WHEN** the run progresses from round 1 upward
- **THEN** the per-round quota rises 16, 24, 32, 40, 48 and stays at 48 for all later rounds

#### Scenario: No premature completion
- **WHEN** fewer than the round's quota of blocks have been placed for the current round
- **THEN** the phase remains BUILD

---

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

---

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
- **THEN** the surviving entities retain their type, durability, and position while the round's quota of additional
  blocks is placed in unoccupied positions

#### Scenario: Refill a destroyed priority position
- **WHEN** a previously occupied high-priority position is empty at the start of a later BUILD phase
- **THEN** that position is selected before lower-priority free positions

---

### Requirement: Round-specific block composition
The NPC builder SHALL use the successful placement index to repeat the ordered block script assigned to the current
round.

The scripts SHALL be SAND for round 1, SAND then CORAL for round 2, ROCK then SHELL for round 3, ROCK then JELLYFISH for
round 4, and ROCK, SHELL, JELLYFISH, then CORAL (the full gauntlet) for round 5 and beyond.

#### Scenario: Campaign opening rounds
- **WHEN** the builder completes rounds 1–4
- **THEN** the placed types follow the per-round scripts (all SAND; alternating SAND/CORAL; ROCK/SHELL; ROCK/JELLYFISH)

#### Scenario: Endless gauntlet from round 5
- **WHEN** the builder completes round 5 or any later round
- **THEN** the placed types repeat ROCK, SHELL, JELLYFISH, CORAL in order

---

### Requirement: Complete block entity state
Each NPC placement SHALL create a block entity with `PositionComponent`, a full-health `BlockComponent` of the scripted
type, and a stable type-derived `ModelComponent`.

#### Scenario: Create a usable block entity
- **WHEN** the builder places a ROCK block
- **THEN** the new entity has the selected grid position, ROCK current and maximum durability of 4, and the ROCK model
  identifier

---

### Requirement: BUILD completion signaling
After the round's final (quota-th) successful placement, the NPC builder SHALL trigger the existing round transition to
ATTACK, including resetting the attack timer to its full duration.

#### Scenario: Complete construction
- **WHEN** the builder creates the last block of the current round's quota
- **THEN** the phase becomes ATTACK and the attack timer equals 60 seconds

#### Scenario: Remain inactive after completion
- **WHEN** construction has transitioned the current round to ATTACK
- **THEN** subsequent builder updates create no additional blocks

---

### Requirement: Required NPC game state
The NPC builder SHALL require round, phase, and result components on the round system's game-state entity before
evaluating a build update.

#### Scenario: Missing required game-state component
- **WHEN** an NPC update runs while required game-state data is absent
- **THEN** the update fails with an invariant error and creates no block

---

### Requirement: Supported NPC round script
The NPC builder SHALL support every round number of 1 or greater and SHALL fail before creating a block only for a non-positive round.

#### Scenario: High round supported
- **WHEN** an eligible BUILD update runs for round 9
- **THEN** the builder places blocks using the gauntlet script and the round-9 quota

#### Scenario: Invalid round
- **WHEN** an eligible BUILD update refers to a round less than 1
- **THEN** the update fails with an invariant error and creates no block
