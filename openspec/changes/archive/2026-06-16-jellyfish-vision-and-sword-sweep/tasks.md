## 1. Sword sweep

- [x] 1.1 Add `BlockEffectSystem.rowTargets(centerBlockId, alongX)` → center + the two ±1 cells along the chosen axis (X or Z) at the same height, occupied only
- [x] 1.2 In `PlayerControlState`, route a SWORD attack through `rowTargets`, choosing the axis from the camera facing (sweep across the view); GUN single, DRONE 3×3 unchanged
- [x] 1.3 `BlockEffectTest`: row expansion (full / sparse / isolated)

## 2. Jellyfish poison

- [x] 2.1 Make `ModelViewState.colorFor(modelId)` public so the poison effect can restore real per-type colours
- [x] 2.2 Add `PoisonState` (`BaseAppState`): diff the block `EntitySet`; a removed Jellyfish while the DRONE is equipped → poison += 5s capped at 10s (Gun/Sword kills add nothing); drain poison by `tpf`; while poison > 0 recolour every real block geometry to random rainbow (any weapon, re-scramble ~0.12s) and restore via `colorFor` when it ends; draw a draining poison bar; restore + hide on cleanup/disable
- [x] 2.3 `GameplayState` attaches/detaches `PoisonState` (after `gameStateId`/player are set)

## 3. Design docs

- [x] 3.1 `design/gdd.md`: Jellyfish effect now implemented (drone-kill poison +5s/cap 10s; rainbow block hallucination; draining bar); Sword sweep implemented (3-cell row)
- [x] 3.2 `design/tdd.md`: matching notes

## 4. Verification

- [x] 4.1 `./gradlew test` green (incl. row-expansion tests)
- [ ] 4.2 Launch: sword clears a 3-row of sand fast; droning a Jellyfish poisons → real blocks flicker rainbow and the bar drains; Gun/Sword Jellyfish kills don't poison
