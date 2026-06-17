# Devlog — 2026-06-03 10:33:19 — `set-up-devlog`

> **Author**: brian
> **Build / Version**: pre-alpha
> **Branch / Commit**: main @ c09d321

---

## Summary

Bootstrapped the devlog system: created the `devlog/` folder, a reusable entry template, and a README documenting the naming convention. This is the first entry — meta, but useful as a worked example.

---

## Goals for this session

- [x] Decide on a devlog file/folder convention
- [x] Write a template that's structured but not rigid
- [x] Document the convention in a README
- [x] Drop a first entry as a reference

---

## What I worked on

### Feature / System: `devlog scaffold`

- Added `devlog/TEMPLATE.md` — copy-paste skeleton for new entries.
- Added `devlog/README.md` — explains layout, naming, and what belongs here.
- Convention: `devlog/YYYYMMDD/hh-mm-ss-<slug>.md`, one folder per day, slug in kebab-case.

### Bugs fixed

| ID | Description | Cause | Fix |
|----|-------------|-------|-----|
| —  | n/a         |       |     |

### Refactors / cleanup

- None.

---

## Technical notes

The date-folder + timestamp-file layout was chosen over a flat `YYYY-MM-DD-slug.md` scheme because:

- Days with many entries stay tidy (folder collapses in editors).
- Sorting `ls devlog/` gives a clean chronological index of active days.
- Timestamps preserve intra-day ordering without needing entry numbers.

---

## Playtest / observations

n/a — no game code touched this session.

---

## Decisions made

- **Decision**: Use `YYYYMMDD/` date folders rather than `YYYY-MM-DD-` filename prefixes.
  **Reason**: Keeps the top-level `devlog/` listing short as the project grows.
  **Alternatives considered**: Flat folder with date-prefixed filenames; per-week folders.

- **Decision**: Template lives at `devlog/TEMPLATE.md`, not inside a `.template/` dotfolder.
  **Reason**: Discoverable for any contributor browsing the repo.
  **Alternatives considered**: Hidden folder, or a snippet in the README.

---

## Open questions / blockers

- [ ] Should screenshots/media live next to the entry, or in a shared `devlog/media/` folder?
- [ ] Want a `make devlog` (or Gradle task) helper to scaffold the file automatically?

---

## Next session

- [ ] Start the actual game scaffold — Gradle project, `com.ourbreak` package, hello-world main class.
- [ ] Decide rendering stack (LWJGL? libGDX? something else?).

---

## Screenshots / media

<!-- none yet -->

---

## References

- `devlog/README.md` — convention and rules
- `devlog/TEMPLATE.md` — template this entry was generated from
