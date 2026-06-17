## MODIFIED Requirements

### Requirement: Weapon damage calculation
The weapon system SHALL calculate damage as the selected weapon's base-damage constant multiplied by the counter-matrix multiplier for that weapon and the target block type. Each weapon SHALL have its own base damage: SWORD 1.0, GUN 2.0, DRONE 1.0. The counter multipliers SHALL be strong 2.0, weak 0.5, and neutral 1.0.

#### Scenario: Neutral matchup
- **WHEN** a weapon attacks a block type that is neither strong nor weak against it
- **THEN** damage equals that weapon's base damage multiplied by the neutral multiplier (e.g. GUN vs SAND = 2.0 × 1.0 = 2.0)

#### Scenario: Strong matchup
- **WHEN** a weapon attacks a block type listed as strong for that weapon
- **THEN** damage equals that weapon's base damage multiplied by the strong multiplier (e.g. SWORD vs SAND = 1.0 × 2.0 = 2.0)

#### Scenario: Weak matchup
- **WHEN** a weapon attacks a block type listed as weak for that weapon
- **THEN** damage equals that weapon's base damage multiplied by the weak multiplier (e.g. DRONE vs SHELL = 1.0 × 0.5 = 0.5)

#### Scenario: Base damage differs per weapon
- **WHEN** SWORD, GUN, and DRONE each attack the same neutral-matchup block
- **THEN** the applied damage reflects each weapon's own base damage (Sword 1.0, Gun 2.0, Drone 1.0), not a single shared base
