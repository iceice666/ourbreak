# Weapon Damage Spec

## Purpose

Weapon damage defines typed player weapons, attack eligibility, counter-based damage, and preselected target processing.

---

## Requirements

### Requirement: Typed player weapon
The system SHALL represent the player's selected weapon with a `WeaponComponent` containing SWORD, GUN, or DRONE.

#### Scenario: Read selected weapon
- **WHEN** a player entity has a `WeaponComponent` for GUN
- **THEN** the weapon system resolves that player's attack as a GUN attack

---

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

---

### Requirement: Weapon damage calculation
The weapon system SHALL calculate damage as the selected weapon's base-damage constant multiplied by the counter-matrix multiplier for that weapon and the target block type. Each weapon SHALL have its own base damage: SWORD 1.0, GUN 8.0, DRONE 1.0. The counter multipliers SHALL be strong 2.0, weak 0.5, and neutral 1.0. The Gun's base is high enough to one-shot any single block (including a Rock at its weak multiplier); its weakness is having no area effect. **Shell is exempt from this damage model** — it is governed by the `shell-splitting` capability (Sword/Drone split it, Gun destroys it cleanly).

#### Scenario: Neutral matchup
- **WHEN** a non-Shell block type that is neither strong nor weak against the weapon is attacked
- **THEN** damage equals that weapon's base damage multiplied by the neutral multiplier (e.g. SAND vs DRONE uses STRONG; GUN base is 8.0)

#### Scenario: Strong matchup
- **WHEN** a non-Shell block type listed as strong for that weapon is attacked
- **THEN** damage equals that weapon's base damage multiplied by the strong multiplier (e.g. SWORD vs SAND = 1.0 × 2.0 = 2.0)

#### Scenario: Weak matchup
- **WHEN** a non-Shell block type listed as weak for that weapon is attacked
- **THEN** damage equals that weapon's base damage multiplied by the weak multiplier (e.g. GUN vs ROCK = 8.0 × 0.5 = 4.0, one-shotting a durability-4 Rock)

#### Scenario: Gun one-shots any single block
- **WHEN** the GUN attacks any single non-Shell block
- **THEN** the block is destroyed in one hit

---

### Requirement: Weapon counter matrix
The weapon system SHALL treat SWORD as strong against SAND and weak against SHELL and CORAL; GUN as strong against CORAL and JELLYFISH and weak against ROCK; and DRONE as strong against ROCK and SAND and weak against JELLYFISH and SHELL.

#### Scenario: Every declared counter uses its assigned multiplier
- **WHEN** each weapon attacks each block type in its strong and weak sets
- **THEN** every strong pair uses the strong multiplier and every weak pair uses the weak multiplier

---

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

---

### Requirement: Weapon required for attack
The weapon system SHALL reject an attack when the supplied player entity has no `WeaponComponent`.

#### Scenario: Player has no selected weapon
- **WHEN** an attack is requested for a player entity without `WeaponComponent`
- **THEN** the attack fails without modifying any target blocks

---

### Requirement: Required weapon-system game state
The weapon system SHALL require phase and result components on its game-state entity before evaluating attack eligibility.

#### Scenario: Missing phase or result
- **WHEN** an attack is requested while required game-state eligibility data is absent
- **THEN** the request fails with an invariant error before any target is modified
