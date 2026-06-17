# Devlog — 2026-06-16 17:11:05 — `free-mouselook-platform-switch`

> **Author**: ceil
> **Build / Version**: M4–M5 playable effects, controls
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

Mouse-look no longer needs the right button. Free look now works in WSLg, and the
code auto-switches to real captured-cursor FPS look on native desktops.

---

## What I worked on

### Root cause (researched, not guessed)

- Standard FPS look (jME FlyByCamera / GLFW `CURSOR_DISABLED`) rotates by warping the
  cursor back to centre each frame. WSLg runs over XWayland, and Wayland forbids apps
  from moving the cursor, so the warp is a no-op and the captured-look never rotates
  (GLFW issue #2271: under WSL, `CURSOR_DISABLED` doesn't hide, capture, or recentre).
  This is a platform limit, not our bug.

### Free look under WSLg (`PlayerControlState`)

- Dropped the right-drag gate. We keep the cursor visible and rotate the view from the
  cursor's per-frame position delta (`inputManager.getCursorPosition()`), so moving the
  mouse looks around with no button. Sensitivity 0.004 rad/px (mouse-right → turn right,
  after a sign flip from playtest).
- **Edge-steer**: when the cursor pins against a left/right window edge there's no delta
  to read, so we keep turning at a steady 1.8 rad/s — you can still spin past where the
  cursor runs out of room. Pitch stays clamped.

### Platform auto-switch

- `runningUnderWsl()` checks `os.name` (native Windows → false) then
  `WSL_DISTRO_NAME` / `WSL_INTEROP` / `/proc/version` for "microsoft"/"wsl".
- Native desktop (Windows / desktop Linux) → `nativeLook`: the fly-cam does normal
  captured-cursor FPS look (cursor hidden, rotationSpeed 1.5). WSLg → the visible-cursor
  delta workaround above. So the eventual Windows build feels native with no config.

---

## Decisions made

- **Decision**: keep both look modes, pick at runtime by platform.
  **Reason**: the workaround is only needed for WSLg; forcing it on Windows would make a
  native build feel worse than it should. Co-designer confirmed Windows should be smooth.

## Next session

- [ ] Windows packaging: Gradle distribution bundling Windows LWJGL natives so the game
  runs on native Windows (where the captured FPS look kicks in).
