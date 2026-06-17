# Player Control Spec

## Purpose

Player control defines first-person input during an active match — movement, mouse-look, weapon switching across the three weapon types, and the attack trigger — and how an attack resolves a target block via a camera ray and applies it through the weapon system.

---
## Requirements
### Requirement: First-person movement and look
While gameplay is active, the player control state SHALL map WASD to horizontal movement of the player viewpoint and SHALL capture the mouse for first-person look, and it SHALL release that capture when gameplay is not the active screen.

#### Scenario: Move with WASD
- **WHEN** the player holds a movement key during gameplay
- **THEN** the camera/player viewpoint translates in the corresponding direction

#### Scenario: Look with the mouse
- **WHEN** the player moves the mouse during gameplay
- **THEN** the camera orientation rotates to follow it

#### Scenario: Release capture off gameplay
- **WHEN** the player control state is disabled or detached
- **THEN** mouse capture is released so menu and end screens are usable

---

### Requirement: Weapon switching
The player control state SHALL bind the 1, 2, and 3 keys to selecting SWORD, GUN, and DRONE respectively by writing the chosen `WeaponType` to the player entity's `WeaponComponent`.

#### Scenario: Select sword
- **WHEN** the player presses 1
- **THEN** the player entity's `WeaponComponent` weapon type is SWORD

#### Scenario: Select gun
- **WHEN** the player presses 2
- **THEN** the player entity's `WeaponComponent` weapon type is GUN

#### Scenario: Select drone
- **WHEN** the player presses 3
- **THEN** the player entity's `WeaponComponent` weapon type is DRONE

---

### Requirement: Attack targeting via camera ray
On a left-click attack, the player control state SHALL cast a ray from the camera, resolve the nearest block entity under the crosshair from the rendered scene, and invoke the weapon system's attack with the player and that target; when no block is under the crosshair it SHALL invoke no damage.

#### Scenario: Hit a block
- **WHEN** the player left-clicks with a block entity under the crosshair during the ATTACK phase
- **THEN** the weapon system applies that weapon's damage to the targeted block's durability

#### Scenario: Destroy a block
- **WHEN** an attack reduces a targeted block's durability to zero
- **THEN** the block entity is removed and the model-view synchronizer removes its spatial

#### Scenario: Miss
- **WHEN** the player left-clicks with no block under the crosshair
- **THEN** no entity takes damage

#### Scenario: Attack gated to ATTACK phase
- **WHEN** the player left-clicks a block during the BUILD phase
- **THEN** the weapon system applies no damage

---

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

