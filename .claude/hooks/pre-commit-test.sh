#!/usr/bin/env bash
# Hook: PreToolUse / Bash
# Intercepts `git commit` commands and requires all tests to pass first.
# Exit 0  → allow the commit to proceed
# Exit 2  → block the commit and surface the message to Claude

set -euo pipefail

# ── Read tool input ──────────────────────────────────────────────────────────
INPUT=$(cat)
COMMAND=$(printf '%s' "$INPUT" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tool_input',{}).get('command',''))" \
  2>/dev/null || true)

# Only act on git commit invocations
if ! printf '%s' "$COMMAND" | grep -qE 'git\s+commit'; then
  exit 0
fi

# ── Locate project root ──────────────────────────────────────────────────────
PROJECT_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd "$PROJECT_ROOT"

# ── Guard: skip if no Gradle project exists yet ──────────────────────────────
if [ ! -f "./gradlew" ] && [ ! -f "./build.gradle" ] && [ ! -f "./build.gradle.kts" ]; then
  echo "⚠️   No Gradle project found — skipping test gate."
  exit 0
fi

echo "🧪  Tests required before commit — running now…"
echo ""

# ── Pick test command ────────────────────────────────────────────────────────
if [ -f "./gradlew" ]; then
  TEST_CMD="./gradlew test"
else
  TEST_CMD="gradle test"
fi

# ── Run tests (must already be inside the nix dev shell) ─────────────────────
# We do NOT enter `nix develop` here — that spawns a full shell and downloads
# the entire toolchain on every commit. Run Claude Code from inside `nix develop`
# or use direnv so the shell is already set up.
if ! command -v gradle &>/dev/null && [ ! -f "./gradlew" ]; then
  echo "❌  Gradle not on PATH. Are you inside the dev shell?"
  echo "    Run: nix develop"
  exit 2
fi

eval "$TEST_CMD"
STATUS=$?

# ── Gate on result ───────────────────────────────────────────────────────────
if [ "$STATUS" -ne 0 ]; then
  echo ""
  echo "❌  Tests FAILED — commit blocked."
  echo "    Fix the failing tests, then retry the commit."
  exit 2
fi

echo ""
echo "✅  All tests passed — commit allowed."
exit 0
