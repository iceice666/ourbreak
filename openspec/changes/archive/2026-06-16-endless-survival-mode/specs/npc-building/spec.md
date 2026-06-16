## MODIFIED Requirements

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

### Requirement: Round-specific block composition
The NPC builder SHALL use the successful placement index to repeat the ordered block script for the current round. The scripts SHALL be: round 1 SAND; round 2 SAND then CORAL; round 3 ROCK then SHELL; round 4 ROCK then JELLYFISH; and round 5 and beyond ROCK, SHELL, JELLYFISH, then CORAL (the full gauntlet).

#### Scenario: Campaign opening rounds
- **WHEN** the builder completes rounds 1–4
- **THEN** the placed types follow the per-round scripts (all SAND; SAND/CORAL; ROCK/SHELL; ROCK/JELLYFISH)

#### Scenario: Endless gauntlet from round 5
- **WHEN** the builder completes round 5 (or any later round)
- **THEN** the placed types repeat ROCK, SHELL, JELLYFISH, CORAL in order

### Requirement: Supported NPC round script
The NPC builder SHALL support every round number of 1 or greater and SHALL fail before creating a block only for a non-positive round.

#### Scenario: High round supported
- **WHEN** an eligible BUILD update runs for round 9
- **THEN** the builder places blocks using the gauntlet script and the round-9 quota

#### Scenario: Invalid round
- **WHEN** an eligible BUILD update refers to a round less than 1
- **THEN** the update fails with an invariant error and creates no block
