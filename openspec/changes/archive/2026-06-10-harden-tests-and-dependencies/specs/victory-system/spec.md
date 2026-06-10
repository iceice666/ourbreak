## MODIFIED Requirements

### Requirement: Immediate win on blocks cleared
During ATTACK phase, when no entities tagged with `BlockComponent` exist, the system SHALL set `GameResultComponent` to
WIN. Block clearance SHALL take precedence over final-round timer expiry evaluated in the same update.

#### Scenario: Win when all blocks destroyed during attack
- **WHEN** in ATTACK phase and no `BlockComponent` entities exist
- **THEN** `GameResultComponent` = WIN

#### Scenario: Win on simultaneous clearance and expiry
- **WHEN** the final ATTACK round timer is zero and no `BlockComponent` entities remain
- **THEN** `GameResultComponent` = WIN rather than LOSS

#### Scenario: No win during BUILD phase
- **WHEN** in BUILD phase and no `BlockComponent` entities exist
- **THEN** `GameResultComponent` is unchanged

#### Scenario: No win when blocks remain
- **WHEN** in ATTACK phase and at least one `BlockComponent` entity exists
- **THEN** `GameResultComponent` is not set to WIN
