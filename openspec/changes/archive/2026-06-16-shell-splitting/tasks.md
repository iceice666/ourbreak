## 1. Remove player health

- [x] 1.1 Delete `PlayerHealthComponent` and `PlayerHealthComponentTest`
- [x] 1.2 Remove `PLAYER_MAX_HEALTH` + player-health wiring from `GameplayState`
- [x] 1.3 Remove `SHELL_REFLECT_DAMAGE`, the reflect tracking and `applyReflect` from `BlockEffectSystem`; drop the reflect cases from `BlockEffectTest`

## 2. Shell splitting in WeaponSystem

- [x] 2.1 Give `WeaponSystem` an occupancy view (`EntitySet` of positioned blocks) + `close()`; `PlayerControlState`/`GameplayState` release it
- [x] 2.2 In `attack`, special-case Shell: a SWORD or DRONE hit removes the Shell and spawns 2 new Shell blocks (BlockComponent + Position + Model + EffectComponent) in the nearest empty cells; a GUN hit destroys it with no spawn
- [x] 2.3 Add an empty-cell search outward/upward from the Shell's grid cell (rings then layers), returning the 2 nearest free cells

## 3. Tests

- [x] 3.1 `WeaponTest` (or a new test): SWORD/DRONE destroying a Shell spawns 2 Shells (net +1); fragments can split again; GUN destroys a Shell with no spawn
- [x] 3.2 Update any tests that assumed reflect/health

## 3b. Gun rework

- [x] 3b.1 Raise `GUN_BASE_DAMAGE` 2.0 → 8.0 (gun one-shots any single block); weapon-damage spec + tests follow the constant

## 4. Design docs

- [x] 4.1 `design/gdd.md`: Shell effect → "wrong weapon (sword/drone) splits it into 2; gun kills cleanly"; remove player-HP references
- [x] 4.2 `design/tdd.md`: Shell implementation note → split in WeaponSystem; no PlayerHealthComponent

## 5. Verification

- [x] 5.1 `./gradlew test` green
- [x] 5.2 Launch: gun cleanly clears Shells; drone/sword on a Shell multiplies it; Drone-spamming a Shell wall snowballs and can lose the round
