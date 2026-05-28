#!/usr/bin/env bash
# Hook: Stop
# Writes a structured devlog entry at the end of every Claude session.
# Output path: devlog/YYYY-MM-DD/HH-MM-SS-<title>.md

set -euo pipefail

# ── Read session data from stdin ─────────────────────────────────────────────
INPUT=$(cat)

SESSION_ID=$(printf '%s' "$INPUT" \
  | python3 -c "import sys,json; print(json.load(sys.stdin).get('session_id','unknown'))" \
  2>/dev/null || echo "unknown")

TRANSCRIPT=$(printf '%s' "$INPUT" \
  | python3 -c "import sys,json; print(json.load(sys.stdin).get('transcript_path',''))" \
  2>/dev/null || true)

# ── Timestamps ───────────────────────────────────────────────────────────────
DATE=$(date +%Y-%m-%d)
TIME=$(date +%H-%M-%S)
DATETIME=$(date '+%Y-%m-%d %H:%M:%S')

# ── Project root ─────────────────────────────────────────────────────────────
PROJECT_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd "$PROJECT_ROOT"

# ── Derive title from first human message in transcript ──────────────────────
TITLE="session"
if [ -f "$TRANSCRIPT" ]; then
  TITLE=$(python3 - "$TRANSCRIPT" <<'PYEOF'
import sys, json, re

path = sys.argv[1]
title = "session"
with open(path) as f:
    for raw in f:
        raw = raw.strip()
        if not raw:
            continue
        try:
            entry = json.loads(raw)
        except Exception:
            continue

        # Support both {type/message} and flat {role/content} shapes
        role = entry.get("type") or entry.get("role", "")
        if role != "user":
            continue

        msg = entry.get("message", entry)
        content = msg.get("content", "")

        text = ""
        if isinstance(content, list):
            for block in content:
                if isinstance(block, dict) and block.get("type") == "text":
                    text = block.get("text", "").strip()
                    break
        elif isinstance(content, str):
            text = content.strip()

        if text:
            # Sanitise to a filesystem-safe slug (max 48 chars)
            slug = re.sub(r"[^a-zA-Z0-9 _-]", "", text)
            slug = re.sub(r"\s+", "-", slug.strip()).lower()[:48].rstrip("-")
            title = slug or "session"
            break

print(title)
PYEOF
  2>/dev/null || echo "session")
fi

# ── Create output directory & file path ──────────────────────────────────────
DEVLOG_DIR="$PROJECT_ROOT/devlog/$DATE"
mkdir -p "$DEVLOG_DIR"
FILEPATH="$DEVLOG_DIR/${TIME}-${TITLE}.md"

# ── Collect git context ───────────────────────────────────────────────────────
BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")
DIFF_STAT=$(git diff --stat HEAD 2>/dev/null || echo "_nothing staged_")
[ -z "$DIFF_STAT" ] && DIFF_STAT="_no uncommitted changes_"

# ── Render transcript as readable markdown ───────────────────────────────────
TRANSCRIPT_MD=""
if [ -f "$TRANSCRIPT" ]; then
  TRANSCRIPT_MD=$(python3 - "$TRANSCRIPT" <<'PYEOF'
import sys, json, textwrap

path = sys.argv[1]
out = []

with open(path) as f:
    for raw in f:
        raw = raw.strip()
        if not raw:
            continue
        try:
            entry = json.loads(raw)
        except Exception:
            continue

        role = entry.get("type") or entry.get("role", "")
        msg  = entry.get("message", entry)
        content = msg.get("content", "")

        if role not in ("user", "assistant"):
            continue

        label = "**You**" if role == "user" else "**Claude**"
        parts = []

        if isinstance(content, list):
            for block in content:
                if not isinstance(block, dict):
                    continue
                btype = block.get("type", "")
                if btype == "text":
                    parts.append(block.get("text", "").strip())
                elif btype == "tool_use":
                    name = block.get("name", "tool")
                    inp  = json.dumps(block.get("input", {}), ensure_ascii=False)
                    parts.append(f"_[tool: `{name}` — {inp[:120]}]_")
                elif btype == "tool_result":
                    snippet = str(block.get("content", ""))[:200]
                    parts.append(f"_[tool result: {snippet}]_")
        elif isinstance(content, str):
            parts.append(content.strip())

        text = "\n\n".join(p for p in parts if p)
        if text:
            out.append(f"{label}\n\n{textwrap.indent(text, '> ', predicate=lambda _: True)}\n")

print("\n---\n".join(out))
PYEOF
  2>/dev/null || echo "_transcript unavailable_")
fi

[ -z "$TRANSCRIPT_MD" ] && TRANSCRIPT_MD="_transcript unavailable_"

# ── Write the devlog file ─────────────────────────────────────────────────────
cat > "$FILEPATH" <<MDEOF
# Devlog — ${DATETIME}

| Field | Value |
|-------|-------|
| **Date** | ${DATE} |
| **Time** | $(date +%H:%M:%S) |
| **Branch** | \`${BRANCH}\` |
| **Session** | \`${SESSION_ID}\` |

---

## Summary

<!-- One-paragraph summary of what was accomplished this session -->

_TODO: fill in_

---

## Goals

- [ ] <!-- what you set out to do -->

## Outcomes

- <!-- what actually got done -->

---

## Files changed

\`\`\`
${DIFF_STAT}
\`\`\`

---

## Decisions & notes

<!-- Key decisions made, trade-offs considered, things to revisit -->

---

## Transcript

${TRANSCRIPT_MD}
MDEOF

echo "📓  Devlog → devlog/${DATE}/${TIME}-${TITLE}.md"
