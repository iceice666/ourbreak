# ourcraft

A first-person **beach-siege endless survival** game. You play Openclaw the destroyer:
each round an NPC builds a 3D wall around its mascot, and you have a limited time to tear
the whole wall down before the clock runs out. Survive to advance — the wall gets bigger
and the pressure rises every round. Your score is how far you get.

Built with **Java 21 + jMonkeyEngine 3.9 + Zay-ES (ECS) + Lemur (UI)**. MIT licensed.

## Gameplay

📹 **[Gameplay videos (Google Drive)](https://drive.google.com/drive/folders/1PX8SBM3ZS3v7FYbvHv5lShIvldhKZ7L2?usp=sharing)**

## How to play

| Action | Input |
|--------|-------|
| Move | `W` `A` `S` `D` |
| Look | Mouse |
| Attack | Left click |
| Switch weapon | `1` Sword · `2` Gun · `3` Drone (`Q` cycles) |
| Confirm / Back | `Enter` / `Esc` |

- **Sword** sweeps a row of soft blocks. **Gun** one-shots any single block (the clean
  answer to Coral and Jellyfish). **Drone** bombs a 3×3 area — but bombing a Jellyfish
  poisons you and bombing Shells makes them split.
- **Coral** regrows the wall while alive (kill it with the Gun), **Rock** is tanky (use
  the Drone), **Shell** splits under Sword/Drone, **Jellyfish** poisons you if droned.

See the in-game **How to Play** screen for the full reference.

## Build & run

The toolchain is pinned with Nix flakes; enter the dev shell first:

```bash
nix develop          # or: direnv allow (once)
./gradlew run        # play
./gradlew test       # headless unit tests
./gradlew :app:distZip   # portable cross-platform build (build/distributions)
```

Runtime is Java 21. The `distZip` bundles every platform's natives, so a build made on
Linux/WSL also runs on native Windows (`bin/ourcraft.bat`), where mouse-look is a true
captured-cursor FPS.

## Project layout

- `app/src/main/java/com/ourcraft/` — game code (ECS components + systems / AppStates)
- `design/` — GDD / TDD / milestones
- `openspec/` — spec-driven design docs (OpenSpec)
- `devlog/` — per-commit development logs
