# Devlog — 2026-06-15 22:52:14 — `m5-block-special-effects`

> **Author**: ceil
> **Build / Version**: M5 — block special effects
> **Branch / Commit**: main

---

## Summary

Added the four GDD block special effects as headless-tested trigger logic, plus
the runtime pieces M4 already supports (Coral movement slow, Drone 3×3 attack).
Planned and tracked through OpenSpec (`m5-block-special-effects`, now archived).

---

## Goals for this session

- [x] Plan M5 with OpenSpec (proposal → design → specs → tasks)
- [x] Implement `EffectComponent`, `PlayerHealthComponent`, `BlockEffectSystem`
- [x] Wire Coral slow + Drone AoE into the live player; keep the suite green

---

## What I worked on

### Feature / System: `block-effects`

- `EffectComponent(EffectType{SLOW,REFLECT,FLICKER})` derived from block type via
  `forBlockType` and attached by `NpcBuilderSystem` at placement (Sand/Rock none).
- `PlayerHealthComponent(current,max)` on the player, reduced by Shell reflect.
- `BlockEffectSystem` (headless) exposing pure, tested methods:
  - `coralSlowFactor(pos)` — strongest slow among Coral blocks within 1.5 cells
    during ATTACK, else 1.0.
  - `update(tpf)` — detects Shell-block removals → reflect (one per Shell, so
    Drone area kills chain), and newly-placed Jellyfish → flicker trigger.
  - `droneAreaTargets(center)` — block entities in the 3×3 grid neighborhood.

### Feature / System: `player-control` (additions)

- Movement speed each frame scales by `coralSlowFactor(cameraPosition)`.
- A DRONE-weapon attack expands the picked target into its 3×3 neighborhood
  before `WeaponSystem.attack(...)`; SWORD/GUN stay single-target.

---

## Technical notes

- Shell on-destroy detection keeps an `EntityId → BlockType` map so a removed
  entity's type is known after its components are gone; seeded from the initial
  set so pre-existing blocks still reflect but don't count as placement flickers.
- `BlockEffectSystem` uses two independent `EntitySet`s (positioned blocks for
  proximity/AoE, tracked blocks for add/remove detection) so `applyChanges()`
  on one path never consumes the other's change set.
- All magnitudes are named placeholder constants (`CORAL_SLOW_FACTOR`,
  `SHELL_REFLECT_DAMAGE`, player max health) — tuned in M7. Tests assert
  behavior (health decreased, factor < 1) not exact numbers.

---

## Decisions made

- **Decision**: `PlayerHealthComponent` decoupled from win/loss.
  **Reason**: GDD says the player (attacker) takes Shell reflect and the weapon
  weakness vs Shell is real, but win/loss stays buildings + timer and no
  player-death is defined.
  **Alternatives considered**: reflect as a cosmetic-only signal (rejected — makes
  the Sword/Drone-vs-Shell weakness meaningless).

- **Decision**: Jellyfish emits a flicker trigger only; the HUD post-process
  filter is deferred to M6.
  **Reason**: it is a HUD/render concern; M5 stays headless-testable.

---

## Open questions / blockers

- [ ] Manual smoke check pending (needs a display): slow near Coral, Drone clears
  a 3×3 area, destroying Shells reduces player health.

---

## Next session

- [ ] Run the M5 smoke check locally
- [ ] M6 — UI / HUD (`HudSystem`, Lemur menus). ⚠️ Lemur is a new dependency and
  needs explicit approval before adding to `libs.versions.toml`.

---

## References

- OpenSpec change: `openspec/changes/archive/2026-06-15-m5-block-special-effects/`
- GDD effect rules: `design/gdd.md` (§方塊, §Mechanics)
- Milestone breakdown: `design/milestones.md` (M5)
