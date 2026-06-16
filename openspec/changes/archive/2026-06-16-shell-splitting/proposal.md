## Why

Two balance problems: (1) Shell's "reflect damage to the player" drains a player-health bar that has no consequence (depleting it does nothing), so the Shell counter is invisible; (2) the Drone is a no-brainer — its 3×3 AoE clears any wall with no real downside. Reworking Shell solves both: a Shell hit by the wrong weapon **shatters into more Shells**, so brainlessly Drone-bombing a wall full of Shells multiplies your workload and burns the survival clock. The right answer is to pick Shells off with the Gun.

## What Changes

- **Remove player health entirely.** Delete `PlayerHealthComponent` and the Shell reflect-damage model; the player has no HP (consistent with the GDD's "mascot/player has no HP").
- **Shell splitting.** When a Shell is destroyed by a **Sword or Drone** (the weapons the counter-matrix marks weak vs Shell), it does not simply die — it **splits into 2 new Shells** placed in nearby empty cells. The fragments are full Shells and can split again, with **no cap** (intentional: this is what punishes mindless Drone use). A Shell hit by the **Gun** is destroyed cleanly with no split.
- This makes the Gun the correct tool for Shells and gives the Drone a real, felt weakness — you cannot just AoE a Shell wall.
- **Gun rework**: raise `GUN_BASE_DAMAGE` 2.0 → 8.0 so the Gun one-shots any single block (including a durability-4 Rock). The Gun's identity becomes single-target burst (rocks, shells, precision); its weakness is no AoE. This is what gives the player a reason to pick the Gun.

## Capabilities

### New Capabilities

- `shell-splitting`: Defines that a Shell destroyed by Sword or Drone splits into 2 new Shells (uncapped), while the Gun destroys it without splitting.

### Modified Capabilities

- `block-effects`: Removes the player-reflect-health and Shell-on-destroy-reflect requirements — there is no player health and Shell's behaviour is now the split mechanic.
- `weapon-damage`: Gun base damage 2.0 → 8.0 (one-shots any single block); Shell is exempt from the damage model (handled by `shell-splitting`).

## Impact

- Deletes `PlayerHealthComponent` (+ its test) and `GameplayState`'s player-health wiring; removes the reflect/health constants and logic from `BlockEffectSystem`; retunes/removes `BlockEffectTest`'s reflect cases.
- `WeaponSystem` gains the Shell-split behaviour: a Sword/Drone hit that would destroy a Shell instead removes it and spawns 2 Shells in the nearest empty cells (needs an occupancy view of positioned blocks). Adds split tests.
- Updates `design/gdd.md` / `design/tdd.md` (Shell effect, no player HP).
- No new dependencies. `EffectComponent` keeps its SHELL marker (now meaning "splits"); the counter-matrix is unchanged (Sword/Drone weak vs Shell now expresses "triggers split").
