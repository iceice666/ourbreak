---
name: commit
description: Create git commits in the ourcraft repo. Enforces Conventional Commits, the per-commit devlog requirement, and devlog immutability. Use when the user asks to commit, write a commit message, split changes into multiple commits, or stage work for a commit.
---

# Commit (ourcraft)

Project-specific commit workflow for the ourcraft repo. Layered on top of the
global `make-commit` skill — that skill covers the generic mechanics of staging,
splitting, and writing Conventional Commit messages; this skill adds the rules
that are unique to this repository.

## Workflow

1. Inspect repo state with `git status --short`; review changed files & diffs
   (staged, unstaged, untracked).
2. Identify intent of each change. Group unrelated areas into separate commits.
3. Run `./gradlew test` from inside `nix develop` before committing. The
   pre-commit hook (`.claude/hooks/pre-commit-test.sh`) will block the commit
   on test failure, so fix any failures first.
4. For each commit candidate:
   - Stage only the files for that commit.
   - If the commit type requires a devlog entry (see matrix below), stage the
     devlog file in the **same** commit.
   - Write a Conventional Commit message and create the commit.
5. Report back with only the commit message(s) created — no markdown, no diff
   summary.

## Conventional Commits — required format

```text
<type>(optional-scope): <summary>

(optional-body)
```

Allowed types:

```text
feat, fix, refactor, docs, build, test, chore, perf, ci, style, revert
```

Subject rules:

- Under 50 characters when possible.
- Lowercase (except proper nouns), imperative mood, no trailing punctuation.
- Pick the most specific allowed type.
- Add a scope only when it sharpens the meaning.

Body rules:

- Omit for trivial changes.
- Wrap at 72 characters.
- Explain motivation, migration notes, or non-obvious behavior — not file lists.

## Devlog requirement matrix

Every commit either includes a devlog entry **in the same commit** or is exempt.
The hook `.claude/hooks/pre-commit-devlog.sh` enforces this for the required
types and will block commits without a staged devlog.

| Commit type | Devlog required? |
|---|---|
| `feat:`, `fix:`, `refactor:`, `docs:`, `build:`, `perf:` | **Required** |
| `chore:`, `style:`, `ci:` | Optional — add one only if the change is worth recording |
| `test:`, `revert:` | Optional, same as above |

Devlog path convention (see `devlog/README.md`):

```
devlog/YYYYMMDD/hh-mm-ss-<slug>.md
```

- `YYYYMMDD` — date folder, no separators (e.g. `20260603`)
- `hh-mm-ss` — 24-hour start time (e.g. `14-30-00`)
- `<slug>` — short kebab-case description

Create one by copying `devlog/TEMPLATE.md` into the timestamped path, filling
in the header and relevant sections, then staging it alongside the change.

## Devlog immutability

Past devlog entries are **frozen records** of a point in time. Once committed,
an entry must not be edited — no typo fixes, no rewording, no backfilling. If
something was wrong, correct it in a *later* entry.

The only allowed in-place change: update `## Open questions / blockers` and
`## Next session` to mark items resolved.

- Flip `- [ ]` to `- [x]` on the item.
- Append a link to the entry / PR / commit / issue that addressed it.
  - Entries: relative path,
    e.g. `→ [20260604/09-12-00-add-chunk-loader.md](../20260604/09-12-00-add-chunk-loader.md)`
  - PRs / issues: the URL.

Use `devlog/tools/open-items.sh` to list outstanding items across all entries.

## Commit splitting (project-specific)

Create separate commits when:

- A feature change is bundled with unrelated test infrastructure.
- Documentation updates are mixed with behavior changes.
- Independent fixes touch separate modules.
- Formatting-only churn is mixed with semantic edits.

Keep together when changes describe one intent — including a devlog entry with
the change it documents (always the same commit).

## Final output

Return only the commit message(s) actually created, one per commit, separated
by a single blank line. No markdown fences, no validation logs, no file lists.
