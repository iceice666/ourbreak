# Devlog — 2026-06-16 12:49:01 — `block-juice`

> **Author**: ceil
> **Build / Version**: playtest polish
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Playtest feedback: every block rendered as the same cyan cube (so the
counter-matrix was invisible) and hits had no feedback (a non-lethal hit looked
like nothing happened). Added per-type block colours and hit juice
(flash + scale-punch + sound).

---

## What I worked on

### Per-type block colours

- `ModelViewState.createSpatial` now colours each block by its model id (sand =
  tan, coral = pink, shell = cream, rock = grey, jelly = blue) using a lit
  material so the shapes read in 3D. Placeholder art only — real models = M8.

### Hit feedback (`HitFeedbackState`)

- Watches block durability via an `EntitySet`; on a durability drop it flashes
  the block white, punches its scale (+25 %) for ~0.14 s, and plays a hit sound.
  Block spatials are resolved through the `entityId` user-data tag the
  model-view synchronizer sets, then animated back to base each frame.
- Sound: `Sound/block-hit.wav` (188 KB) loaded as a Buffer AudioNode, fired with
  `playInstance()` so rapid/AoE hits overlap. Loads tolerantly (silent if absent).

---

## Technical notes

- Why this matters for design: you can't choose the right weapon if you can't
  see whether a block is Coral or Rock, and multi-hit Rock felt like a bug
  without per-hit feedback. This makes the M7 weapon-rotation puzzle legible.

---

## Next session

- [ ] Endless-rounds survival mode (player idea): infinite rounds, escalating
  difficulty, score = rounds survived. Plan + GDD update.
