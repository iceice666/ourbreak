# Block Effects Spec

## Purpose

Block effects define the per-block special-effect triggers that give the game tactical depth: an `EffectComponent` marker derived from block type, the Coral proximity slow, the Coral regrowth (the wall heals itself while a Coral lives), the Jellyfish placement flicker trigger, and the Drone 3×3 / Sword 3-cell target expansions. (Shell defends by splitting under the wrong weapon, handled in `WeaponSystem`; there is no player health.) All magnitudes are placeholders tuned in M7.

---
## Requirements
### Requirement: Effect marker assignment
Each placed block SHALL carry an `EffectComponent` whose `effectType` is derived from its block type — Coral→SLOW, Shell→REFLECT, Jellyfish→FLICKER — and Sand and Rock blocks SHALL receive no `EffectComponent`.

#### Scenario: Effect-bearing blocks
- **WHEN** the NPC builder places a Coral, Shell, or Jellyfish block
- **THEN** that block entity has an `EffectComponent` of SLOW, REFLECT, or FLICKER respectively

#### Scenario: Plain blocks
- **WHEN** the NPC builder places a Sand or Rock block
- **THEN** that block entity has no `EffectComponent`

---

### Requirement: Coral proximity slow
During the ATTACK phase, the block-effect system SHALL compute a movement slow factor from Coral blocks within 1.5 grid cells of the player position, returning the strongest applicable slow, and SHALL return no slow (factor 1.0) when no Coral block is in range. The applied slow factor SHALL be 0.5 (movement at half speed while in range).

#### Scenario: Player within range of a Coral block
- **WHEN** the player is within 1.5 grid cells of a Coral block during ATTACK
- **THEN** the computed slow factor is 0.5

#### Scenario: Player outside range
- **WHEN** no Coral block is within 1.5 grid cells of the player
- **THEN** the computed slow factor is exactly 1.0

---

### Requirement: Coral regrowth
During the ATTACK phase the coral-growth system SHALL snapshot the wall's occupied cells once (the footprint) and, every 7 seconds, grow a new Coral block for each living Coral into one adjacent face-neighbour cell that is empty and inside the footprint, reserving each chosen cell so two Corals never grow into the same cell. A Coral with no empty in-footprint neighbour SHALL grow nothing, and no Coral SHALL grow outside the footprint (so regrowth is capped at the original wall).

#### Scenario: Coral heals an adjacent hole
- **WHEN** a footprint cell adjacent to a living Coral is empty at a regrowth tick
- **THEN** a new Coral block is grown into that cell

#### Scenario: Surrounded Coral does nothing
- **WHEN** every face-neighbour of a Coral is occupied
- **THEN** no new Coral is grown for it

#### Scenario: Growth stays inside the footprint
- **WHEN** a Coral's only empty adjacent cell is outside the snapshotted footprint
- **THEN** no new Coral is grown (the wall cannot expand beyond its original cells)

---

### Requirement: Jellyfish placement flicker trigger
When a Jellyfish block is placed, the block-effect system SHALL emit a flicker trigger that downstream HUD rendering can consume. The visual flicker filter itself is out of scope for this capability.

#### Scenario: Placement emits a flicker trigger
- **WHEN** a Jellyfish block is newly placed
- **THEN** the block-effect system records a flicker trigger for that placement

#### Scenario: Other placements do not flicker
- **WHEN** a non-Jellyfish block is placed
- **THEN** no flicker trigger is emitted

---

### Requirement: Drone area expansion
The block-effect system SHALL expand a single center block into the block entities occupying the 3×3 grid neighborhood centered on it (the center plus its eight grid-adjacent positions), including only positions that contain a block.

#### Scenario: Full neighborhood
- **WHEN** the 3×3 neighborhood around a center block is fully occupied by blocks
- **THEN** the expansion returns all nine block entities

#### Scenario: Sparse neighborhood
- **WHEN** only some of the eight surrounding positions contain blocks
- **THEN** the expansion returns the center plus only the occupied neighbors

#### Scenario: Isolated block
- **WHEN** a center block has no occupied neighbors
- **THEN** the expansion returns only the center block

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

