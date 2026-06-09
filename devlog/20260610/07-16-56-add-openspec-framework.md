<!--
File path: devlog/20260610/07-16-56-add-openspec-framework.md
-->

# Devlog — 2026-06-10 07:16:56 — `add-openspec-framework`

> **Author**: iceice666
> **Build / Version**: M0 complete
> **Branch / Commit**: main

---

## Summary

Added OpenSpec SDD framework to the project and updated agent guidance to prefer OpenSpec over built-in plan mode when planning and executing changes.

---

## Goals for this session

- [x] Install OpenSpec skills and commands
- [x] Document SDD-first planning rules in AGENTS.md

---

## What I worked on

### Feature / System: `OpenSpec SDD framework`

- Added all OpenSpec skills under `.claude/skills/openspec-*/` and shorthand commands under `.claude/commands/opsx/`
- Added `openspec/config.yaml` as the framework configuration entry point
- Updated `AGENTS.md` with a "Planning with OpenSpec (SDD)" section that instructs agents to default to OpenSpec over `EnterPlanMode`, handle plan-mode conflicts, and provides a quick-reference skill table

### Decisions made

- **Decision**: Prefer OpenSpec over built-in plan mode for all planning work
  **Reason**: SDD artifacts are versioned, structured, and persistent — superior to ephemeral plan mode sessions for a project with multiple milestones
  **Alternatives considered**: Using built-in plan mode alongside SDD (rejected — creates confusion about which workflow to follow)

---

## Next session

- [ ] Start M1 implementation using OpenSpec (`/opsx:new`)
