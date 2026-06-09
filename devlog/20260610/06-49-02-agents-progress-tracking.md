<!--
File path: devlog/20260610/06-49-02-agents-progress-tracking.md
-->

# Devlog — 2026-06-10 06:49:02 — `agents-progress-tracking`

> **Author**: iceice666
> **Build / Version**: skeleton (M0 done)
> **Branch / Commit**: main

---

## Summary

Added a Current Progress table and rule 8 to `AGENTS.md` so agents always see
which milestone is active and are required to keep the table current on every
commit.

---

## Goals for this session

- [x] Add Current Progress section to AGENTS.md with milestone status table
- [x] Add agent rule 8: update the table on every commit

---

## What I worked on

### Docs: `AGENTS.md` progress tracking

- Added a **Current Progress** section above Agent rules, linking to
  `design/milestones.md` for the full breakdown and showing a one-line status
  per milestone.
- Added **rule 8**: agents must flip milestone status to `✅ done` / `⬜ next`
  and stage `AGENTS.md` in the same commit as milestone work.
- This makes the project state visible at a glance without reading devlogs or
  the full milestone doc.

---

## Decisions made

- **Decision**: Keep the progress table in `AGENTS.md` (not a separate file).
  **Reason**: Agents load CLAUDE.md / AGENTS.md on every session; putting the
  table there means no extra file to remember to read.
  **Alternatives considered**: Separate `STATUS.md` — adds indirection with no
  benefit since the audience is agents reading CLAUDE.md.

---

## Next session

- [ ] Implement M1: `RoundComponent`, `PhaseComponent`, `RoundSystem`,
  `VictorySystem` — tests first (`RoundSystemTest`, `VictorySystemTest`)
