## Why

Every system shipped with placeholder constants and the GDD's mechanics tables are full of `TBD`s. The pieces work but the game has never been *tuned*: all weapons share one base damage, so weapon choice barely matters, and the block effects' magnitudes are arbitrary. M7 replaces the placeholders with a deliberately designed set of values that turn the four rounds into an escalating weapon-rotation puzzle, and fills in the GDD/TDD tables to match.

## What Changes

- **Per-weapon base damage** in `WeaponSystem` (Sword 1.0, Gun 2.0, Drone 1.0) instead of a single shared `BASE_DAMAGE`, giving each weapon a distinct cadence and power. Counter-matrix multipliers are finalized (Strong ×2.0, Weak ×0.5, Neutral ×1.0).
- **Tuned block effects** in `BlockEffectSystem`: Coral slow factor 0.5 (move at half speed within 1.5 cells), Shell reflect damage 20, and a named Jellyfish flicker duration constant (2.0 s) ready for the flicker visual.
- **Player health** raised to 100 so reflect damage reads on a meaningful scale.
- **NPC blocks-per-round** confirmed at 8 (one complete protective ring around the mascot) as the tuned value.
- **GDD/TDD tables** updated: every `TBD` in `design/gdd.md` §Mechanics replaced with the tuned value, and the TDD block/weapon tables brought in line.
- **Retuned tests**: `WeaponTest`'s counter-matrix assertions account for per-weapon base damage; `BlockEffectTest`'s reflect assertions use the new scale.

## Capabilities

### Modified Capabilities

- `weapon-damage`: Damage is now per-weapon base × counter multiplier (Sword 1.0 / Gun 2.0 / Drone 1.0; Strong ×2.0 / Weak ×0.5 / Neutral ×1.0) rather than a single shared base damage.
- `block-effects`: Coral slow, Shell reflect, and Jellyfish flicker-duration magnitudes are finalized (placeholder language removed).

## Impact

- `WeaponSystem` gains `SWORD_BASE_DAMAGE` / `GUN_BASE_DAMAGE` / `DRONE_BASE_DAMAGE` and drops the single `BASE_DAMAGE`; `BlockEffectSystem` reflect/flicker constants change; `GameplayState.PLAYER_MAX_HEALTH` → 100.
- `WeaponTest` and `BlockEffectTest` are retuned to the new numbers; `BlockTest` and `NpcBuilderTest` are unaffected (durabilities and block count are unchanged).
- `design/gdd.md` and `design/tdd.md` mechanics tables are updated.
- No new mechanics: this is numeric tuning only. (A noted follow-up: Shell reflect and player health have no consequence yet — see design.md.)
