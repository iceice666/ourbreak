## ADDED Requirements

### Requirement: Typed player weapon
The system SHALL represent the player's selected weapon with a `WeaponComponent` containing SWORD, GUN, or DRONE.

#### Scenario: Read selected weapon
- **WHEN** a player entity has a `WeaponComponent` for GUN
- **THEN** the weapon system resolves that player's attack as a GUN attack

### Requirement: Attack-phase eligibility
The weapon system SHALL apply damage only while the game-state entity is in the ATTACK phase.

#### Scenario: Attack during ATTACK phase
- **WHEN** a player with a weapon attacks a block while the phase is ATTACK
- **THEN** weapon damage is applied to the block

#### Scenario: Attack during BUILD phase
- **WHEN** a player with a weapon attacks a block while the phase is BUILD
- **THEN** the block durability is unchanged

### Requirement: Weapon damage calculation
The weapon system SHALL calculate damage as the named base-damage constant multiplied by the counter-matrix multiplier for the selected weapon and target block type.

#### Scenario: Neutral matchup
- **WHEN** a weapon attacks a block type that is neither strong nor weak against it
- **THEN** damage equals base damage multiplied by the neutral multiplier

#### Scenario: Strong matchup
- **WHEN** a weapon attacks a block type listed as strong for that weapon
- **THEN** damage equals base damage multiplied by the strong multiplier

#### Scenario: Weak matchup
- **WHEN** a weapon attacks a block type listed as weak for that weapon
- **THEN** damage equals base damage multiplied by the weak multiplier

### Requirement: Weapon counter matrix
The weapon system SHALL treat SWORD as strong against SAND and weak against SHELL and CORAL; GUN as strong against CORAL and JELLYFISH and weak against ROCK; and DRONE as strong against ROCK and SAND and weak against JELLYFISH and SHELL.

#### Scenario: Every declared counter uses its assigned multiplier
- **WHEN** each weapon attacks each block type in its strong and weak sets
- **THEN** every strong pair uses the strong multiplier and every weak pair uses the weak multiplier

### Requirement: Preselected target processing
The weapon system SHALL accept entity IDs selected by an external targeting adapter and SHALL process each distinct block target at most once per attack.

#### Scenario: Multiple distinct blocks
- **WHEN** one attack supplies multiple distinct block entity IDs
- **THEN** each supplied block receives one damage application

#### Scenario: Duplicate block target
- **WHEN** one attack supplies the same block entity ID more than once
- **THEN** that block receives exactly one damage application

#### Scenario: Invalid targets
- **WHEN** an attack contains a missing entity or an entity without `BlockComponent`
- **THEN** the invalid target is ignored and other valid block targets are still processed

### Requirement: Weapon required for attack
The weapon system SHALL reject an attack when the supplied player entity has no `WeaponComponent`.

#### Scenario: Player has no selected weapon
- **WHEN** an attack is requested for a player entity without `WeaponComponent`
- **THEN** the attack fails without modifying any target blocks
