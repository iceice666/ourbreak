# Devlog — 2026-06-17 10:39:14 — `remove-stale-design-doc`

> **Author**: ceil
> **Build / Version**: docs cleanup
> **Branch / Commit**: chore/remove-stale-design-doc

---

## Summary

Removed the stale original design doc (`design/Ourcraft.md`) and its 10 MB source PDF,
now fully superseded by the maintained `design/gdd.md`.

---

## What I worked on

- `design/Ourcraft.md` was a one-off PDF→markdown conversion (see the 2026-06-10
  design-doc-cleanup devlog) whose `TBD`s were filled into `gdd.md` instead. It still
  described the obsolete design (4 fixed rounds, Shell "reflects damage", no endless
  mode / coral regrowth / jellyfish poison / difficulty curve), so it only misled.
  Deleted.
- `design/Ourcraft.pdf` (≈10 MB) was the original PDF the stale `.md` came from — same
  outdated content, heavy binary in git history; the concept art it contains already
  lives in `design/img/`. Deleted.
- `design/gdd.md` (current, maintained) is kept as the single source of truth, alongside
  `tdd.md` / `milestones.md` / `art_style.md`.

## Next session

- [ ] —
