## MODIFIED Requirements

### Requirement: Game state singleton
The system SHALL maintain exactly one game-state entity holding `RoundComponent` (currentRound, remainingSeconds — no maximum-round cap), `PhaseComponent` (BUILD | ATTACK), and `GameResultComponent` (IN_PROGRESS | LOSS). Initialization SHALL be idempotent for each `RoundSystem` instance.

#### Scenario: Initial state on system start
- **WHEN** the game starts and `RoundSystem` initializes
- **THEN** currentRound = 1, phase = BUILD, result = IN_PROGRESS, remainingSeconds = 60.0

#### Scenario: Repeated initialization
- **WHEN** `initialize()` is called again on an initialized round system
- **THEN** the original game-state entity and all of its current components are preserved

## ADDED Requirements

### Requirement: Round advancement on survival
The system SHALL expose an operation that advances the run to the next round — incrementing `currentRound` by 1 and entering BUILD phase with the timer reset to 60 seconds — and SHALL do so only while the game is IN_PROGRESS. There SHALL be no upper bound on the round number.

#### Scenario: Advance after surviving a round
- **WHEN** the current round's wall has been cleared and the round is advanced
- **THEN** currentRound increases by 1, phase = BUILD, remainingSeconds = 60.0, result = IN_PROGRESS

#### Scenario: Rounds are unbounded
- **WHEN** the run has already advanced past round 4
- **THEN** the round continues to increment (5, 6, …) with no maximum

#### Scenario: No advance after game over
- **WHEN** the result is LOSS and an advance is requested
- **THEN** round, phase, and timer state remain unchanged

## REMOVED Requirements

### Requirement: Round advancement on timer expiry
**Reason**: In endless survival the attack timer no longer advances the round — surviving (clearing the wall) advances it, and a timer expiry with blocks remaining ends the run instead.
**Migration**: Round advancement now occurs via the survival path (see `victory-system`'s round-survival requirement calling the round system's advance operation).

### Requirement: Existing zero-time boundary
**Reason**: With no timer-driven advancement, the auto-advance-from-zero boundary no longer applies; a zero timer with blocks remaining is now a game over.
**Migration**: Game-over-on-timeout is handled by the survival/victory system.

### Requirement: No duplicate game result write
**Reason**: The round system no longer writes `GameResultComponent` at all (the survival/victory system owns win/loss), so a "don't overwrite the result" guard here is obsolete.
**Migration**: Result idempotency now lives in the survival/victory system.
