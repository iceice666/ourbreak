# Menu UI Spec

## Purpose

Menu UI defines the Lemur-rendered front-end screens — the main menu (Start Game / Exit) and the end screen (Win/Lose + Restart) — with clickable controls, replacing the M4 placeholder text. Screen transitions themselves are owned by the app-state-machine; this capability covers their presentation.

---

## Requirements

### Requirement: Main menu controls
The main menu SHALL present Lemur controls with a clickable Start Game button and a clickable Exit button, and clicking them SHALL perform the same actions as the existing screen transitions (start a match / quit).

#### Scenario: Start from the menu
- **WHEN** the player clicks Start Game on the main menu
- **THEN** the menu is dismissed and a gameplay match begins

#### Scenario: Exit from the menu
- **WHEN** the player clicks Exit on the main menu
- **THEN** the application stops

---

### Requirement: End screen controls
The end screen SHALL present a Lemur panel showing the match outcome (Win or Lose) and a clickable Restart button that returns to the main menu.

#### Scenario: Outcome shown
- **WHEN** a match ends in a win
- **THEN** the end screen shows a win outcome; when it ends in a loss it shows a lose outcome

#### Scenario: Restart from the end screen
- **WHEN** the player clicks Restart on the end screen
- **THEN** the end screen is dismissed and the main menu is shown

---

### Requirement: Menu UI lifecycle
Each menu screen's UI SHALL be attached while that screen is active and removed when it is dismissed, leaving no GUI elements behind on the next screen.

#### Scenario: No leftover UI
- **WHEN** a menu or end screen is dismissed
- **THEN** its buttons and panels are removed from the GUI before the next screen is shown
