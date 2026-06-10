## ADDED Requirements

### Requirement: Required component references
Components containing required reference values SHALL reject null values during construction.

#### Scenario: Null enum component value
- **WHEN** a phase, game result, weapon, or block component is constructed with a null enum value
- **THEN** construction fails before the component can be stored in `EntityData`

#### Scenario: Null model identifier
- **WHEN** a model component is constructed with a null model identifier
- **THEN** construction fails before the component can be stored in `EntityData`

### Requirement: Valid model identifier
A `ModelComponent` SHALL require a non-blank model identifier.

#### Scenario: Blank model identifier
- **WHEN** a model component is constructed with an empty or whitespace-only identifier
- **THEN** construction fails

### Requirement: Finite position
A `PositionComponent` SHALL contain only finite X, Y, and Z coordinates.

#### Scenario: Non-finite coordinate
- **WHEN** a position is constructed with NaN or infinity in any coordinate
- **THEN** construction fails

#### Scenario: Vector conversion round trip
- **WHEN** a finite jME vector is converted to a position component and back
- **THEN** the resulting vector contains the original coordinates

### Requirement: Valid round state
A `RoundComponent` SHALL require a positive maximum round, a current round between 1 and the maximum inclusive, and a
finite nonnegative remaining duration.

#### Scenario: Invalid round range
- **WHEN** round state is constructed with a nonpositive maximum, a current round below 1, or a current round above maximum
- **THEN** construction fails

#### Scenario: Invalid remaining duration
- **WHEN** round state is constructed with negative, NaN, or infinite remaining seconds
- **THEN** construction fails

#### Scenario: Boundary round state
- **WHEN** final-round state is constructed with zero remaining seconds
- **THEN** construction succeeds
