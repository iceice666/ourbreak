# Devlog — 2026-06-17 16:35:54 — `round-clear-banner`

> **Author**: ceil
> **Build / Version**: UX — between-round flourish
> **Branch / Commit**: feat/destruction-juice

---

## Summary

A between-round banner acknowledges a cleared village before the next one starts, so rounds
no longer blur together.

---

## What I worked on

- New `RoundBannerState`: watches the round number and, when it advances, drops a centred
  upper-third banner — "ROUND N CLEARED / Next: Round N+1" — for 2.5s (auto), styled with the
  shared `UiTheme` card. Purely visual: the next village rebuilds behind it and input is never
  blocked. The first round (game start) isn't announced (nothing was cleared yet).
- Wired into `GameplayState` alongside the other states.

## Next

- [ ] Optional polish: fade in/out, a soft chime; the game-balance / playability review the
  designer asked for.
