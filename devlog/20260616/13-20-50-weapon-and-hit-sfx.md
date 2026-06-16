# Devlog — 2026-06-16 13:20:50 — `weapon-and-hit-sfx`

> **Author**: ceil
> **Build / Version**: audio polish
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Playtest audio pass: per-weapon attack sounds (sword slash, drone boom), fixed
the AoE sound "roar", and fixed silent one-hit-kills.

---

## Bugs fixed

| ID | Description | Cause | Fix |
|----|-------------|-------|-----|
| — | Drone AoE hit sound was a deafening roar | `playInstance()` fired per damaged block, so a 3×3 stacked many copies | One hit sound per frame regardless of how many blocks were hit |
| — | Hitting Sand (and other 1-hit blocks) made no sound | Hit feedback only fired on a *surviving* durability drop; 1-hit blocks are destroyed without that step | Count block *removals* as hits too |

---

## What I worked on

- `PlayerControlState`: per-weapon attack SFX — SWORD → `sword-slash.wav`
  (volume 0.4), DRONE → `drone-boom.wav` (0.8), GUN → none yet. Played on the
  attack action via `playInstance()`.
- `HitFeedbackState`: the block-hit sound now plays once per frame for any
  block damaged *or destroyed*; flash/punch stays per block.
- Assets: `Sound/drone-boom.wav`, `Sound/sword-slash.wav` (188 KB / 104 KB).

---

## Next session

- [ ] Gun attack sound; consider material-specific block-hit variants.
