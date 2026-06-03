#!/usr/bin/env bash
# Hook: PreToolUse / Bash
# Intercepts `git commit` and requires at least one staged devlog entry
# when the commit type is feat | fix | refactor | docs | build | perf.
# Exit 0 → allow the commit; Exit 2 → block and surface to Claude.

set -euo pipefail

# ── Read tool input ──────────────────────────────────────────────────────────
INPUT=$(cat)
COMMAND=$(printf '%s' "$INPUT" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tool_input',{}).get('command',''))" \
  2>/dev/null || true)

# Only act on `git commit` invocations
if ! printf '%s' "$COMMAND" | grep -qE 'git[[:space:]]+commit'; then
  exit 0
fi

# Skip `--amend` — the user is intentionally rewriting an existing commit.
if printf '%s' "$COMMAND" | grep -qE '(^|[[:space:]])--amend([[:space:]]|$)'; then
  exit 0
fi

# ── Extract the commit type from -m / --message ──────────────────────────────
# The Python script body is loaded via `read -d ''` so it can run under
# `python3 -c` while we pipe $COMMAND in as the script's stdin.
read -r -d '' EXTRACT_SCRIPT <<'PYEOF' || true
import sys, re, shlex

cmd = sys.stdin.read()
msg = ""

try:
    parts = shlex.split(cmd, posix=True)
    it = iter(parts)
    for tok in it:
        if tok in ("-m", "--message"):
            msg = next(it, "") or ""
            break
        if tok.startswith("-m") and len(tok) > 2:
            msg = tok[2:]; break
        if tok.startswith("--message="):
            msg = tok.split("=", 1)[1]; break
except ValueError:
    pass  # heredoc or unbalanced quoting — fall through

type_str = ""
if msg:
    m = re.match(r"\s*([a-z]+)(?:\([^)]+\))?!?:", msg)
    if m:
        type_str = m.group(1)
else:
    # Heredoc / multi-line fallback: scan each line for a CC prefix.
    quote_chars = chr(34) + chr(39)  # " and '
    for raw in cmd.splitlines():
        stripped = raw.lstrip().lstrip(quote_chars).lstrip()
        m = re.match(r"([a-z]+)(?:\([^)]+\))?!?:\s", stripped)
        if m:
            type_str = m.group(1)
            break

print(type_str)
PYEOF

TYPE=$(printf '%s' "$COMMAND" | python3 -c "$EXTRACT_SCRIPT")

# Types that require a devlog entry per the commit skill.
if ! printf '%s' "$TYPE" | grep -qE '^(feat|fix|refactor|docs|build|perf)$'; then
  # Either unknown (e.g., interactive editor commit) or a type that does
  # not require a devlog (chore, style, ci, test, revert). Allow.
  exit 0
fi

# ── Check staged files for a devlog entry ────────────────────────────────────
PROJECT_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd "$PROJECT_ROOT"

DEVLOG_FILES=$(git diff --name-only --cached --diff-filter=AM \
  | grep -E '^devlog/[0-9]{8}/[0-9]{2}-[0-9]{2}-[0-9]{2}-.+\.md$' || true)

if [ -z "$DEVLOG_FILES" ]; then
  echo "❌  Commit type '${TYPE}:' requires a devlog entry."
  echo "    No staged file matches devlog/YYYYMMDD/hh-mm-ss-<slug>.md."
  echo "    Copy devlog/TEMPLATE.md into devlog/$(date +%Y%m%d)/$(date +%H-%M-%S)-<slug>.md,"
  echo "    stage it, then retry the commit."
  exit 2
fi

echo "📓  Devlog entry detected:"
printf '%s\n' "$DEVLOG_FILES" | sed 's/^/    /'
exit 0
