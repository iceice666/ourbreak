# Model View Sync Spec

## Purpose

Model-view synchronization defines renderer-independent attachment, update, removal, and cleanup behavior for renderable
entities.

---

## Requirements

### Requirement: Renderable entity addition
The model-view synchronizer SHALL create and attach exactly one spatial for each entity containing both
`PositionComponent` and `ModelComponent`.

#### Scenario: Add renderable entity
- **WHEN** a matching entity is added and synchronization runs
- **THEN** one spatial with the entity's model identity is attached at the entity's position

#### Scenario: Ignore incomplete entity
- **WHEN** an entity lacks either position or model state
- **THEN** no spatial is attached for that entity

---

### Requirement: Renderable entity updates
The model-view synchronizer SHALL keep attached spatial position and model identity consistent with current component
state.

#### Scenario: Position-only update
- **WHEN** a matching entity's position changes without changing its model
- **THEN** the existing spatial moves to the new position

#### Scenario: Model update
- **WHEN** a matching entity's model identifier changes
- **THEN** the previous spatial is detached and one replacement spatial is attached at the current position

---

### Requirement: Renderable entity removal
The model-view synchronizer SHALL detach and forget a spatial when its entity no longer matches the required component
set or is removed.

#### Scenario: Entity removal
- **WHEN** a rendered entity is removed from `EntityData`
- **THEN** its spatial is detached and no tracked spatial remains for that entity

#### Scenario: Required component removal
- **WHEN** position or model state is removed from a rendered entity
- **THEN** its spatial is detached

---

### Requirement: Model-view cleanup
Cleanup SHALL release the synchronizer's `EntitySet`, detach every spatial it owns, and clear its tracked state.

#### Scenario: Cleanup with active spatials
- **WHEN** cleanup runs after multiple renderable entities were synchronized
- **THEN** all owned spatials are detached and subsequent cleanup performs no additional scene mutation
