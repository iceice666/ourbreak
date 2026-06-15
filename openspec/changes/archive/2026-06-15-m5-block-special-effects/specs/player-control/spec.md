## ADDED Requirements

### Requirement: Movement honors active slow
While gameplay is active, the player control state SHALL scale first-person movement speed by the current Coral slow factor from the block-effect system, so that the player moves slower while within range of a Coral block and at normal speed otherwise.

#### Scenario: Slowed near Coral
- **WHEN** the block-effect system reports a slow factor below 1.0 for the player position
- **THEN** the player's movement speed is reduced by that factor

#### Scenario: Normal speed away from Coral
- **WHEN** the block-effect system reports a slow factor of 1.0
- **THEN** the player moves at the unmodified base speed

---

### Requirement: Drone attack hits a 3x3 area
When the equipped weapon is DRONE, a left-click attack SHALL expand the block under the crosshair into its 3×3 grid neighborhood via the block-effect system and apply the weapon attack to every block in that area; for SWORD and GUN the attack SHALL target only the single block under the crosshair.

#### Scenario: Drone area attack
- **WHEN** the player attacks with the DRONE weapon and a block is under the crosshair
- **THEN** the weapon system receives the 3×3 neighborhood of blocks around that block as targets

#### Scenario: Non-drone single target
- **WHEN** the player attacks with the SWORD or GUN weapon
- **THEN** the weapon system receives only the single block under the crosshair

#### Scenario: Drone miss
- **WHEN** the player attacks with the DRONE weapon and no block is under the crosshair
- **THEN** no entity takes damage
