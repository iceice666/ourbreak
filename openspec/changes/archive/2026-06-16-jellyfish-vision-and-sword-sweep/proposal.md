## Why

Two counter-matrix matchups are still dead because the mechanics behind them were never built: Jellyfish's "disrupts your view" is just an unrendered trigger count, and the Sword's "sweep a row" is unimplemented (Sword is single-target, so it differs from the Gun only by a damage number). Building both makes the matrix live — Jellyfish punishes brainlessly droning the wall (the only way to get poisoned), while the Gun/Sword are the clean way to clear Jellyfish, and the Sword gets its real melee identity (clear a row of soft blocks fast).

## What Changes

- **Jellyfish poison.** Blowing up a Jellyfish with the DRONE poisons the player: +5 seconds, capped at 10. While poisoned — no matter which weapon is then held — every real block flickers through random rainbow colours, so you can't read block types (Shell vs Rock vs Sand) until it wears off. A draining bar shows the remaining poison. Killing a Jellyfish with the Gun or Sword does **not** poison, so the poison specifically punishes lazy area-droning. (Trigger is Drone-only; the hallucination effect itself is weapon-agnostic once active.)
- **Sword sweep.** A SWORD attack hits a 3-cell horizontal row — the crosshair block plus its two neighbours across the player's view — instead of a single block. The Sword becomes the fast soft-wall clearer; Gun stays single-target, Drone stays 3×3.

## Capabilities

### New Capabilities

- `jellyfish-poison`: Defines the Jellyfish poison — drone-killing a Jellyfish adds +5s poison (cap 10s); while poisoned every real block flickers random rainbow colours (any weapon) and a draining bar shows the remaining time; Gun/Sword kills do not poison.

### Modified Capabilities

- `player-control`: A SWORD attack sweeps a 3-cell row (crosshair block + the two cells across the view), not just the single block under the crosshair.
- `block-effects`: Adds a sword-row expansion (the 3-cell horizontal row around a centre block) alongside the existing Drone 3×3 expansion.

## Impact

- Adds `PoisonState` (detects drone-killed Jellyfish, holds the 0–10s poison timer, recolours real block geometries to random rainbow while poisoned, restores per-type colours when it ends, and draws the draining bar), attached by `GameplayState`.
- Makes `ModelViewState.colorFor(modelId)` public so `PoisonState` can restore the real per-type block colours.
- Adds `BlockEffectSystem.rowTargets(...)`; `PlayerControlState` picks the sweep axis from the camera facing and routes a SWORD attack through it.
- Updates `design/gdd.md` / `design/tdd.md` (Jellyfish poison implemented; Sword sweep implemented). Adds `BlockEffectTest` coverage for the row expansion. The poison hallucination is runtime-visual (verified by launching). No new dependencies.
