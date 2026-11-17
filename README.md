# Game Portal

Android app with common puzzle games: Tic-Tac-Toe, 15 Puzzle, Memory Match,
2048, Minesweeper, and Lights Out. Built with Kotlin, Views, and Gradle
(AGP 9, Kotlin via AGP built-in Kotlin support).

Tic-Tac-Toe has a built-in opponent (Easy / Medium / Hard minimax). The 15
Puzzle tracks moves and time with a best score; 2048 tracks score with a best
score; Minesweeper has flags (long-press) and a best time; Lights Out generates
always-solvable boards with a best move count. All games survive device
rotation.

## Build

```
./gradlew.bat assembleDebug        # local dev build (needs Android SDK, see local.properties)
./gradlew.bat assembleRelease      # release build (R8-minified)
./gradlew.bat test                 # JVM unit tests for the pure game logic
```

## Releases

CI (`.github/workflows/release.yml`) builds a release APK and creates a
GitHub release with the APK attached whenever a tag is pushed. Tags must match:

```
v<major>.<minor>.<patch>[-suffix]      e.g. v1.0.0, v1.2.3-beta, v2.0.0-rc1
suffix: lowercase letters/digits, 1-5 chars
```

Version name/code are derived from the tag (`versionName` = tag without `v`,
`versionCode` = numeric digits of the version). Invalid tags fail the build.
