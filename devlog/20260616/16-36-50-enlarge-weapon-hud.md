# Devlog — 2026-06-16 16:36:50 — `enlarge-weapon-hud`

> **Author**: ceil
> **Build / Version**: M4–M5 playable effects, HUD polish
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Enlarged the weapon readout so the `Weapon: NAME [icon]` is easy to read at a glance.

---

## What I worked on

- `HudState`: weapon icon box 72 → 110px and the weapon label font 24 → 34px (a new
  `WEAPON_FONT_SIZE`, applied only to the weapon label so the rest of the HUD keeps
  its size). The aspect-fit and text-then-icon layout are unchanged, so it scales
  cleanly.

## Next session

- [ ] Main-menu upgrade: Ourbreak banner art as the title + enlarged buttons.
