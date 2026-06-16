# Devlog — 2026-06-16 16:11:44 — `coral-regrowth-poison-textures`

> **Author**: ceil
> **Build / Version**: M4–M5 playable effects, balance co-design
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

A playtest-driven polish pass: shipped the Jellyfish **poison** mechanic, gave every
block a real **seamless texture**, fixed the WSLg **cursor** so the menu is clickable,
added the **poison SFX**, and redesigned **Coral** from a never-triggering proximity
slow into a wall-healing **regrowth** mechanic.

---

## What I worked on

### Jellyfish poison (`jellyfish-poison` capability)

- New `PoisonState`: blowing up a Jellyfish with the **Drone** poisons the player
  (+5s, cap 10s); Gun/Sword kills don't. While poison > 0 — any weapon — every real
  block geometry flickers random rainbow colours (re-scrambled ~0.12s) so block types
  are unreadable; restored to each block's base colour when it ends. A draining
  bottom-centre poison bar + `POISON` label shows the timer. Poison SFX (`poison.wav`)
  plays on the drone-kill trigger.
- `ModelViewState.colorFor` made public; block base Diffuse recorded in geometry
  user-data so poison/hit-flash restore the correct colour (White for textured blocks).

### Block textures

- `ModelViewState.createSpatial` now loads `Textures/<type>.png` (Repeat wrap) as a
  `DiffuseMap` with a White base — texture shows true colour, and hit-flash / poison
  still tint by overwriting Diffuse. Falls back to the flat per-type colour when no
  texture ships. Five seamless textures added (sand/coral/shell/rock/jellyfish).

### WSLg cursor + SFX

- `OurcraftGame`, `MainMenuState`, `GameEndState` force `setCursorVisible(true)` —
  WSLg hid the OS cursor on window-enter, making the Lemur menu unclickable.
- Sword slash volume 0.4 → 0.2.

### Coral redesign — regrowth (`block-effects` capability)

- The old Coral 1.5-cell proximity slow never fired: the player clears the wall from
  range (Gun 20-cell / Drone AoE / Sword sweep) and never walks up to a Coral, so the
  effect was dead. Replaced its *identity* with **regrowth** while keeping the slow as
  a situational secondary.
- New `CoralGrowthSystem`: on the first ATTACK frame it snapshots the wall's cells (the
  footprint). Every **7 seconds** each living Coral grows a new Coral into one adjacent
  face-neighbour cell that is empty and inside the footprint — so it heals the holes
  you punch and you can't finish clearing while a Coral lives, at any range. New Coral
  also spreads (snowball), but the footprint caps it at the original wall.
- Counter = **Gun**: one shot deletes each Coral, so the answer is to pick the regrowth
  sources off first. Core resolver `growthTargets(...)` is a pure function with headless
  coverage (`CoralGrowthTest`: heal a hole / surrounded does nothing / stays in
  footprint / two Corals don't share a cell).

---

## Decisions made

- **Decision**: Coral regrows **Coral** (not Sand), every **7s** (started 5s).
  **Reason**: co-designer wanted the hardcore snowball; the footprint cap keeps it
  bounded, and 5s felt too vicious in playtest so we slowed it to 7s.
- **Decision**: poison trigger is Drone-only but the hallucination is weapon-agnostic
  once active. **Reason**: punishes mindless area-droning specifically, while staying
  blinded even if you switch weapons.

---

## Next session

- [ ] Sand still falls back to a flat colour only if its texture is missing — confirm
  all five textures load on a clean checkout.
- [ ] Consider a global "one regrowth per 7s" knob if multi-Coral walls still snowball
  too hard at high rounds.
