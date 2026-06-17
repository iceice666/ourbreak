# Devlog — 2026-06-17 17:15:10 — `high-score-new-best`

> **Author**: ceil
> **Build / Version**: meta / retention — peak-end
> **Branch / Commit**: feat/destruction-juice

---

## Summary

A persistent best-round high score, with a "NEW BEST" celebration on game-over so a death
reads as an achievement, not just a loss.

---

## What I worked on

- `HighScore`: persists the best round reached across runs via the platform `Preferences` store
  (cross-platform, no file-path management). `submit(round)` stores + returns true on a new record.
- `GameEndState`: records the run; on a new record it shows a pulsing gold "★ NEW BEST ★" banner
  (breathing brightness) above the score — reframing the loss as a win (peak-end rule). Otherwise it
  shows "Best: Round X" as the target to beat.
- `MainMenuState`: shows "Best: Round N" under the tagline once a record exists, reinforcing the
  beat-your-own-score loop.

## Why

From the design review (peak-end): an endless survival run always ends on death, so the session's
last beat is sour. Celebrating a record turns that into "I got further than ever" — the highest-value
retention change.

## Next

- [ ] Optional: a record chime; the other review items (gun-pace playtest, drone trivialization,
  sword identity, the crab's emotional layer).
