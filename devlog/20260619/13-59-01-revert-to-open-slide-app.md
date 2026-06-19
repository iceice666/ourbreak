<!--
File path: devlog/20260619/13-59-01-revert-to-open-slide-app.md
-->

# Devlog — 2026-06-19 13:59:01 — `revert-to-open-slide-app`

> **Author**: iceice666
> **Build / Version**: deck (open-slide 1.12.0)
> **Branch / Commit**: main / f5359c4 (reverted)

---

## Summary

Revert the standalone HTML export deploy (`f5359c4`) and go back to deploying
the full open-slide app. The exported single-page HTML broke the viewing UX on
phones, so we return to shipping the interactive SPA build.

---

## Goals for this session

- [x] Roll back the Playwright export pipeline
- [x] Restore the `npm run build` → `deck/dist` deploy with SPA fallback
- [x] Verify the build still produces `deck/dist` locally

---

## What I worked on

### CI: `.github/workflows/deploy-deck.yml`

- Restored the pre-`f5359c4` workflow: `npm ci` → `npm run build`, generate
  `dist/_redirects` (root → `/s/ourbreak`, SPA fallback), deploy `deck/dist`.
- Dropped the Playwright install + dev-server-export steps and reverted the
  deploy command from `pages deploy export` back to `pages deploy dist`.

### Tooling

- Removed `deck/scripts/export-html.mjs` and the `npm run export` script.
- Removed `playwright` from `deck/package.json` devDependencies (and lockfile).
- Removed the `export` entry from `deck/.gitignore` (artifact no longer exists).

---

## Technical notes

- Root cause of the rollback: open-slide's "Export as HTML" produces a static
  snapshot that loses the responsive/touch presenter behavior, degrading the
  phone viewing experience. The full SPA keeps the interactive viewer.
- Build output again lands in `deck/dist` (gitignored); deploy is unchanged from
  the original `09-59-28-deck-cloudflare-deploy` setup.

---

## Decisions made

- **Decision**: Revert to the full open-slide app deploy instead of patching the
  export.
  **Reason**: Phone UX regression; the interactive build already works and the
  deploy path is proven (run `27801115084`).

---

## Open questions / blockers

- None. Secrets (`CLOUDFLARE_API_TOKEN`, `CLOUDFLARE_ACCOUNT_ID`) are already set.

---

## Next session

- [ ] Push and confirm the SPA redeploys cleanly to `ourbreak.pages.dev`.

---

## References

- Superseded approach: `devlog/20260619/11-00-57-deploy-standalone-presentation.md`
- open-slide: https://github.com/1weiho/open-slide
- wrangler-action: https://github.com/cloudflare/wrangler-action
