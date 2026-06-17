## 1. Weapon damage

- [x] 1.1 In `WeaponSystem`, replace the single `BASE_DAMAGE` with per-weapon `SWORD_BASE_DAMAGE = 1.0`, `GUN_BASE_DAMAGE = 2.0`, `DRONE_BASE_DAMAGE = 1.0`; compute `damage = baseDamage(weaponType) × multiplier(...)`. Keep STRONG 2.0 / WEAK 0.5 / NEUTRAL 1.0.
- [x] 1.2 Retune `WeaponTest`: the counter-matrix assertion accounts for each weapon's base damage; add coverage that base damage differs per weapon.

## 2. Block effects & player health

- [x] 2.1 In `BlockEffectSystem`: confirm `CORAL_SLOW_FACTOR = 0.5`, set `SHELL_REFLECT_DAMAGE = 20.0`, add `JELLYFISH_FLICKER_SECONDS = 2.0` (named, reserved for the flicker visual)
- [x] 2.2 In `GameplayState`, set `PLAYER_MAX_HEALTH = 100`
- [x] 2.3 Retune `BlockEffectTest`: player starts at 100 HP; single reflect → 80, three chained → 40

## 3. NPC blocks-per-round

- [x] 3.1 Confirm `NpcBuilderSystem.BLOCKS_PER_ROUND = 8` as the tuned value (one full ring) — no change, documented in design

## 4. Design docs

- [x] 4.1 Fill `design/gdd.md` §Mechanics `TBD`s: Coral slow 50%, Shell reflect 20, Jellyfish flicker 2 s, blocks/round 8, weapon base damages (Sword 1.0 / Gun 2.0 / Drone 1.0)
- [x] 4.2 Bring `design/tdd.md` block/weapon tables in line with the tuned values

## 5. Verification

- [x] 5.1 Run `./gradlew test`; the full headless suite is green (zero failures, zero errors)
- [ ] 5.2 Launch sanity check: weapon switching changes how fast each block type breaks (right weapon noticeably faster)
