## MODIFIED Requirements

### Requirement: Coral proximity slow
During the ATTACK phase, the block-effect system SHALL compute a movement slow factor from Coral blocks within 1.5 grid cells of the player position, returning the strongest applicable slow, and SHALL return no slow (factor 1.0) when no Coral block is in range. The applied slow factor SHALL be 0.5 (movement at half speed while in range).

#### Scenario: Player within range of a Coral block
- **WHEN** the player is within 1.5 grid cells of a Coral block during ATTACK
- **THEN** the computed slow factor is 0.5

#### Scenario: Player outside range
- **WHEN** no Coral block is within 1.5 grid cells of the player
- **THEN** the computed slow factor is exactly 1.0

### Requirement: Shell on-destroy reflect
When a Shell block is destroyed, the block-effect system SHALL apply one reflect of 20 damage to the player's health, and SHALL apply one reflect per destroyed Shell so that area destruction chains.

#### Scenario: Destroying a Shell reflects
- **WHEN** a Shell block transitions from present to destroyed
- **THEN** the player's health is reduced by 20

#### Scenario: Chained reflect from area destruction
- **WHEN** three Shell blocks are destroyed in the same update
- **THEN** the player's health is reduced by 60 (three reflects of 20)

#### Scenario: Non-shell destruction does not reflect
- **WHEN** a Sand, Coral, Rock, or Jellyfish block is destroyed
- **THEN** the player's health is unchanged
