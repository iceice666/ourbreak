# jellyfish-poison Specification

## Purpose
TBD - created by archiving change jellyfish-vision-and-sword-sweep. Update Purpose after archive.
## Requirements
### Requirement: Drone-killed Jellyfish poisons the player
When a Jellyfish block is destroyed while the player's equipped weapon is the DRONE, the game SHALL add 5 seconds of poison to the player, capped at a maximum of 10 seconds. Destroying a Jellyfish with the GUN or the SWORD SHALL NOT add poison. Poison SHALL drain over real time and reach zero on its own.

#### Scenario: Droning a Jellyfish adds poison
- **WHEN** a Jellyfish block is destroyed while the DRONE is equipped
- **THEN** the player's poison timer increases by 5 seconds, not exceeding 10 seconds total

#### Scenario: Gun or Sword kills do not poison
- **WHEN** a Jellyfish block is destroyed while the GUN or SWORD is equipped
- **THEN** the player's poison timer is unchanged

#### Scenario: Poison drains to zero
- **WHEN** the player is poisoned and time passes with no further Jellyfish drone-kills
- **THEN** the poison timer decreases each frame until it reaches zero

### Requirement: Poison hallucination recolours real blocks
While the player's poison timer is above zero, regardless of the currently equipped weapon, the game SHALL recolour every real block in the scene to random rapidly-changing rainbow colours so that block types are visually indistinguishable. When the poison timer reaches zero, the game SHALL restore every block to its real per-type colour.

#### Scenario: Blocks flicker rainbow while poisoned
- **WHEN** the poison timer is above zero
- **THEN** the real blocks display random shifting rainbow colours, hiding their type, no matter which weapon is held

#### Scenario: Real colours restored when poison ends
- **WHEN** the poison timer reaches zero
- **THEN** every block returns to its correct per-type colour

### Requirement: Draining poison bar UI
While the player is poisoned, the game SHALL display a bar whose fill is proportional to the remaining poison (full at the 10-second cap), draining toward empty as the poison decreases, and hidden when the poison timer is zero.

#### Scenario: Bar reflects remaining poison
- **WHEN** the poison timer is above zero
- **THEN** a bar is shown whose fill fraction equals the remaining poison divided by the 10-second cap

#### Scenario: Bar hidden when not poisoned
- **WHEN** the poison timer is zero
- **THEN** no poison bar is shown

