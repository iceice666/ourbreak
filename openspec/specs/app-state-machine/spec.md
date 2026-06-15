# App State Machine Spec

## Purpose

The app state machine defines the runtime screen lifecycle — main menu, gameplay, and game end — and how the gameplay screen wires the headless M1–M3 systems to the jME renderer and input, then resolves a match into the end screen and back to the menu.

---

## Requirements

### Requirement: Screen state lifecycle
The application SHALL drive screens through jME `BaseAppState` subclasses managed by the `AppStateManager`, starting at the main menu, and SHALL transition between screens by detaching the current screen state and attaching the next.

#### Scenario: Launch to main menu
- **WHEN** the application starts
- **THEN** the main menu state is the only attached screen state and gameplay is not running

#### Scenario: Start gameplay from the menu
- **WHEN** the player triggers Start Game from the main menu
- **THEN** the main menu state is detached and a gameplay state is attached

#### Scenario: Exit from the menu
- **WHEN** the player triggers Exit from the main menu
- **THEN** the application stops

---

### Requirement: Gameplay world construction
On entering gameplay, the gameplay state SHALL create a fresh `EntityData`, attach a model-view renderer bound to it, initialize the round system, and spawn a mascot entity (with `MascotComponent` and `PositionComponent`) and a player entity (with `WeaponComponent` and `PositionComponent`).

#### Scenario: Fresh world per match
- **WHEN** a gameplay state is entered
- **THEN** it owns a new `EntityData` containing exactly the game-state entity, the mascot, and the player before the first build placement

#### Scenario: Round system initialized
- **WHEN** the gameplay world is constructed
- **THEN** the game-state entity has a round at round 1 of 4, phase BUILD, and result IN_PROGRESS

---

### Requirement: Gameplay system orchestration
Each gameplay frame SHALL update the NPC builder, round, and victory systems so that BUILD-phase placement, phase/timer advancement, and win/loss evaluation run, and the player control state SHALL run as an attached child for input.

#### Scenario: Build then attack progression
- **WHEN** gameplay runs through a BUILD phase
- **THEN** the NPC builder places the round's blocks and the round system transitions the phase to ATTACK

#### Scenario: Attack timer advances
- **WHEN** gameplay frames elapse during an ATTACK phase
- **THEN** the round system decrements the attack timer toward zero

---

### Requirement: Match resolution and end screen
The gameplay state SHALL observe `GameResultComponent` and, when the result becomes WIN or LOSS, SHALL detach gameplay and attach a game-end screen that displays the corresponding outcome.

#### Scenario: Win transition
- **WHEN** the game result becomes WIN
- **THEN** gameplay is detached and the end screen shows a win outcome

#### Scenario: Loss transition
- **WHEN** the game result becomes LOSS
- **THEN** gameplay is detached and the end screen shows a loss outcome

---

### Requirement: Gameplay teardown
On exiting gameplay, the gameplay state SHALL release every system holding an `EntitySet`, close its `EntityData`, and detach its child states so no entities, spatials, or input mappings leak into the next screen.

#### Scenario: Clean teardown on exit
- **WHEN** the gameplay state is detached
- **THEN** the victory and NPC builder entity sets are released, the `EntityData` is closed, and the player control child state is detached

---

### Requirement: Restart to menu
The game-end screen SHALL offer a Restart action that returns to the main menu, and a subsequent Start Game SHALL begin a brand-new match with a fresh world.

#### Scenario: Restart returns to menu
- **WHEN** the player triggers Restart on the end screen
- **THEN** the end screen is detached and the main menu state is attached

#### Scenario: New match after restart
- **WHEN** the player starts gameplay again after a restart
- **THEN** the new gameplay state begins at round 1 with a freshly constructed world and no entities from the prior match
