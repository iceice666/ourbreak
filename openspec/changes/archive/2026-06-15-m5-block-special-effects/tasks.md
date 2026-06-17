## 1. Components

- [x] 1.1 Add `EffectComponent(EffectType)` with `EffectType { SLOW, REFLECT, FLICKER }` under `ecs/components/`
- [x] 1.2 Add `PlayerHealthComponent(float current, float max)` with validation and a clamped `applyDamage(float)` helper under `ecs/components/`
- [x] 1.3 Add a block-type → effect mapping (Coral→SLOW, Shell→REFLECT, Jellyfish→FLICKER; Sand/Rock→none) used at placement

## 2. Effect assignment

- [x] 2.1 In `NpcBuilderSystem`, attach an `EffectComponent` to each placed block when its type has an effect (no component for Sand/Rock)
- [x] 2.2 In `GameplayState`, give the player entity a `PlayerHealthComponent` at world construction

## 3. BlockEffectSystem (headless)

- [x] 3.1 Create `BlockEffectSystem` holding the needed Zay-ES `EntitySet`s (effect-bearing blocks, positioned blocks) and the player + game-state references
- [x] 3.2 Implement `coralSlowFactor(playerPosition)` → strongest slow among Coral blocks within 1.5 cells during ATTACK, else 1.0 (placeholder slow constant)
- [x] 3.3 Implement Shell on-destroy reflect: track `EntityId → BlockType`, detect removed SHELL blocks on `applyChanges()`, apply one placeholder reflect each to `PlayerHealthComponent`
- [x] 3.4 Implement Jellyfish placement flicker trigger: detect newly-added Jellyfish blocks and record a flicker trigger
- [x] 3.5 Implement `droneAreaTargets(centerBlockId)` → block entities in the 3×3 grid neighborhood (center + occupied neighbors)
- [x] 3.6 Add an `update(tpf)` that advances destroy/placement detection, and a `close()` releasing the entity sets

## 4. Runtime wiring

- [x] 4.1 In `GameplayState`, construct `BlockEffectSystem`, update it each frame, and release it on cleanup
- [x] 4.2 In `PlayerControlState`, scale fly-cam move speed each frame by `coralSlowFactor(playerPosition)`
- [x] 4.3 In `PlayerControlState`, when the weapon is DRONE expand the picked target via `droneAreaTargets(...)` before `WeaponSystem.attack(...)`; keep single-target for SWORD/GUN

## 5. Tests

- [x] 5.1 Add `EffectComponent` / `PlayerHealthComponent` component tests (construction, validation, clamped damage)
- [x] 5.2 Add `BlockEffectTest`: Coral proximity trigger (in-range factor < 1.0, out-of-range = 1.0)
- [x] 5.3 `BlockEffectTest`: Shell reflect trigger (single reflect, chained reflect for multiple, no reflect for non-shell)
- [x] 5.4 `BlockEffectTest`: Jellyfish placement trigger (emitted for Jellyfish, not for others)
- [x] 5.5 `BlockEffectTest`: Drone AoE expansion (full, sparse, isolated neighborhoods)

## 6. Verification

- [x] 6.1 Run `./gradlew test`; the full headless suite incl. `BlockEffectTest` is green (zero failures, zero errors)
- [ ] 6.2 Manual smoke check (deferred, needs display): player slows near Coral, Drone clears a 3×3 area, destroying Shells reduces player health

> Note: 6.2 needs an OpenGL display and was deferred — verify locally in a dev shell. 6.1 passed via a throwaway portable JDK 21 (BUILD SUCCESSFUL, full headless suite green incl. BlockEffectTest).
