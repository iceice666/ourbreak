## MODIFIED Requirements

### Requirement: Attack-phase eligibility
The weapon system SHALL apply damage only while the game-state entity is IN_PROGRESS and in the ATTACK phase.

#### Scenario: Attack during ATTACK phase
- **WHEN** a player with a weapon attacks a block while the game is IN_PROGRESS and the phase is ATTACK
- **THEN** weapon damage is applied to the block

#### Scenario: Attack during BUILD phase
- **WHEN** a player with a weapon attacks a block while the phase is BUILD
- **THEN** the block durability is unchanged

#### Scenario: Attack after game completion
- **WHEN** a player attacks while the game result is WIN or LOSS
- **THEN** all target blocks remain unchanged

### Requirement: Preselected target processing
The weapon system SHALL accept a non-null collection of entity IDs selected by an external targeting adapter and SHALL
process each distinct block target at most once per attack. Null IDs, missing entities, and non-block entities SHALL be
ignored.

#### Scenario: Multiple distinct blocks
- **WHEN** one attack supplies multiple distinct block entity IDs
- **THEN** each supplied block receives one damage application

#### Scenario: Duplicate block target
- **WHEN** one attack supplies the same block entity ID more than once
- **THEN** that block receives exactly one damage application

#### Scenario: Invalid targets
- **WHEN** an attack contains a null ID, missing entity, or entity without `BlockComponent`
- **THEN** each invalid target is ignored and other valid block targets are still processed

#### Scenario: Empty target collection
- **WHEN** an eligible attack supplies an empty target collection
- **THEN** the attack completes without modifying entity state

#### Scenario: Null target collection
- **WHEN** an attack supplies a null target collection
- **THEN** the request is rejected before any target is modified

## ADDED Requirements

### Requirement: Required weapon-system game state
The weapon system SHALL require phase and result components on its game-state entity before evaluating attack eligibility.

#### Scenario: Missing phase or result
- **WHEN** an attack is requested while required game-state eligibility data is absent
- **THEN** the request fails with an invariant error before any target is modified
