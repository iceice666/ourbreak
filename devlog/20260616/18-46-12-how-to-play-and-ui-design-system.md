# Devlog — 2026-06-16 18:46:12 — `how-to-play-and-ui-design-system`

> **Author**: ceil
> **Build / Version**: M6 UI polish
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Added a How to Play screen and gave the whole UI a consistent, legible design system
(colour theory + UX), instead of plain default-Lemur text on the bright scene.

---

## What I worked on

### How to Play (`HowToPlayState`, menu button)

- New main-menu button opens a reference screen: Controls (all keyboard/mouse inputs),
  the survival Goal, the three Weapons (with counters), and the five Blocks (with
  effects), in two columns. Back button + Esc return to the menu. English text (the
  bundled font is ASCII-only; the user chose English over bundling a CJK font).

### Design system (`UiTheme`) + restyle

- One palette + component factories so every screen matches. Colour theory: the scene is
  a sunny beach, so panels are a deep-ocean teal (recede → text pops), the primary accent
  is a **complementary** warm gold (max draw for the main action), aqua is an analogous
  highlight, coral red is reserved for game-over / urgency. Hierarchy by fill:
  gold-filled primary, dark+gold secondary, near-invisible subtle; uniform sizing,
  centred text, and a hover-brighten affordance.
- **Main menu**: full-screen scrim + card, "OURCRAFT" gold wordmark + tagline, three
  tiered buttons.
- **Game over**: scrim + card, GAME OVER in coral, the round reached shown as a big 72px
  gold score, Restart primary + Enter/Esc hint.
- **HUD**: dark "pill" backings behind every label so it's legible over the bright beach;
  round/weapon in gold, remaining-buildings in aqua, and the countdown turns coral-red in
  the final 10 seconds (urgency). Countdown/buildings attach only during ATTACK.
- `OurcraftGame`: hide jME's debug stat/FPS HUD (it overlapped menus and isn't for
  players).

## Next session

- [ ] Optional: align HowToPlayState's local colours to the shared `UiTheme` constants
  (currently near-identical), and add hover/transition polish.
