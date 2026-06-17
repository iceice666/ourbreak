# Devlog — 2026-06-16 10:58:40 — `fix-mouse-look-wslg`

> **Author**: ceil
> **Build / Version**: post-M5 playtest polish
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Fixed first-person mouse-look not working under WSLg, and added a crosshair.
Replaced the fly-cam's look (which hides the cursor) with our own right-drag
mouse-look that keeps the cursor visible.

---

## Bugs fixed

| ID | Description | Cause | Fix |
|----|-------------|-------|-----|
| — | Mouse never rotated the view in gameplay | jME `FlyByCamera` hides/grabs the cursor to look; WSLg/XWayland delivers no relative motion for a grabbed cursor | Take over mouse-look in `PlayerControlState` with the cursor kept visible; fly-cam keeps WASD only |

---

## Technical notes

- Diagnosed with temporary instrumentation rather than guessing: logged
  `flyCam.isEnabled` (true), the fly-cam mappings (present), and raw mouse-axis
  events. The axis events DID fire (1704 of them, value ≈ 1/1024 per pixel) —
  but only while the cursor was visible. The moment look-mode hid the cursor,
  motion stopped. That pinned the cause to cursor grabbing, not input plumbing.
- Fix: strip the fly-cam's `FLYCAM_Left/Right/Up/Down` + `RotateDrag` mappings,
  set `dragToRotate(true)` so it won't hide the cursor, and implement look here:
  hold RIGHT mouse + move to rotate (yaw/pitch with pitch clamp), cursor stays
  visible. Left-click stays a clean attack; added a centre `+` crosshair.

---

## Decisions made

- **Decision**: hand-rolled mouse-look instead of jME's `FlyByCamera` look.
  **Reason**: FlyByCamera couples look to a hidden/grabbed cursor, which is
  exactly what WSLg can't feed; a visible-cursor right-drag is the only reliable
  path here.
  **Alternatives considered**: force the GLFW X11 backend (unset
  `WAYLAND_DISPLAY`) — tried, did not fix it; the cursor-grab is the real issue.
