# Devlog — 2026-06-10 07:02:52 — `art-style-doc`

> **Author**: iceice666
> **Build / Version**: M0 done
> **Branch / Commit**: main

---

## Summary

Created `design/art_style.md`, the first dedicated art style document for ourcraft. Covers visual direction, color system, character art specs, block/weapon art, environment, VFX, UI/HUD, and lighting.

---

## Goals for this session

- [x] Write art style document grounded in existing GDD world-building

---

## What I worked on

### Feature / System: `art_style.md`

- Establishes Tropical Cartoon Low-Poly as the visual direction
- Defines a 9-color primary palette with hex values for all game objects
- Specifies character proportions, color coding, and idle animation direction for Clawd and Openclaw
- Documents visual identity for all 5 block types (including destruction VFX)
- Covers weapon first-person appearance and attack effects
- Defines HUD layout, UI typography style, and win/lose screen art direction
- Sets lighting parameters (directional light + ambient, no dynamic global shadows)

---

## Decisions made

- **Decision**: Low-poly stylized over realistic or pixel art
  **Reason**: Fits the tropical beach + cute mascot theme; achievable within the project's development timeline; pairs well with JMonkeyEngine's rendering model
  **Alternatives considered**: Pixel art (doesn't suit first-person 3D), realistic (too costly to produce)

- **Decision**: Color-code each block type with a distinct hex primary color
  **Reason**: Players need to identify block types instantly in first-person; color is the fastest signal ahead of shape

---

## Next session

- [ ] Begin M1 implementation (RoundSystem, VictorySystem)

---

## References

- `design/gdd.md` — world-building, block/weapon specs
- `design/img/` — all concept art referenced in the doc
