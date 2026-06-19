<!--
File path: devlog/20260619/09-59-28-deck-cloudflare-deploy.md
-->

# Devlog — 2026-06-19 09:59:28 — `deck-cloudflare-deploy`

> **Author**: iceice666
> **Build / Version**: deck (open-slide 1.12.0)
> **Branch / Commit**: main / b49dea2

---

## Summary

Add CI to auto-deploy the `deck/` slide presentation to Cloudflare Pages at
`ourbreak.justaslime.dev` on every push that touches `deck/**`.

---

## Goals for this session

- [x] Wire GitHub Actions → Cloudflare Pages for the deck
- [x] Serve the deck at the custom domain `ourbreak.justaslime.dev`
- [x] Redirect site root `/` to the slide route `/s/ourbreak`

---

## What I worked on

### Feature / System: `deploy-deck` workflow

- `.github/workflows/deploy-deck.yml`: on push to `main` under `deck/**`,
  runs `npm ci` + `npm run build` in `deck/`, then deploys `deck/dist` to the
  Cloudflare Pages project `ourbreak` via `cloudflare/wrangler-action@v3`.
- Path-filtered + `workflow_dispatch` for manual runs; `concurrency` guard
  cancels superseded runs.

### Cloudflare setup (done out-of-band)

- Created Pages project `ourbreak` (production branch `main`).
- First deploy uploaded manually; custom domain `ourbreak.justaslime.dev`
  attached (in-zone `justaslime.dev`, CNAME auto-provisioned, cert issuing).

---

## Technical notes

- open-slide builds a SPA with absolute `/assets/` paths and a client-side
  route `/s/ourbreak`. A `dist/_redirects` is generated at deploy time:
  - `/ → /s/ourbreak (302)` so the bare domain lands on the deck.
  - `/* → /index.html (200)` SPA fallback for deep links / refresh.
- Build output lands in `deck/dist` (gitignored).

---

## Decisions made

- **Decision**: deploy via Cloudflare Pages + `wrangler-action`, not Netlify/Vercel.
  **Reason**: `justaslime.dev` is already a Cloudflare zone (sibling `inm` site).
  **Alternatives considered**: Pages GitHub integration (no Actions) — chose
  Actions so the build is reproducible and path-scoped to `deck/**`.

---

## Open questions / blockers

- [ ] CI needs two repo secrets: `CLOUDFLARE_API_TOKEN` (scoped: Pages Edit)
      and `CLOUDFLARE_ACCOUNT_ID`. Account ID is set; the API token must be
      minted by the repo owner and added before the workflow can deploy.

---

## Next session

- [ ] Add `CLOUDFLARE_API_TOKEN` secret, then push to verify the workflow.
- [ ] Confirm cert active + custom domain serving over HTTPS.

---

## References

- open-slide: https://github.com/1weiho/open-slide
- wrangler-action: https://github.com/cloudflare/wrangler-action
