## Context

After M4 the game is playable but blocks differ only by durability and the weapon counter-matrix. The GDD (`design/gdd.md`) assigns each block a special effect — Coral slows the player within 1.5 cells, Shell reflects damage to the attacker on destruction, Jellyfish disrupts vision (flicker) on placement, and the Drone weapon bombs a 3×3 area. The mascot has no HP and the match is decided only by buildings + the 60s timer, yet the GDD's weapon weaknesses (Sword and Drone are weak vs Shell) only mean something if the player actually takes reflect damage. All effect magnitudes are marked `TBD` and belong to the M7 balancing pass.

M5 follows the project's headless-first rule: the trigger/decision logic is built and unit-tested with no renderer, and only the runtime application that M4 already supports (movement slow, attack target expansion) is wired into the live `PlayerControlState`. Jellyfish's actual HUD post-process filter is a HUD concern and waits for M6.

## Goals / Non-Goals

**Goals:**
- `EffectComponent` marker on blocks + `PlayerHealthComponent` on the player.
- Headless `BlockEffectSystem` with four pure, testable triggers: Coral proximity slow, Shell on-destroy reflect, Jellyfish on-placement flicker, Drone 3×3 AoE expansion.
- Reflect damage reduces `PlayerHealthComponent`, decoupled from win/loss (no death rule in M5).
- Wire Coral slow into fly-cam movement speed and Drone AoE into the attack path in `PlayerControlState`.
- `BlockEffectTest` green; full headless suite green.

**Non-Goals:**
- Jellyfish HUD flicker filter / any post-process rendering (M6).
- Particle/visual FX for Coral, Shell, Drone (M8).
- Tuning any magnitude — slow %, reflect amount, flicker duration, AoE/base damage stay placeholders (M7).
- A player-death or player-HP-based loss condition (not in the GDD).
- Weapon-scaled reflect (GDD hints Sword reflect is larger); M5 uses a flat placeholder, scaling is an M7 tuning detail.

## Decisions

### D1 — `EffectComponent(EffectType)` derived from block type, attached at placement
`EffectType` is `SLOW | REFLECT | FLICKER` (Sand/Rock get no `EffectComponent`). `NpcBuilderSystem` attaches it alongside `BlockComponent` when placing a block, mapping Coral→SLOW, Shell→REFLECT, Jellyfish→FLICKER. Rationale: a marker component lets `BlockEffectSystem` query effect-bearing blocks via Zay-ES `EntitySet`s instead of re-deriving from `blockType`, and keeps the "which blocks have effects" decision in one place. Alternative: switch on `blockType` everywhere — rejected as scattered and harder to query.

### D2 — `PlayerHealthComponent` decoupled from win/loss
A simple `record PlayerHealthComponent(float current, float max)` on the player entity. Shell reflect subtracts a placeholder amount, clamped at 0. `VictorySystem`/`RoundSystem` do **not** read it — the GDD keeps win/loss on buildings + timer. Rationale: faithful to the GDD (the player takes reflect damage and the Shell weakness is real) without inventing a death rule the GDD never specifies. Alternative: no health, reflect as cosmetic signal — rejected because it makes the Sword/Drone-vs-Shell weakness mechanically meaningless.

### D3 — `BlockEffectSystem` is headless and event/state-driven, not renderer-coupled
The system holds Zay-ES `EntitySet`s and exposes pure methods the tests and `PlayerControlState` call:
- `coralSlowFactor(playerPosition)` → smallest movement multiplier among Coral blocks within 1.5 cells (1.0 = no slow).
- `update(tpf)` → detects Shell-block removals since the last frame and applies reflect to `PlayerHealthComponent`; detects newly-placed Jellyfish blocks and emits a flicker trigger.
- `droneAreaTargets(centerBlockId)` → the block entities in the 3×3 grid neighborhood around the center block.
Rationale: pure inputs/outputs are directly unit-testable and keep all decision logic out of the jME states. Alternative: push effects from inside `WeaponSystem`/render code — rejected (untestable, entangles systems).

### D4 — Shell on-destroy detection via tracked block set
Component data is gone once an entity is removed, so `BlockEffectSystem` keeps an `EntityId → BlockType` map populated from its block `EntitySet` while entities are present. On `applyChanges()`, entities that left the set and were SHELL trigger one reflect each. Rationale: a Drone AoE that destroys several Shells then chains naturally (one reflect per removed Shell), matching the GDD's "引爆連鎖反彈". Alternative: have `WeaponSystem` fire a reflect callback on destroy — rejected to avoid coupling and keep `WeaponSystem` unchanged.

### D5 — Runtime wiring stays thin in `PlayerControlState`
Coral: each frame multiply the fly-cam move speed by `coralSlowFactor(playerPos)`. Drone: when the equipped weapon is DRONE, replace the single picked target with `droneAreaTargets(picked)` before `WeaponSystem.attack(...)`. Jellyfish flicker trigger is recorded but not rendered (M6 consumes it). Rationale: the live player exists since M4, so movement slow and AoE are cheap to wire and make M5 visibly playable; the renderer-only flicker is the sole deferred piece.

## Risks / Trade-offs

- **Reflect magnitude / slow % are placeholders** → Mitigation: named constants in `BlockEffectSystem`, tuned in M7; tests assert behavior (health decreased, factor < 1) not exact numbers, so M7 retuning won't churn tests.
- **Tracking removals across frames could miss a same-frame place-and-destroy** → Mitigation: blocks live for at least one BUILD→ATTACK transition before they can be destroyed, so a Shell is always seen present before removal; covered by a test.
- **Coral slow recomputed every frame over all Coral blocks** → Mitigation: block counts are small (≤ a few dozen); a linear scan within 1.5 cells is negligible. Revisit only if profiling in M7 flags it.
- **PlayerHealth with no consumer looks inert** → Accepted for M5: it records reflect damage now so M6 HUD / M7 balancing can surface or gate on it later; documented as intentional.
