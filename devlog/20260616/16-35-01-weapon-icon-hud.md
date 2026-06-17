# Devlog — 2026-06-16 16:35:01 — `weapon-icon-hud`

> **Author**: ceil
> **Build / Version**: M4–M5 playable effects, HUD polish
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

The HUD weapon readout now shows the equipped weapon's **icon** after the name —
`Weapon: SWORD [icon]` — using the hand-drawn sword/gun/drone art.

---

## What I worked on

### Weapon icon (`HudState`)

- Added a `Picture` to the HUD that displays the current weapon's icon from
  `Icons/<weapon>.png`. The image is swapped only when the equipped weapon changes
  (setImage reloads the texture), and is fit into a 72px box **preserving aspect
  ratio** so the wide source art isn't squashed.
- Layout: the `Weapon: NAME` label sits at the bottom-left and the icon is placed
  right after it (repositioned each frame from the label width, so GUN/DRONE/SWORD
  name lengths all line up), vertically centred on the text.
- Icons are transparent PNGs (sword/gun use the cropped v3 art, drone the v2). The
  earlier opaque-background exports were a problem; the final art ships with real
  alpha.

---

## Decisions made

- **Decision**: icon after the text, not before.
  **Reason**: co-designer wanted `Weapon: sword <icon>` reading order.

## Next session

- [ ] Optional: a drone v3 (cropped) to match sword/gun; currently drone uses v2.
- [ ] Main-menu upgrade: wire the Ourcraft banner art as the title + enlarge buttons.
