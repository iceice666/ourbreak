## ADDED Requirements

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
