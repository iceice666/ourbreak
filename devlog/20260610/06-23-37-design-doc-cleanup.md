# Devlog — 2026-06-10 06:23:37 — `design-doc-cleanup`

> **Author**: ceil32768
> **Build / Version**: pre-implementation
> **Branch / Commit**: design/doc-cleanup

---

## Summary

Renamed design files to drop the `v2` suffix and converted the master design
PDF (`Ourbreak.pdf`) into a standard Markdown document with embedded images.

---

## Goals for this session

- [x] Rename `gddv2.md` → `gdd.md` and `tddv2.md` → `tdd.md`
- [x] Convert `design/Ourbreak.pdf` to `design/Ourbreak.md`
- [x] Embed concept art images from `design/img/` into the Markdown

---

## What I worked on

### Docs: design file rename

Dropped the `v2` suffix — files are plain `gdd.md` and `tdd.md` going forward.
Content is unchanged.

### Docs: PDF → Markdown conversion

Extracted text via `pdftotext` (poppler) and structured it into Markdown with
tables. All five concept-art images from `design/img/` are embedded at the
relevant sections:

- `democoncept.png` — gameplay screenshot (cover)
- `ourbreakconcept.png` — full game concept overview
- `blockconcept.jpg` — block durability & feature sheet
- `weaponconcept.jpg` — weapon vs. block counter-play chart
- `selectchar.jpg` — character select screen (User Widget section)

---

## Decisions made

- **Decision**: use `design/img/` (pre-existing, descriptively named) instead of a new `images/` folder.
  **Reason**: the folder already existed with better filenames; no duplication needed.

---

## Next session

- [ ] Fill in TBD values in `Ourbreak.md` (block counts, damage numbers, effect durations)
