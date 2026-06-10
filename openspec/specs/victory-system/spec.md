# Victory System Spec

## Purpose

The `VictorySystem` monitors game-state and block presence each update tick to determine when the game is won or lost, writing the outcome to `GameResultComponent` exactly once.

---

## Requirements

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

---

### Requirement: Loss on final round timer expiry with blocks remaining
When the final round's ATTACK phase ends (timer == 0, currentRound == maxRounds) and at least one `BlockComponent` entity still exists, the system SHALL set `GameResultComponent` to LOSS.

#### Scenario: Loss at end of final round with blocks present
- **WHEN** currentRound = 4 = maxRounds, ATTACK phase timer has reached 0, and at least one `BlockComponent` entity exists
- **THEN** `GameResultComponent` = LOSS

#### Scenario: No loss on non-final round timer expiry
- **WHEN** currentRound = 2, maxRounds = 4, and the ATTACK timer expires
- **THEN** `GameResultComponent` remains IN_PROGRESS

---

### Requirement: Idempotent outcome
Once `GameResultComponent` is set to WIN or LOSS, the system SHALL NOT overwrite it on subsequent updates.

#### Scenario: No double-write after win
- **WHEN** `GameResultComponent` = WIN and the system updates again
- **THEN** `GameResultComponent` remains WIN

#### Scenario: No double-write after loss
- **WHEN** `GameResultComponent` = LOSS and the system updates again
- **THEN** `GameResultComponent` remains LOSS
