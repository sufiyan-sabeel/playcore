# Architecture notes

## Shape

One Gradle module (`:app`), packages split by responsibility. Phase 1 does not justify a
multi-module build: Gradle configuration cost and cross-module wiring would arrive before there is
enough code to benefit. The package boundaries below are the seams a later split would cut along.

```
core/model          data types; Metric and Capability contracts
core/designsystem   tokens (colour, type, shape, spacing) + PlaycoreTheme
core/monitoring     DeviceMonitor — platform telemetry only
core/permissions    CapabilityManager — status per capability + settings intents
data/local          Room: GameEntity, CategoryEntity, DAOs, PlaycoreDatabase
data/preferences    SettingsRepository over DataStore
data                InstalledAppScanner, GameRepository
domain              LibraryFilter — pure, unit tested
di                  AppContainer — manual DI
ui                  navigation, components, one package per screen with its ViewModel
```

## Decisions

**Room is the source of truth for the library.** The package manager is consulted only to discover
candidates and to launch. A library entry survives the app being uninstalled; the UI marks it
"Not installed" instead of silently deleting the user's choice.

**Nothing fabricates a reading.** `Metric` has exactly two cases: `Value` and `Unavailable(reason)`.
There is no default, no zero and no estimate. If a device refuses `EXTRA_TEMPERATURE`, the dashboard
says so.

**Capabilities are explicit.** `CapabilityStatus` carries six states (available, permission
required, backend required, device unsupported, temporarily unavailable, unavailable) so a missing
feature is never reported as a broken one.

**Sampling is subscriber-driven.** The dashboard ticker is a cold `Flow` collected with
`WhileSubscribed(5_000)`, so leaving the screen stops the polling. There is no background service in
Phase 1.

**Manual DI.** `AppContainer` holds lazily created singletons. No annotation processor, no generated
code, and the graph is small enough to read in one file.

## Data model

```
library_games(packageName PK, label, isFavorite, categoryId, lastLaunchedAt, addedAt)
categories(id PK autoincrement, name UNIQUE)
```

Version 1. Migrations are declared in `DatabaseMigrations` and destructive fallback is never enabled;
losing a curated library on upgrade is not acceptable.

## Deliberate omissions in Phase 1

- No overlay service, so no `SYSTEM_ALERT_WINDOW` request at startup.
- No background monitoring service.
- No privileged backends: Shizuku and Axeron are detected and reported, never used.
- No custom covers (needs the media permission flow and image caching).
- No strings externalisation: UI copy is inline, so localisation is a Phase 2 task.
