# Devlog — 2026-06-16 14:33:07 — `shell-splitting-and-gun`

> **Author**: ceil
> **Build / Version**: balance co-design
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Co-designed a weapon/shell rebalance. Removed player health; Shell now defends by
**splitting** under the wrong weapon; the Gun became a single-target deleter.

---

## What I worked on

### Shell splitting (`shell-splitting` capability)

- Deleted `PlayerHealthComponent` + the reflect model (the old health bar had no
  consequence). A Shell destroyed by **Sword or Drone** removes itself and spawns
  **2 new Shells** in the nearest empty cells — full Shells, **uncapped**, so
  AoE-bombing a Shell wall snowballs and the survival clock kills you. The **Gun**
  destroys a Shell cleanly (no split) — it is the correct answer.
- Implemented in `WeaponSystem` (it knows the weapon): an occupancy `EntitySet` +
  an outward/upward empty-cell search; `close()` released by `PlayerControlState`.
- `BlockEffectSystem` keeps only Coral slow, Drone area, and Jellyfish flicker.

### Gun rework

- `GUN_BASE_DAMAGE` 2.0 → 8.0: the Gun now one-shots any single block (a Rock at
  its ×0.5 weak multiplier is exactly lethal). Identity = single-target burst
  (rocks, shells, precision); weakness = no AoE. Gives the player a reason to pick
  it. Shell is exempt from the damage model.

---

## Decisions made

- **Decision**: uncapped split into full Shells (not inert debris).
  **Reason**: the co-designer wants mindless Drone use to genuinely spiral out of
  control; debris would be too forgiving.

---

## Next session

- [ ] Jellyfish vision-disrupt and Sword 3×1 sweep are still unimplemented; the
  Drone's other weakness (Jellyfish) won't bite until Jellyfish is real.
- [ ] Playtest the swing — if a stray Drone volley feels too punishing, drop the
  split count 2 → 1.
