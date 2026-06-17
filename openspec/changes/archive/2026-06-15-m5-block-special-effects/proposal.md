## Why

M4 made the game playable, but every block behaves identically — durability is the only difference. The GDD's tactical depth comes from per-block special effects (Coral slows, Shell reflects, Jellyfish disrupts vision, Drone bombs an area), which is also what makes the weapon counter-matrix meaningful. M5 adds those effects as headless-tested trigger logic, with the one runtime piece that M4 already supports (Coral movement slow) wired in now.

## What Changes

- Add `EffectComponent` — an `effectType` marker placed on block entities, derived from their block type (Coral→SLOW, Shell→REFLECT, Jellyfish→FLICKER; Sand/Rock→none).
- Add `PlayerHealthComponent` — player hit points that Shell reflect reduces. Per the GDD, the player (attacker) takes reflect damage, but the match is still decided only by buildings + the round timer; M5 adds **no** player-death/loss rule and leaves the reflect magnitude as a placeholder (tuned in M7).
- Add a headless `BlockEffectSystem` implementing the four effect triggers:
  - **Coral**: during ATTACK, detect the player within 1.5 grid cells of a Coral block → produce a movement slow (placeholder %, tuned M7).
  - **Shell**: on destruction of a Shell block → deal reflect damage to `PlayerHealthComponent` (placeholder amount, tuned M7). Drone AoE destroying several Shells naturally chains.
  - **Jellyfish**: on placement of a Jellyfish block → emit a flicker trigger/marker (the actual HUD post-process filter is deferred to M6).
  - **Drone**: expand a single picked block into the 3×3 grid neighborhood of block entities for area damage.
- Wire the runtime pieces M4 already supports into `PlayerControlState`: movement speed honors an active Coral slow, and a DRONE-weapon attack expands the picked target via the 3×3 AoE before calling `WeaponSystem`.
- Add `BlockEffectTest` covering the Coral proximity trigger, Shell reflect trigger, Jellyfish placement trigger, and Drone AoE expansion. Headless suite stays green.

## Capabilities

### New Capabilities

- `block-effects`: Defines the per-block special-effect triggers (Coral proximity slow, Shell on-destroy reflect, Jellyfish on-placement flicker, Drone 3×3 AoE expansion), the `EffectComponent` marker, and the `PlayerHealthComponent` reflect-damage model decoupled from win/loss.

### Modified Capabilities

- `player-control`: Adds two consumer behaviors — first-person movement is reduced while a Coral slow is active, and a DRONE-weapon attack hits the 3×3 area around the crosshair block instead of a single block.

## Impact

- Adds `EffectComponent` and `PlayerHealthComponent` under `ecs/components/`, and `BlockEffectSystem` under `ecs/systems/`.
- `NpcBuilderSystem` attaches an `EffectComponent` to each placed block (and `GameplayState` gives the player a `PlayerHealthComponent`).
- `PlayerControlState` consumes `BlockEffectSystem` for the Coral slow factor and Drone AoE expansion; `GameplayState` runs `BlockEffectSystem` each frame.
- Consumes the existing `BlockComponent`, `PositionComponent`, `WeaponComponent`, and `WeaponSystem` without changing their APIs.
- Adds no Gradle dependencies. Jellyfish's HUD flicker filter and all `TBD` magnitudes are out of scope (M6 / M7). Adds `BlockEffectTest`.
