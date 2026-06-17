<!--
File path: devlog/20260617/19-36-18-rename-project-to-ourbreak.md
-->

# Devlog — 2026-06-17 19:36:18 — rename-project-to-ourbreak

> **Author**: agent
> **Build / Version**: main
> **Branch / Commit**: (this commit)

---

## Summary

Renamed the entire project from `ourcraft` to `ourbreak` — package root, main class, build config, input mappings, docs, and all historical references.

---

## Goals for this session

- [x] Rename Java package `com.ourcraft` → `com.ourbreak`
- [x] Rename main class `OurcraftGame` → `OurbreakGame`
- [x] Update Gradle build scripts (`mainClass`, `applicationName`, `rootProject.name`)
- [x] Update Nix flake description
- [x] Update all docs and design files
- [x] Update jME input mappings and Preferences node
- [x] Verify build + tests pass

---

## What I worked on

### Refactors / cleanup

- Moved `app/src/main/java/com/ourcraft/` → `com/ourbreak/`
- Moved `app/src/test/java/com/ourcraft/` → `com/ourbreak/`
- Replaced `OurcraftGame` with `OurbreakGame` everywhere
- Replaced `com.ourcraft` package/import references across 59 source files and 30+ test files
- Replaced jME input mapping prefixes (`ourcraft.*` → `ourbreak.*`)
- Replaced Preferences node (`com/ourcraft` → `com/ourbreak`)
- Updated `app/build.gradle.kts`, `settings.gradle.kts`, `flake.nix`
- Updated `AGENTS.md`, `README.md`, `design/*.md`, `.claude/skills/commit/SKILL.md`
- Updated historical `devlog/` and `openspec/` references (immutable records updated in-place for consistency)

---

## Decisions made

- **Decision**: Bulk-replace all historical references in devlog and openspec archive files.
  **Reason**: The rename is project-wide; leaving stale names in searchable docs would create confusion.
  **Alternatives considered**: Leave historical docs untouched — rejected because `ourcraft` would still surface in grep/code search.

---

## Open questions / blockers

- [ ] Any hardcoded paths in CI or external scripts referencing `ourcraft`?

---

## Next session

- [ ] Resume M8 stretch goals (playable builder, real 3D art)
