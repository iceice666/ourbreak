<!--
File path: devlog/20260619/11-00-57-deploy-standalone-presentation.md
-->

# Devlog — 2026-06-19 11:00:57 — `deploy-standalone-presentation`

> **Author**: iceice666
> **Build / Version**: deck (open-slide 1.12.0)
> **Branch / Commit**: main / TBD

---

## Summary

Switch the deck deployment from the full open-slide app to a standalone HTML
export of the `ourbreak` slide. Add Playwright automation so CI can generate
the export without manual browser clicks.

---

## Goals for this session

- [x] Confirm open-slide "Export as HTML" can be automated
- [x] Add a script that drives Chromium to export the slide
- [x] Update the deploy workflow to publish the exported bundle
- [x] Verify the exported bundle renders at `/`

---

## What I worked on

### Feature / System: `deck/scripts/export-html.mjs`

- Launches Chromium (local ungoogled-chromium or Playwright-managed browser),
  opens the dev server at `/s/ourbreak`, clicks **Download → Export as HTML**,
  extracts the resulting zip, and renames `ourbreak.html` to `index.html`.
- Output lands in `deck/export/` (`index.html` + `assets/`).

### CI: `.github/workflows/deploy-deck.yml`

- Installs Playwright Chromium in the runner.
- Starts the open-slide dev server, runs the export script, then deploys
  `deck/export/` to the Cloudflare Pages project `ourbreak`.
- Removes the old SPA-fallback `_redirects` step because the export is already
  a plain static site rooted at `index.html`.

### Tooling

- Added `playwright` to `deck/package.json` devDependencies.
- Added `npm run export` script.
- Added `deck/export/` to `deck/.gitignore` (generated artifact).

---

## Technical notes

- open-slide's "Export as HTML" is a client-side blob download; there is no API
  endpoint. The only reliable way to automate it is a real browser.
- Because the slide imports `assets/Inter-Variable.ttf`, the export produces a
  zip rather than a single HTML file. The script extracts the zip and keeps the
  assets directory.
- The exported `index.html` uses relative `./assets/` URLs, so it can be served
  from any domain root.

---

## Decisions made

- **Decision**: Use Playwright (full package) instead of `playwright-core`.
  **Reason**: The full package includes the CLI to install browsers in CI;
  locally we can override `CHROMIUM_EXECUTABLE` to use ungoogled-chromium.
- **Decision**: Revert `open-slide.config.ts` to default settings.
  **Reason**: The dev server needs the full slide UI so Playwright can click the
  Download menu during export. The deployed artifact no longer runs open-slide.

---

## Open questions / blockers

- [ ] Push the commit and confirm the workflow deploys to `ourbreak.pages.dev`.
- [ ] Add the missing `ourbreak CNAME ourbreak.pages.dev` DNS record so
      `ourbreak.justaslime.dev` resolves and the Pages cert can issue.

---

## Next session

- [ ] Verify the deployed standalone presentation loads at the custom domain.
- [ ] Decide whether to keep a fallback open-slide app deploy for editing.

---

## References

- open-slide: https://github.com/1weiho/open-slide
- Playwright: https://playwright.dev
