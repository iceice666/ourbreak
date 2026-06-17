# Devlog — 2026-06-16 17:14:40 — `windows-distribution-packaging`

> **Author**: ceil
> **Build / Version**: M4–M5 playable effects, packaging
> **Branch / Commit**: feat/m4-m5-playable-effects

---

## Summary

The Gradle build now produces a portable cross-platform distribution, so the game can
be run on native Windows (where the captured-cursor FPS look works) from a zip built on
WSL/Linux.

---

## What I worked on

### Cross-platform natives (`app/build.gradle.kts`)

- jME's lwjgl3 backend only resolves the **build host's** LWJGL natives via Gradle
  module metadata, so a zip built on WSL/Linux had no Windows natives and couldn't run
  on Windows. Added explicit `runtimeOnly` classifier deps for every platform
  (`natives-windows` / `-linux` / `-macos` / `-macos-arm64`) across the modules jME
  loads (`lwjgl`, `lwjgl-glfw`, `lwjgl-jemalloc`, `lwjgl-openal`, `lwjgl-opengl`),
  pinned to LWJGL 3.3.6 (the version jME 3.9 resolves).
- Set `applicationName = "ourbreak"` so the distribution and start scripts are named
  `ourbreak` / `ourbreak.bat` instead of `app`.

### Result

- `./gradlew :app:distZip` → `app/build/distributions/ourbreak.zip` (~58 MB) with
  `bin/ourbreak.bat` (Windows) + `bin/ourbreak` (sh) and all platforms' natives in
  `lib/`. Resources (textures/sounds/icons) ship inside `app.jar`.

## How to run on Windows

- Needs a Java 21 runtime on the Windows side (`JAVA_HOME` or `java` on PATH).
- Unzip `ourbreak.zip`, run `bin\ourbreak.bat`. The platform check picks the native
  captured-cursor FPS look automatically (no WSLg workaround).

## Next session

- [ ] If "no Java install required" is wanted, add jpackage/jlink — but a Windows
  installer must be built on Windows (jpackage is host-targeted), so distZip + a
  Windows JDK is the cross-build path from WSL.
