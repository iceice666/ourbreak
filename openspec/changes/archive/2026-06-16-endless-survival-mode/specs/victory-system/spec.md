## MODIFIED Requirements

### Requirement: Immediate win on blocks cleared
During ATTACK phase, when no entities tagged with `BlockComponent` exist, the system SHALL treat the round as **survived** and advance the run to the next round (via the round system's advance operation) rather than setting a win. There is no win state in endless survival.

#### Scenario: Survive and advance when all blocks destroyed during attack
- **WHEN** in ATTACK phase and no `BlockComponent` entities exist
- **THEN** the run advances to the next round (currentRound + 1, phase BUILD, timer reset) and the result stays IN_PROGRESS

#### Scenario: Survival takes precedence over simultaneous timer expiry
- **WHEN** the ATTACK timer is zero and no `BlockComponent` entities remain
- **THEN** the round is survived (advance) rather than a game over

#### Scenario: No advance during BUILD phase
- **WHEN** in BUILD phase and no `BlockComponent` entities exist
- **THEN** the round does not advance and the result is unchanged

#### Scenario: No advance when blocks remain
- **WHEN** in ATTACK phase and at least one `BlockComponent` entity exists
- **THEN** the round does not advance

### Requirement: Loss on final round timer expiry with blocks remaining
When any round's ATTACK phase ends (timer == 0) and at least one `BlockComponent` entity still exists, the system SHALL set `GameResultComponent` to LOSS (game over). This applies to every round, not only a final one.

#### Scenario: Game over when the timer expires with blocks present
- **WHEN** the ATTACK timer has reached 0 and at least one `BlockComponent` entity exists
- **THEN** `GameResultComponent` = LOSS regardless of the round number

#### Scenario: No game over while time remains
- **WHEN** the ATTACK timer is above 0 and blocks remain
- **THEN** `GameResultComponent` remains IN_PROGRESS

### Requirement: Idempotent game over
Once `GameResultComponent` is set to LOSS, the system SHALL NOT overwrite it on subsequent updates. (Endless survival has no win state.)

#### Scenario: No double-write after game over
- **WHEN** `GameResultComponent` = LOSS and the system updates again
- **THEN** `GameResultComponent` remains LOSS
