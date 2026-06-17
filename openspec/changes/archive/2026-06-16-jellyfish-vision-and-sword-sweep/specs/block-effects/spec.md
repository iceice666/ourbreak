## ADDED Requirements

### Requirement: Sword row expansion
The block-effect system SHALL expand a single center block into the block entities occupying a 3-cell horizontal row centered on it along a given grid axis (the center plus its two neighbours one step to each side on that axis at the same height), including only cells that contain a block.

#### Scenario: Full row
- **WHEN** both side cells along the chosen axis are occupied by blocks
- **THEN** the expansion returns all three block entities

#### Scenario: Sparse row
- **WHEN** only one side cell along the axis is occupied
- **THEN** the expansion returns the center plus that one occupied neighbour

#### Scenario: Isolated center
- **WHEN** neither side cell is occupied
- **THEN** the expansion returns only the center block
