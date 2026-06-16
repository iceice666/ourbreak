# HUD Spec

## Purpose

The HUD presents in-gameplay status — the round counter (always), the attack countdown and remaining-building count (ATTACK phase only), and the player's current weapon — rendered with Lemur over the gameplay scene. All value formatting is delegated to a pure, unit-testable helper.

---

## Requirements

### Requirement: Round counter
During gameplay the HUD SHALL display the current round and the maximum round in both BUILD and ATTACK phases.

#### Scenario: Round shown
- **WHEN** a match is in round 1 of 4
- **THEN** the HUD shows a round counter reading round 1 of 4

#### Scenario: Round updates across rounds
- **WHEN** the match advances to a later round
- **THEN** the round counter reflects the new current round

---

### Requirement: Attack countdown
The HUD SHALL display the remaining attack time only during the ATTACK phase, formatted as minutes and seconds with the seconds rounded up and never negative, and SHALL hide it outside the ATTACK phase.

#### Scenario: Countdown during attack
- **WHEN** the phase is ATTACK with 60 seconds remaining
- **THEN** the HUD shows a countdown of `1:00`

#### Scenario: Countdown rounds up and clamps
- **WHEN** the phase is ATTACK with 0.4 seconds remaining
- **THEN** the HUD shows `0:01`, and at 0 seconds it shows `0:00` (never negative)

#### Scenario: Hidden during build
- **WHEN** the phase is BUILD
- **THEN** the HUD does not show the attack countdown

---

### Requirement: Remaining building count
The HUD SHALL display the number of remaining block entities only during the ATTACK phase, and SHALL hide it outside the ATTACK phase.

#### Scenario: Building count during attack
- **WHEN** the phase is ATTACK and eight blocks remain
- **THEN** the HUD shows a remaining-building count of 8

#### Scenario: Hidden during build
- **WHEN** the phase is BUILD
- **THEN** the HUD does not show the remaining-building count

---

### Requirement: Current weapon readout
The HUD SHALL display the player's currently equipped weapon in both phases and SHALL update it immediately when the weapon changes.

#### Scenario: Weapon shown
- **WHEN** the player has the SWORD equipped
- **THEN** the HUD shows the current weapon as SWORD

#### Scenario: Updates on switch
- **WHEN** the player switches to a different weapon
- **THEN** the HUD updates to show the newly equipped weapon

---

### Requirement: Pure HUD formatting
The HUD value formatting (round counter text, countdown text, building-count text, weapon text) SHALL be computed by pure functions independent of the renderer so the formatting is unit-testable.

#### Scenario: Deterministic formatting
- **WHEN** the formatting helper is given the same round, remaining-seconds, block-count, and weapon inputs
- **THEN** it returns the same display strings every time, with no jME/render dependency
