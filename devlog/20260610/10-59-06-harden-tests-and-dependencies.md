# Devlog — 2026-06-10 10:59:06 — `harden-tests-and-dependencies`

> **Author**: Codex
> **Build / Version**: Java 21, Gradle 8.14.4
> **Branch / Commit**: main / b0a63f6

---

## Summary

Hardened component invariants, gameplay boundary handling, headless model-view synchronization, and dependency resolution
for the completed `harden-tests-and-dependencies` OpenSpec change.

---

## Goals for this session

- [x] Add boundary tests for components, round flow, weapon targeting, NPC construction, and model-view synchronization
- [x] Implement invariant checks and idempotent behavior needed by the expanded tests
- [x] Sync and archive the OpenSpec change after validation

---

## What I worked on

### Feature / System: `test and dependency hardening`

- Added constructor validation for ECS components that require non-null, non-blank, finite, or bounded values.
- Hardened round, weapon, and NPC systems around missing game-state data, completed-game behavior, invalid elapsed time,
  and unsupported build rounds.
- Extracted renderer-independent model-view synchronization so lifecycle behavior can be tested headlessly.
- Updated JUnit and pinned a secure Guava override for Zay-ES runtime compatibility.

### Bugs fixed

| ID | Description | Cause | Fix |
|----|-------------|-------|-----|
| boundary-state | Systems could null-dereference missing game-state components | Required components were read without invariant checks | Added explicit state validation and failure modes |
| completed-game-attacks | Weapon attacks could still evaluate after a terminal result | Eligibility only checked phase | Required `IN_PROGRESS` before applying damage |
| stale-model-spatial | Model changes were not represented by replacing the spatial | Model-view state only moved existing spatials | Added synchronizer logic for model replacement |

### Refactors / cleanup

- Split model-view entity synchronization from `ModelViewState` so it can be exercised without a running jME app.
- Archived `harden-tests-and-dependencies` under the dated OpenSpec archive directory.

---

## Technical notes

The spec sync preserved existing milestone requirements and merged the hardening deltas as additional requirements or
scenarios. `openspec validate --all` passes for all main specs after archiving.

---

## Playtest / observations

- Headless test suite passes through Gradle in the Nix development shell.

---

## Decisions made

- **Decision**: Add a direct Guava dependency override instead of leaving Zay-ES on its transitive Guava 19.0.
  **Reason**: The dependency audit required a current, secure runtime selection while preserving Zay-ES behavior.
  **Alternatives considered**: Leave transitive resolution unmanaged; rejected because the audit explicitly identified
  Guava as needing an override.

---

## Open questions / blockers

- [ ] None

---

## Next session

- [ ] Start M4 first-person playable work with AppState machine, player controls, and raycast wiring

---

## Screenshots / media

---

## References

- `openspec/changes/archive/2026-06-10-harden-tests-and-dependencies/`
