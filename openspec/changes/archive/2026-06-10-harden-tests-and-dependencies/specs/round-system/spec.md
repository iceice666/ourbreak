## MODIFIED Requirements

### Requirement: Game state singleton
The system SHALL maintain exactly one game-state entity holding `RoundComponent` (currentRound, maxRounds),
`PhaseComponent` (BUILD | ATTACK), and `GameResultComponent` (IN_PROGRESS | WIN | LOSS). Initialization SHALL be
idempotent for each `RoundSystem` instance.

#### Scenario: Initial state on system start
- **WHEN** the game starts and `RoundSystem` initializes
- **THEN** currentRound = 1, maxRounds = 4, phase = BUILD, result = IN_PROGRESS, remainingSeconds = 60.0

#### Scenario: Repeated initialization
- **WHEN** `initialize()` is called again on an initialized round system
- **THEN** the original game-state entity and all of its current components are preserved

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

## ADDED Requirements

### Requirement: Existing zero-time boundary
An IN_PROGRESS non-final ATTACK round already at zero remaining seconds SHALL advance exactly once on update.

#### Scenario: Advance from an existing zero timer
- **WHEN** a non-final ATTACK round begins an update with zero remaining seconds
- **THEN** the next round starts in BUILD with the timer reset to 60 seconds

#### Scenario: No repeated advancement
- **WHEN** an existing zero timer advances the round to BUILD
- **THEN** subsequent BUILD updates do not advance additional rounds

### Requirement: Required round-system state
Round-system operations SHALL fail explicitly when their game-state entity lacks a required round, phase, or result
component.

#### Scenario: Missing required component
- **WHEN** an initialized game-state entity is missing required state and a round operation needs that state
- **THEN** the operation fails with an invariant error instead of a null dereference
