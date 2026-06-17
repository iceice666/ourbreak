## ADDED Requirements

### Requirement: Shell splits under the wrong weapon
When a Shell block is destroyed by a SWORD or DRONE attack, the weapon system SHALL remove that Shell and spawn 2 new Shell blocks in the nearest unoccupied grid cells, each a full Shell carrying the same components (position, block, model, effect marker) as an NPC-placed Shell.

#### Scenario: Sword shatters a Shell into two
- **WHEN** a SWORD attack destroys a Shell block
- **THEN** that Shell is removed and 2 new Shell blocks exist in nearby empty cells (net block count increases by 1)

#### Scenario: Drone shatters each Shell it destroys
- **WHEN** a DRONE area attack destroys two Shell blocks in one volley
- **THEN** each destroyed Shell spawns 2 new Shells (four new Shells total)

#### Scenario: Fragments are full Shells that can split again
- **WHEN** a Shell produced by a previous split is destroyed by a SWORD or DRONE
- **THEN** it splits into 2 more Shells, with no cap on further splitting

### Requirement: Gun destroys a Shell cleanly
When a Shell block is destroyed by a GUN attack, the weapon system SHALL remove it without spawning any new Shells.

#### Scenario: Gun is the clean answer to Shells
- **WHEN** a GUN attack destroys a Shell block
- **THEN** the Shell is removed and no new Shell blocks are spawned

### Requirement: No player health
The game SHALL NOT model player hit points; there is no `PlayerHealthComponent` and Shell does not deal reflect damage. Shell's consequence is the split (more blocks to clear under the survival timer), not player damage.

#### Scenario: Player has no health component
- **WHEN** a match is in progress
- **THEN** the player entity has no health component and destroying a Shell never reduces any player value
