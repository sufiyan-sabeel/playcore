# Feature status — Phase 1 (build 0.2.0)

**Verified build:** Android CI run 43446896635 — 20 JVM unit tests passed, debug APK assembled.
Shell rebuilt to the reference: three modes, eight-tile Super Base, violet console design system.

Status vocabulary: Planned, In Progress, Implemented, Tested, Blocked, Not Applicable.
"Tested" is used only where a test actually ran and passed. Instrumented tests need a device and
are therefore Implemented, not Tested.

| Feature | Module | Status | Source | Test | Evidence level |
| --- | --- | --- | --- | --- | --- |
| Three-mode shell (Explore / Handheld / Gamepad) | navigation | Implemented | `ui/navigation/PlaycoreApp.kt` | NavigationTest (device) | Inferred |
| Violet console design system | designsystem | Implemented | `core/designsystem/Theme.kt`, `Shapes.kt` | — | Verified tokens |
| Chamfered panels, parallelogram tabs and toggles | components | Implemented | `ui/components/Components.kt` | — | Verified |
| Super Base hub, eight tiles | superbase | Implemented | `ui/superbase/SuperBaseScreen.kt` | NavigationTest (device) | Inferred |
| Game Center (playtime, rank ladder) | superbase | Implemented | `ui/superbase/TileScreen.kt` | — | Inferred |
| Plugin Library, twenty verified plugins | superbase | Implemented | `ReferenceContent.kt`, `TileScreen.kt` | ProfileMappingTest | Inferred |
| Control Panel, seven verified groups | superbase | Implemented | `TileScreen.kt` | — | Inferred |
| Custom Mode (read-only, gated) | superbase | Implemented | `TileScreen.kt` | — | Inferred |
| Atmosphere: ten backdrops, particles, vignette | superbase | Implemented | `TileScreen.kt`, `ConsoleBackground.kt` | — | Verified names |
| Core Reactor: Balance / Rise / Eco | superbase | Implemented | `TileScreen.kt`, settings | ProfileMappingTest | Verified names |
| Root Tools capability report | superbase | Implemented | `CapabilityManager.kt` | — | Inferred |
| Energy Cube screen, four styles, nineteen cards | overlay | Implemented | `ui/overlay/EnergyCubeScreen.kt` | ProfileMappingTest | Verified names |
| Floating panel transport (WindowManager) | overlay | Implemented | `overlay/OverlayController.kt` | — | Phase 3 layer |
| Handheld lobby | handheld | Implemented | `ui/handheld/HandheldScreen.kt` | — | Inferred |
| Gamepad mode with controller detection | gamepad | Implemented | `ui/gamepad/GamepadScreen.kt` | — | Inferred |
| Help, seven topics | help | Implemented | `ui/help/HelpScreen.kt` | — | Verified content |
| Game library: discovery, add, remove, launch | library | Implemented | `data/GameRepository.kt`, `LibraryScreen.kt` | PlaycoreDatabaseTest (device) | Inferred |
| Search, sort, filter | library | Tested | `domain/LibraryFilter.kt` | LibraryFilterTest (11) | — |
| Per-game profiles | data | Tested | `GameProfileEntity`, `GameDetailViewModel` | ProfileMappingTest (5) | Verified names |
| Profile restoration engine | restoration | Tested | `domain/restoration/*`, `data/local/RoomRestorationStore.kt` | RestorationEngineTest (14) | Passed in CI (run 43459317835) |
| Stale-session recovery at startup | restoration | Implemented | `PlaycoreApplication.kt`, `RestorationEngine.recoverStaleSessions` | RestorationEngineTest (2 cases) | Domain only; not run on a device |
| Room schema v3 migration | data | Implemented | `DatabaseMigrations.MIGRATION_2_3` | — | Migration SQL only; no device test |
| Room schema v2 with real migration | data | Implemented | `DatabaseMigrations.kt` | PlaycoreDatabaseTest (device) | — |
| Playtime and rank | data | Tested | `GameDao.addPlaytime`, rank function | ProfileMappingTest, mapping tests | — |
| Monitor: real device readings | monitoring | Implemented | `core/monitoring/DeviceMonitor.kt` | — | — |
| Settings persistence | preferences | Implemented | `SettingsRepository.kt` | Mapping tests | — |
| Capability report incl. root probe | permissions | Implemented | `CapabilityManager.kt` | — | — |
| Energy Cube full card layout | overlay | Blocked | — | — | Blocked on screenshot |
| Overlay service for in-game sessions | overlay | Planned | — | — | Phase 3 |
| Input plugins (execution) | plugins | Planned | — | — | Phase 5 |
| Task Manager, Album, Backup | tools | Planned | — | — | Phase 6 |
| Character system | — | Not Applicable | — | — | Upstream identity |

## Tests

- `LibraryFilterTest` — 11 cases, search / sort / filter combinations. Passed in CI.
- `LibraryMappingTest` — 4 cases, entity mapping and settings defaults. Passed in CI.
- `ProfileMappingTest` — 5 cases, profile round trip, unknown enum fallback, reference content counts. Passed in CI.
- `PlaycoreDatabaseTest` — 5 instrumented cases, Room CRUD and category clearing. Needs a device.
- `NavigationTest` — 3 instrumented cases, shell, library and Super Base. Needs a device.
