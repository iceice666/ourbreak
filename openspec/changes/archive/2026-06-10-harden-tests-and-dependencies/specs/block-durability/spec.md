## ADDED Requirements

### Requirement: Valid block durability state
A `BlockComponent` SHALL require a non-null block type, finite nonnegative current durability, finite positive maximum
durability, and current durability no greater than maximum durability.

#### Scenario: Null block type
- **WHEN** block state is constructed with no block type
- **THEN** construction fails

#### Scenario: Invalid current durability
- **WHEN** block state is constructed with negative, NaN, infinite, or above-maximum current durability
- **THEN** construction fails

#### Scenario: Invalid maximum durability
- **WHEN** block state is constructed with zero, negative, NaN, or infinite maximum durability
- **THEN** construction fails

### Requirement: Valid damage input
Damage application SHALL accept finite nonnegative values and SHALL reject negative, NaN, or infinite values.

#### Scenario: Zero damage
- **WHEN** zero damage is applied to a block
- **THEN** the returned block preserves its type, current durability, and maximum durability

#### Scenario: Invalid damage
- **WHEN** negative, NaN, or infinite damage is applied
- **THEN** damage application fails without producing modified block state
