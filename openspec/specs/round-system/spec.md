# Round System Spec

## Purpose

The `RoundSystem` manages the game-state entity that tracks round progression, phase transitions (BUILD / ATTACK), and the attack-phase countdown timer. It drives the core game loop from round 1 through the final round.

---

## Requirements

### Requirement: Game state singleton
The system SHALL maintain exactly one game-state entity holding `RoundComponent` (currentRound, remainingSeconds — no
maximum-round cap), `PhaseComponent` (BUILD | ATTACK), and `GameResultComponent` (IN_PROGRESS | LOSS). Initialization
SHALL be idempotent for each `RoundSystem` instance.

#### Scenario: Initial state on system start
- **WHEN** the game starts and `RoundSystem` initializes
- **THEN** currentRound = 1, phase = BUILD, result = IN_PROGRESS, remainingSeconds = 60.0

#### Scenario: Repeated initialization
- **WHEN** `initialize()` is called again on an initialized round system
- **THEN** the original game-state entity and all of its current components are preserved

---

### Requirement: Phase transition to ATTACK
The system SHALL transition from BUILD to ATTACK phase only while the game is IN_PROGRESS, resetting the attack timer to
the full duration of 60 seconds. Signals received outside an active BUILD phase SHALL be no-ops.

#### Scenario: BUILD to ATTACK transition
- **WHEN** the active BUILD phase is signaled to end while the game is IN_PROGRESS
- **THEN** `PhaseComponent` changes to ATTACK and `remainingSeconds` is set to 60.0

#### Scenario: Repeated signal during ATTACK
- **WHEN** ATTACK is already active and the attack timer has partially elapsed
- **THEN** another attack-phase signal does not reset the timer

#### Scenario: Signal after game completion
- **WHEN** the result is WIN or LOSS and the BUILD phase is signaled to end
- **THEN** phase and timer state remain unchanged

---

### Requirement: Attack timer countdown
During ATTACK phase, the system SHALL decrement `remainingSeconds` by finite nonnegative elapsed time (`tpf`) each update
tick, clamped to a minimum of zero. Invalid elapsed time SHALL be rejected without changing game state.

#### Scenario: Timer counts down normally
- **WHEN** in ATTACK phase with `remainingSeconds = 60.0` and `update(30.0)` is called
- **THEN** `remainingSeconds == 30.0`

#### Scenario: Timer clamps to zero
- **WHEN** `remainingSeconds = 10.0` and `update(15.0)` is called
- **THEN** `remainingSeconds == 0.0` and is not negative

#### Scenario: Timer does not decrement in BUILD phase
- **WHEN** in BUILD phase and `update(30.0)` is called
- **THEN** `remainingSeconds` is unchanged

#### Scenario: Invalid elapsed time
- **WHEN** `update()` receives negative, NaN, or infinite elapsed time
- **THEN** the call fails and round, phase, timer, and result state remain unchanged

---

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

---

### Requirement: Required round-system state
Round-system operations SHALL fail explicitly when their game-state entity lacks a required round, phase, or result
component.

#### Scenario: Missing required component
- **WHEN** an initialized game-state entity is missing required state and a round operation needs that state
- **THEN** the operation fails with an invariant error instead of a null dereference
