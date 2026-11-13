# Feature status — Phase 1

**Verified build:** release v0.1.0, workflow run 43439309038, APK `playcore-0.1.0.apk`
(20,841,700 bytes, sha256 `dae799e2…f57444`), package `app.playcore`, versionCode 3,
minSdk 29, targetSdk 35, signed with the debug key. Three earlier runs failed and were fixed:
a theme type error, then a lintVitalRelease error on an invalid backup rule.

Status vocabulary: Planned, In Progress, Implemented, Tested, Blocked, Not Applicable.
"Implemented" means the code exists and the project builds. "Tested" is used only where a test
actually ran and passed; instrumented tests have not run in CI, so they are listed as Implemented,
not Tested.

| Feature | Module | Status | Source | Test coverage | Test result | Known limitation |
| --- | --- | --- | --- | --- | --- | --- |
| Gradle + Kotlin + Compose configuration | build | Tested | `settings.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml` | CI build | Passed: run 43439309038 built the release APK | No wrapper JAR committed |
| Application class and DI container | app | Implemented | `PlaycoreApplication.kt`, `di/AppContainer.kt` | — | Not run | Manual DI, no Hilt |
| Main activity and theme wiring | app | Implemented | `MainActivity.kt` | NavigationTest (device) | Not run in CI | — |
| Design tokens and theme | designsystem | Implemented | `core/designsystem/Theme.kt` | — | Not run | Two colour schemes only |
| Reusable components | designsystem | Implemented | `ui/components/Components.kt` | — | Not run | No bottom sheet component yet |
| Five-destination navigation | navigation | Implemented | `ui/navigation/PlaycoreApp.kt` | NavigationTest (device) | Not run in CI | Tools and Settings share no sub-nav |
| Installed app discovery | library | Implemented | `data/InstalledAppScanner.kt` | — | Not run | Sees launcher activities only |
| Add / remove library entries | library | Implemented | `data/GameRepository.kt` | PlaycoreDatabaseTest (device) | Not run in CI | — |
| Search, sort, filter | library | Tested | `domain/LibraryFilter.kt` | `LibraryFilterTest` (11 cases) | Passed locally on JVM | Case-insensitive, no fuzzy match |
| Favourites | library | Implemented | `GameRepository.toggleFavorite` | PlaycoreDatabaseTest (device) | Not run in CI | — |
| Categories | library | Implemented | `CategoryDao`, `GameRepository.createCategory` | PlaycoreDatabaseTest (device) | Not run in CI | Create only; no rename or delete UI yet |
| Grid and list layouts | library | Implemented | `ui/library/LibraryScreen.kt` | — | Not run | List/Grid choice is per-session, not persisted |
| Game details screen | library | Implemented | `ui/details/GameDetailScreen.kt` | — | Not run | — |
| Launch installed app | library | Implemented | `GameRepository.launch` | — | Not run | Needs a device to verify |
| Recently launched | library | Implemented | `lastLaunchedAt` in Room | — | Not run | Only records launches made through PLAYCORE |
| Custom covers | library | Planned | — | — | — | Phase 2; needs media permission flow |
| Dashboard real metrics | dashboard | Implemented | `core/monitoring/DeviceMonitor.kt` | — | Not run | CPU utilisation unavailable without privilege |
| Unavailable-state reporting | dashboard | Implemented | `Metric.Unavailable` | `LibraryMappingTest` covers model defaults only | Partially verified | Per-device reasons vary |
| Settings persistence | settings | Implemented | `data/preferences/SettingsRepository.kt` | — | Not run | — |
| Theme and accent switching | settings | Implemented | `SettingsScreen`, `PlaycoreTheme` | — | Not run | — |
| Capability report and shortcuts | permissions | Implemented | `core/permissions/CapabilityManager.kt` | — | Not run | Status only; no privileged operations |
| Overlay / usage / media / capture / Shizuku | permissions | Implemented (status) | same | — | Not run | Detection and settings shortcuts only |
| Room database and DAOs | data | Implemented | `data/local/*` | PlaycoreDatabaseTest (device) | Not run in CI | Version 1, no migrations yet |
| Unit tests | test | Tested | `app/src/test/**` | 15 cases | Passed in GitHub Actions (run 43439309038) | — |
| Instrumented tests | test | Implemented | `app/src/androidTest/**` | 8 cases | Not run (no emulator in CI) | Needs a device |
| CI workflow | ci | Tested | `.github/workflows/ci.yml` | — | Passed (run 1 after the theme fix) | — |
| Release workflow | ci | Tested | `.github/workflows/release-apk.yml` | — | Passed: published v0.1.0, 20,841,700 bytes | Debug-signed without secrets |
| Overlays, gaming mode, input plugins, kernel tuning | later phases | Planned | — | — | — | Phase 4–6 |
