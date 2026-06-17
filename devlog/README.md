# Devlog

Development log for the ourbreak game project. Each entry captures what was built, decided, or learned during a session.

## Structure

```
devlog/
├── README.md          # This file
├── TEMPLATE.md        # Copy this for new entries
└── YYYYMMDD/          # One folder per day
    └── hh-mm-ss-<slug>.md
```

## Naming convention

`devlog/YYYYMMDD/hh-mm-ss-<slug>.md`

- `YYYYMMDD` — date folder, no separators (e.g. `20260603`)
- `hh-mm-ss` — 24-hour time the session started, dash-separated (e.g. `14-30-00`)
- `<slug>` — short kebab-case description (e.g. `add-chunk-loader`, `fix-lighting-bug`)

Example: `devlog/20260603/14-30-00-add-chunk-loader.md`

## Creating a new entry

1. Create today's folder if it doesn't exist: `devlog/YYYYMMDD/`
2. Copy `TEMPLATE.md` into it with the timestamped slug filename.
3. Fill in the header (author, build, branch/commit) and sections.
4. Leave sections empty or delete them if not relevant — the template is a menu, not a checklist.

## Immutability

Past devlog entries are **frozen records of a point in time**. Once committed, an entry must not be edited — typo fixes, rewording, or backfilling sections are all prohibited. If you got something wrong, correct it in a *later* entry.

**The only exception**: the `## Open questions / blockers` and `## Next session` sections can be updated to mark items resolved. When you do:

- Change `- [ ]` to `- [x]` on the item.
- Append a link to the entry, PR, commit, or issue that addressed it. Use a relative path for entries (e.g. `→ [20260604/09-12-00-add-chunk-loader.md](../20260604/09-12-00-add-chunk-loader.md)`), or the URL for PRs/issues.

Example:

```markdown
## Next session
- [x] Decide rendering stack — chose LWJGL → [20260604/09-12-00-rendering-stack.md](../20260604/09-12-00-rendering-stack.md)
- [ ] Wire up the main loop
```

No other sections may be touched after the commit lands.

## Tools

- `devlog/tools/open-items.sh` — list every unchecked item across all entries' `Open questions / blockers` and `Next session` sections. Useful when starting a new session to see what's still outstanding.

## What belongs here

- Feature work, design decisions, refactors, bugs fixed
- Playtest observations and performance numbers
- Open questions and blockers carried between sessions
- Links to references, inspiration, papers

## What doesn't

- Issue tracking — use the issue tracker
- API documentation — lives next to the code
- Anything that needs to stay current — devlog entries are frozen in time
