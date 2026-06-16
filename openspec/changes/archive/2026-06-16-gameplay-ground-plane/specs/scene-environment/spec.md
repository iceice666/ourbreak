## ADDED Requirements

### Requirement: Gameplay ground plane
While gameplay is active, the scene SHALL render a horizontal ground plane beneath the play area at the block base level, visually distinct from the background, with a grid overlay that provides motion and standing-on-ground cues.

#### Scenario: Ground visible during gameplay
- **WHEN** a match is running
- **THEN** a horizontal ground plane with a grid is rendered under the blocks, distinct from the background colour

#### Scenario: Ground aligned to the play area
- **WHEN** the ground is rendered
- **THEN** its surface sits at the block base level so blocks rest on it rather than floating above or sinking below

---

### Requirement: Ground lifecycle bound to gameplay
The ground plane SHALL exist only while gameplay is the active screen and SHALL be removed from the scene when gameplay exits, leaving no environment geometry behind.

#### Scenario: Removed on exit
- **WHEN** the gameplay state is detached
- **THEN** the ground plane and grid are removed from the scene

#### Scenario: Absent outside gameplay
- **WHEN** the main menu or end screen is showing
- **THEN** no gameplay ground plane is rendered
