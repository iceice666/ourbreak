## ADDED Requirements

### Requirement: Sword attack sweeps a row
When the equipped weapon is SWORD, a left-click attack SHALL expand the block under the crosshair into a 3-cell horizontal row (that block plus the two grid cells one step to each side across the player's view) and apply the weapon attack to every block in that row; SWORD therefore clears a row, while GUN stays single-target and DRONE stays a 3×3 area.

#### Scenario: Sword clears a row
- **WHEN** the player attacks with the SWORD weapon and a block is under the crosshair
- **THEN** the weapon system receives the crosshair block plus its two across-view neighbours as targets

#### Scenario: Sword on an isolated block
- **WHEN** the player sword-attacks a block whose row neighbours are empty
- **THEN** only the crosshair block is hit

#### Scenario: Sword miss
- **WHEN** the player attacks with the SWORD weapon and no block is under the crosshair
- **THEN** no entity is hit
