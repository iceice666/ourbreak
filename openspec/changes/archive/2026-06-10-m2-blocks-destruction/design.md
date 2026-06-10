## Context

M1 uses an empty `BlockComponent` only as a tag so `VictorySystem` can count remaining blocks. M2 must turn that tag into durable game state and add weapon interactions while preserving headless tests and the existing Zay-ES pattern of immutable record components.

The TDD assigns raycasting to `WeaponSystem`, but raycasts require the camera and scene integration that intentionally arrives in M4. M2 therefore needs a boundary that resolves damage after another layer has selected the affected entities.

## Goals / Non-Goals

**Goals:**

- Represent all block and weapon types as typed ECS component data.
- Resolve deterministic weapon damage without jME rendering, physics, or input.
- Enforce ATTACK-phase eligibility in the domain system.
- Preserve compatibility with `VictorySystem` by removing destroyed block entities.
- Keep placeholder balance values named and isolated for replacement in M7.

**Non-Goals:**

- Select targets using sword sweeps, gun raycasts, or drone grid areas.
- Implement block special effects, player health, weapon cooldowns, or ammunition.
- Tune final weapon damage or counter multipliers.
- Change round progression or victory requirements.

## Decisions

### Use enums nested in immutable ECS records

`BlockComponent` will contain `BlockType`, current durability, and maximum durability. `WeaponComponent` will contain `WeaponType`; `MascotComponent` remains an empty marker record. Block and weapon types will be enums colocated with their owning components, matching the existing `PhaseComponent` and `GameResultComponent` style.

Each block type exposes its standard maximum durability: SAND 1, CORAL 2, SHELL 1, ROCK 4, and JELLYFISH 1. A convenience constructor creates a full-health block while the canonical record constructor remains available for ECS updates.

Alternative considered: separate type and health components. That would make queries and updates more complex without a current requirement for independently reusable health.

### Accept selected entity IDs rather than perform targeting

`WeaponSystem` will expose an operation equivalent to:

```java
void attack(EntityId playerId, Collection<EntityId> targetIds)
```

The system reads the player's `WeaponComponent`, resolves damage against each distinct target carrying `BlockComponent`, and writes a replacement component or removes the entity. In M4, input and scene adapters will translate sword, gun, and drone targeting into the same entity-ID collection.

Alternative considered: pass positions or implement weapon-specific geometry in M2. Both duplicate future raycast/grid responsibilities and couple headless damage rules to targeting mechanics.

### Gate attacks through the game-state entity

`WeaponSystem` receives the game-state entity ID and performs no damage unless its `PhaseComponent` is ATTACK. A missing player weapon is treated as an invalid call and rejected; missing, removed, duplicate, and non-block targets are ignored.

The system deduplicates target IDs per attack so overlapping selection results cannot damage one block multiple times during one trigger.

Alternative considered: rely entirely on `PlayerControlState` to suppress BUILD-phase attacks. Keeping the rule in `WeaponSystem` prevents future adapters or tests from bypassing a core game invariant.

### Centralize placeholder damage constants and counter lookup

M2 uses named `float` constants:

- Base damage: `1.0`
- Strong multiplier: `2.0`
- Weak multiplier: `0.5`
- Neutral multiplier: `1.0`

The counter matrix is resolved by `(WeaponType, BlockType)`:

| Weapon | Strong | Weak |
|--------|--------|------|
| SWORD | SAND | SHELL, CORAL |
| GUN | CORAL, JELLYFISH | ROCK |
| DRONE | ROCK, SAND | JELLYFISH, SHELL |

Damage is `baseDamage * multiplier`. Current durability is clamped to zero, and an entity is removed when the result reaches zero.

Alternative considered: store balance values inside components. These are rules shared by every entity, so system constants avoid duplicated mutable configuration and clearly identify M7 tuning points.

### Test through in-memory EntityData

`BlockTest` covers standard durability and overkill clamping. `WeaponTest` uses `DefaultEntityData` to cover neutral, strong, and weak damage; all weapon types; ATTACK-phase gating; duplicate and invalid targets; missing weapons; and entity removal.

Existing `VictorySystemTest` construction sites will use full-health block components. No production dependency is added.

## Risks / Trade-offs

- [Removing the whole entity also removes any unrelated components] → Blocks are modeled as one entity whose lifetime ends on destruction; later destruction effects must observe or be invoked before removal.
- [Floating-point durability can produce precision edge cases] → Clamp results at zero and use tolerance-based assertions; placeholder values are exactly representable binary fractions.
- [A collection of targets does not enforce weapon-specific target counts or shapes] → M4 adapters own selection rules; M2 guarantees only damage resolution and per-trigger deduplication.
- [Destroying a shell will eventually need a pre-removal effect] → M5 may add an explicit destruction notification or effect hook without changing the M2 damage contract.
