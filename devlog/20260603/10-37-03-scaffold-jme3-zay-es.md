# Devlog — 2026-06-03 10:37:03 — `scaffold-jme3-zay-es`

> **Author**: brian
> **Build / Version**: pre-alpha
> **Branch / Commit**: main @ c335aa6

---

## Summary

Stood up the rendering + ECS stack: jMonkeyEngine 3.9.0-stable for the engine and Zay-ES 1.6.0 for entities. Replaced the placeholder `App` with a `SimpleApplication` subclass, added a minimal component/system layer, and verified the build green with two headless ECS tests.

---

## Goals for this session

- [x] Pick versions for jME3 and Zay-ES from Maven Central (not guess from memory)
- [x] Wire them through the Gradle version catalog
- [x] Get a window-capable entry point compiling
- [x] Land a tiny ECS slice (components + a view system) that actually drives a `Spatial`
- [x] Keep tests headless so CI doesn't need a display

---

## What I worked on

### Feature / System: `OurbreakGame` entry point

- `SimpleApplication` subclass at `com.ourbreak.OurbreakGame`.
- Creates a `DefaultEntityData`, attaches `ModelViewState`, sets a directional + ambient light, and spawns two cube entities.
- `destroy()` closes the `EntityData` so we don't leak in repeated app starts.

### Feature / System: ECS scaffold

- `PositionComponent(float x, y, z)` and `ModelComponent(String modelId)` — both records implementing `EntityComponent`. Records-as-components keeps them immutable, which is the Zay-ES contract.
- `ModelViewState extends BaseAppState` — holds an `EntitySet` over `(Position, Model)`, materialises a `Box` `Geometry` per added entity, syncs translations on changed, removes on removed.
- Materials are `Unshaded` cyan for now; lighting can come once we have a real mesh pipeline.

### Bugs fixed

| ID | Description | Cause | Fix |
|----|-------------|-------|-----|
| —  | n/a         |       |     |

### Refactors / cleanup

- Deleted the Gradle-init `App.java` / `AppTest.java` placeholders.

---

## Technical notes

**Dependencies (version catalog).** Pinned via `gradle/libs.versions.toml`:

```toml
jme3   = "3.9.0-stable"   # 3.10.x is still beta
zay-es = "1.6.0"          # latest release on Maven Central
```

jME3 ships `core`, `desktop`, `lwjgl3`, `plugins`, `effects` as a bundle. `jme3-lwjgl3` pulls the right LWJGL natives transitively, so no manual classifier wiring.

**macOS quirk.** LWJGL3 requires `-XstartOnFirstThread` on macOS or the app crashes immediately when starting the GLFW context. Conditionally injected in `app/build.gradle.kts`:

```kotlin
if (System.getProperty("os.name").lowercase().contains("mac")) {
    applicationDefaultJvmArgs = listOf("-XstartOnFirstThread")
}
```

This only applies to `./gradlew run`. Anyone running the jar directly will need to pass the flag themselves.

**Components as records.** `PositionComponent` stores raw floats rather than a `Vector3f`, because `Vector3f` is mutable — handing one out via the record accessor would let a caller silently mutate "immutable" component state. `toVector3f()` / `of(Vector3f)` cover the bridge.

**Headless tests.** `DefaultEntityData` is fully in-process and doesn't touch jME3, so `EntityDataTest` exercises `setComponents` + `EntitySet.applyChanges()` without spinning up a window. Both tests pass in ~40 ms.

```
Tests: 2, Skipped: 0, Failures: 0, Errors: 0
```

---

## Playtest / observations

- Haven't actually launched the window yet this session — just verified `./gradlew build` is green. Visual check is the first task next session.

---

## Decisions made

- **Decision**: Use jME3 `3.9.0-stable`, not 3.10 or 3.8.
  **Reason**: Latest *stable* line on Maven Central; 3.10 is still beta, 3.8 has known fixes superseded by 3.9.
  **Alternatives considered**: Pin to 3.8.1-stable for conservatism — rejected since 3.9 is already stamped stable.

- **Decision**: Zay-ES (`com.simsilica:zay-es`) for the ECS layer, not Artemis-odb or a hand-rolled one.
  **Reason**: Designed for jME3, well-trodden in the community, idiomatic `AppState` integration via `EntitySet`. Keeps us in one ecosystem.
  **Alternatives considered**: Artemis-odb (faster but jME-agnostic), Ashley (libGDX-ish), DIY (premature).

- **Decision**: Components are Java records, not plain classes.
  **Reason**: Immutability for free; matches Zay-ES's "replace the component" update model; no boilerplate.
  **Alternatives considered**: Plain final-field classes with explicit constructors — strictly worse in Java 21.

- **Decision**: Split ECS code into `ecs.components` and `ecs.systems` packages from day one.
  **Reason**: This will balloon fast (voxel, physics, AI, input). Setting the convention now is cheap.
  **Alternatives considered**: Flat `ecs` package until it hurts — rejected; the split is too obvious to defer.

---

## Open questions / blockers

- [ ] What's the chunk / voxel data model? Per-block entity is a non-starter perf-wise; need a `ChunkComponent` holding packed block IDs.
- [ ] Input handling — straight `InputManager.addMapping` calls, or a `PlayerControlState` that emits intent components?
- [ ] Do we want `sim-ethereal` later for client/server prediction, or stay single-player until much later?

---

## Next session

- [ ] Actually run `./gradlew run` on the Mac and confirm two cubes render.
- [ ] Add a `PlayerControlState` that reads input and writes a `VelocityComponent`.
- [ ] Sketch the chunk component shape (16×16×16? 16×256×16?) before writing any voxel code.

---

## Screenshots / media

<!-- TODO: capture window screenshot after first successful run -->

---

## References

- jMonkeyEngine: <https://jmonkeyengine.org/>
- Zay-ES wiki: <https://github.com/jMonkeyEngine-Contributions/zay-es/wiki>
- Maven Central — jme3-core releases (checked this session for 3.9.0-stable)
- Maven Central — `com.simsilica:zay-es` (checked for 1.6.0)
