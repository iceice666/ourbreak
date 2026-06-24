<!--
File path: devlog/20260624/10-41-12-deck-audit-fixes-merges-diagram.md
-->

# Devlog — 2026-06-24 10:41:12 — `deck-audit-fixes-merges-diagram`

> **Author**: Brian Duan
> **Build / Version**: deck (open-slide workspace)
> **Branch / Commit**: main

---

## Summary

Audited the `deck/` presentation against the assignment brief, then applied
three classes of change to `slides/ourbreak/index.tsx`: corrected test-count
claims against the real repo, merged two pairs of redundant slides, and added a
new "engineering harness" cycle diagram. Net slide count 23 → 22.

---

## Goals for this session

- [x] Verify the deck's factual claims against the actual codebase
- [x] Trim redundancy so the talk fits the 10–12 min budget
- [x] Add a diagram that anchors the anti-vibe thesis as one loop

---

## What I worked on

### Refactors / cleanup

- **Count fixes** (two were *underselling* the work):
  - `AUTO · 20 個 JUnit` → `AUTO · 101 測試 · 20 類` — repo has 101 `@Test`
    methods across 20 test files, not 20 tests.
  - `WeaponTest.java（337 行 / 20 個測試）` → `（336 行 / 15 個測試）` to match
    the real file; the in-CI point card `20 個 JUnit` → `101 個 JUnit`.
  - `~15 個 system` (TDD + ECS slides) → `9 個 system / state`, matching the 9
    tiles actually drawn on the ECS slide.
  - Left verified-correct claims untouched: 64 devlog, 12 archived changes,
    9 components.

- **Slide merges** (−2 slides, removed near-duplicate pairs):
  - `JmeEcsDecisionPage` (AI chat screenshots) + `AiDecisionRecordPage`
    (✓/✗ ECS comparison) → one slide: conversation on the left, the auditable
    decision table + conclusion on the right.
  - `DevlogPage` + `AiDialogPage` (decision quotes) → one slide: frozen
    timeline on the left, three real decision quotes on the right.

### Feature / System: `HarnessCyclePage`

- New slide after the Agenda: spec → code → ⛔ test gate → devlog → commit → ↺.
- Why: the thesis is a *process* but was never drawn as one; the pieces were
  scattered across ~7 slides. The cycle becomes the spine later sections hang
  on, so the talk can call back to "the test-gate box" instead of
  re-introducing each mechanism.

---

## Technical notes

- open-slide pages are plain React on a fixed 1920×1080 canvas; followed the
  existing token system (LIGHT/DARK scopes, `PageHead`, `CodeWin`, `PointCard`).
- Kept the shared helpers `OptionRow` and `QuoteCard` when deleting their
  former host components; moved `QuoteCard` above its new caller.
- `npm run build` (open-slide build) succeeds; the >500 kB chunk warning is
  pre-existing (large `democoncept.png` + bundle), unrelated to this change.

---

## Decisions made

- **Decision**: Fix counts rather than round them.
  **Reason**: Two figures undersold the test suite (101 vs "20"); accuracy
  also avoids a "where are the other systems?" question on stage.
  **Alternatives considered**: Leaving the round numbers — rejected, they were
  simply wrong against the repo.

- **Decision**: Merge the two duplicate-topic pairs instead of cutting content.
  **Reason**: Each pair made one point across two slides; merging keeps the
  substance while pulling runtime toward the 10–12 min budget.
  **Alternatives considered**: Cutting `art_style.md` too — deferred; flagged
  as the next easy trim if the talk still runs long.

---

## Open questions / blockers

- [ ] No real in-engine gameplay screenshot yet — only the demo video link and
      concept art. Grab 1–2 frames for the Demo slide before presenting.

---

## Next session

- [ ] Optionally fold the 9-colour palette into the concept-art slide to firmly
      hit 10–12 min.
- [ ] Add a gameplay screenshot pulled from the demo recording.

---

## References

- Assignment brief: vibe-coding journey from AI chat + Git history.
- `deck/slides/ourbreak/index.tsx`
