# Block Effects Spec

## Purpose

Block effects define the per-block special-effect triggers that give the game tactical depth: an `EffectComponent` marker derived from block type, the Coral proximity slow, the Shell on-destroy reflect against a decoupled `PlayerHealthComponent`, the Jellyfish placement flicker trigger, and the Drone 3×3 area expansion. All magnitudes are placeholders tuned in M7.

---

## Requirements

### Requirement: Effect marker assignment
Each placed block SHALL carry an `EffectComponent` whose `effectType` is derived from its block type — Coral→SLOW, Shell→REFLECT, Jellyfish→FLICKER — and Sand and Rock blocks SHALL receive no `EffectComponent`.

#### Scenario: Effect-bearing blocks
- **WHEN** the NPC builder places a Coral, Shell, or Jellyfish block
- **THEN** that block entity has an `EffectComponent` of SLOW, REFLECT, or FLICKER respectively

#### Scenario: Plain blocks
- **WHEN** the NPC builder places a Sand or Rock block
- **THEN** that block entity has no `EffectComponent`

---

### Requirement: Coral proximity slow
During the ATTACK phase, the block-effect system SHALL compute a movement slow factor from Coral blocks within 1.5 grid cells of the player position, returning the strongest applicable slow, and SHALL return no slow (factor 1.0) when no Coral block is in range.

#### Scenario: Player within range of a Coral block
- **WHEN** the player is within 1.5 grid cells of a Coral block during ATTACK
- **THEN** the computed slow factor is less than 1.0

#### Scenario: Player outside range
- **WHEN** no Coral block is within 1.5 grid cells of the player
- **THEN** the computed slow factor is exactly 1.0

#### Scenario: Slow magnitude is a placeholder
- **WHEN** a Coral slow applies
- **THEN** the reduction uses a named placeholder constant (tuned in M7), not a hard-coded final value

---

### Requirement: Player reflect health
The player entity SHALL carry a `PlayerHealthComponent` with current and maximum hit points, and reflect damage SHALL reduce current health clamped at zero. The match result SHALL NOT depend on player health — win/loss remains decided by buildings and the round timer, and M5 SHALL add no player-death condition.

#### Scenario: Reflect reduces health
- **WHEN** reflect damage is applied to the player
- **THEN** current health decreases by the reflect amount, not below zero

#### Scenario: Health does not decide the match
- **WHEN** the player's health reaches zero
- **THEN** the game result is unchanged by that fact and remains driven by buildings and the timer

---

### Requirement: Shell on-destroy reflect
When a Shell block is destroyed, the block-effect system SHALL apply one reflect to the player's health using a named placeholder amount, and SHALL apply one reflect per destroyed Shell so that area destruction chains.

#### Scenario: Destroying a Shell reflects
- **WHEN** a Shell block transitions from present to destroyed
- **THEN** the player's health is reduced by one reflect amount

#### Scenario: Chained reflect from area destruction
- **WHEN** three Shell blocks are destroyed in the same update
- **THEN** the player's health is reduced by three reflect amounts

#### Scenario: Non-shell destruction does not reflect
- **WHEN** a Sand, Coral, Rock, or Jellyfish block is destroyed
- **THEN** the player's health is unchanged

---

### Requirement: Jellyfish placement flicker trigger
When a Jellyfish block is placed, the block-effect system SHALL emit a flicker trigger that downstream HUD rendering can consume. The visual flicker filter itself is out of scope for this capability.

#### Scenario: Placement emits a flicker trigger
- **WHEN** a Jellyfish block is newly placed
- **THEN** the block-effect system records a flicker trigger for that placement

#### Scenario: Other placements do not flicker
- **WHEN** a non-Jellyfish block is placed
- **THEN** no flicker trigger is emitted

---

### Requirement: Drone area expansion
The block-effect system SHALL expand a single center block into the block entities occupying the 3×3 grid neighborhood centered on it (the center plus its eight grid-adjacent positions), including only positions that contain a block.

#### Scenario: Full neighborhood
- **WHEN** the 3×3 neighborhood around a center block is fully occupied by blocks
- **THEN** the expansion returns all nine block entities

#### Scenario: Sparse neighborhood
- **WHEN** only some of the eight surrounding positions contain blocks
- **THEN** the expansion returns the center plus only the occupied neighbors

#### Scenario: Isolated block
- **WHEN** a center block has no occupied neighbors
- **THEN** the expansion returns only the center block
