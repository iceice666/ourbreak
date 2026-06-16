## MODIFIED Requirements

### Requirement: Round counter
During gameplay the HUD SHALL display the current round number in both BUILD and ATTACK phases. There is no maximum round, so no total is shown.

#### Scenario: Round shown
- **WHEN** a match is in round 1
- **THEN** the HUD shows a round counter reading round 1 (with no maximum)

#### Scenario: Round updates across rounds
- **WHEN** the run advances to a later round
- **THEN** the round counter reflects the new current round, unbounded
