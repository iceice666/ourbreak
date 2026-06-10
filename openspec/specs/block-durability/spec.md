# Block Durability Spec

## Purpose

Block durability defines typed block state, standard durability values, damage updates, and block destruction.

---

## Requirements

### Requirement: Typed block state
The system SHALL represent each block with a `BlockComponent` containing its block type, current durability, and maximum durability.

#### Scenario: Create a full-health block
- **WHEN** a block is created from a block type
- **THEN** its current durability equals its maximum durability

---

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

---

### Requirement: Standard block durability
The system SHALL assign maximum durability of 1 to SAND, 2 to CORAL, 1 to SHELL, 4 to ROCK, and 1 to JELLYFISH.

#### Scenario: Standard durability for every block type
- **WHEN** one full-health block of each block type is created
- **THEN** each block has the maximum durability defined for its type

---

### Requirement: Durability damage and clamping
The system SHALL subtract applied damage from current durability and SHALL clamp the resulting durability to a minimum of zero.

#### Scenario: Nonlethal damage
- **WHEN** a block with durability 4 receives 1 damage
- **THEN** its current durability becomes 3

#### Scenario: Overkill damage
- **WHEN** a block with durability 1 receives damage greater than 1
- **THEN** its resulting durability is zero and never negative

---

### Requirement: Valid damage input
Damage application SHALL accept finite nonnegative values and SHALL reject negative, NaN, or infinite values.

#### Scenario: Zero damage
- **WHEN** zero damage is applied to a block
- **THEN** the returned block preserves its type, current durability, and maximum durability

#### Scenario: Invalid damage
- **WHEN** negative, NaN, or infinite damage is applied
- **THEN** damage application fails without producing modified block state

---

### Requirement: Block destruction
The system SHALL remove a block entity when applied damage reduces its durability to zero.

#### Scenario: Lethal damage removes block entity
- **WHEN** a block receives damage equal to its remaining durability
- **THEN** the block entity no longer exists in `EntityData`

#### Scenario: Nonlethal damage preserves block entity
- **WHEN** a block receives less damage than its remaining durability
- **THEN** the block entity remains and contains its reduced durability
