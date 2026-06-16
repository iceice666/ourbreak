## MODIFIED Requirements

### Requirement: Weapon damage calculation
The weapon system SHALL calculate damage as the selected weapon's base-damage constant multiplied by the counter-matrix multiplier for that weapon and the target block type. Each weapon SHALL have its own base damage: SWORD 1.0, GUN 8.0, DRONE 1.0. The counter multipliers SHALL be strong 2.0, weak 0.5, and neutral 1.0. The Gun's base is high enough to one-shot any single block (including a Rock at its weak multiplier) — its weakness is having no area effect. **Shell is exempt from this damage model**: it is governed by the `shell-splitting` capability (Sword/Drone split it, Gun destroys it cleanly).

#### Scenario: Neutral matchup
- **WHEN** a non-Shell block type that is neither strong nor weak against the weapon is attacked
- **THEN** damage equals that weapon's base damage multiplied by the neutral multiplier (e.g. GUN vs SAND = 8.0 × 1.0)

#### Scenario: Strong matchup
- **WHEN** a non-Shell block type listed as strong for that weapon is attacked
- **THEN** damage equals that weapon's base damage multiplied by the strong multiplier (e.g. SWORD vs SAND = 1.0 × 2.0)

#### Scenario: Weak matchup
- **WHEN** a non-Shell block type listed as weak for that weapon is attacked
- **THEN** damage equals that weapon's base damage multiplied by the weak multiplier (e.g. DRONE vs ROCK... uses STRONG; GUN vs ROCK = 8.0 × 0.5 = 4.0, one-shotting a durability-4 Rock)

#### Scenario: Gun one-shots any single block
- **WHEN** the GUN attacks any single non-Shell block
- **THEN** the block is destroyed in one hit (the Gun's base damage exceeds every block's durability at any multiplier)
