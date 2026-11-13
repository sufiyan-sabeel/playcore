# PLAYCORE

An independent Android gaming launcher: a library you curate yourself, a device dashboard that
reports real readings, and a capability report that says plainly what the device will and will not
allow. Offline-first, no account, no advertising SDK, no analytics.

**Status: Phase 1 preview (0.1.0).** Phase 1 is the foundation, the game library and the dashboard.
Overlays, gaming mode, input plugins and kernel tuning are later phases and are not stubbed here.

PLAYCORE is not a fork, continuation or endorsed version of any other launcher. It was written from
scratch against publicly documented behaviour.

## What works in this build

| Area | Behaviour |
| --- | --- |
| Library | Every app with a launcher icon can be discovered and added. PLAYCORE never guesses which apps are games; you classify them. |
| Search and sort | Search by name or package; sort by recent, name or favourites-first; filter to favourites and by category. |
| Game details | Icon, name, package, launch, favourite toggle, category assignment, add/remove from the library without uninstalling. |
| Launching | Through the platform launcher intent. Missing apps and packages with no launcher intent produce a typed result and a visible message. |
| Dashboard | Battery level, charge status and temperature, RAM used and available, storage used, network transport, refresh rate, CPU model and Android version. |
| Unavailable values | Shown as "Unavailable" with the reason the platform gave, never as a zero. |
| Settings | Theme (system/dark/light), AMOLED black, accent colour, default sort, default layout, dashboard refresh interval, version, licences. |
| Capabilities | Overlay, usage access, notifications, media, screen capture and Shizuku, each with a status and a working shortcut to the right settings screen. |
| Persistence | Room for library entries and categories; DataStore for preferences. |

## Requirements

- Android 10 (API 29) or newer to install.
- JDK 17 and the Android SDK (compileSdk 35) to build.

## Build

The repository does not commit a Gradle wrapper JAR. Either use a Gradle 8.11.1 installation, or
generate the wrapper once and commit it:

```bash
gradle wrapper --gradle-version 8.11.1   # creates gradlew + gradle/wrapper/
./gradlew :app:assembleDebug
```

Without the wrapper:

```bash
gradle :app:assembleDebug          # debug APK in app/build/outputs/apk/debug/
gradle :app:assembleRelease        # release APK in app/build/outputs/apk/release/
gradle :app:testDebugUnitTest      # JVM unit tests
```

### Signing

A release build is signed with the debug key unless four environment variables are present, in
which case the Gradle script signs with that keystore:

```
PLAYCORE_KEYSTORE_PATH   path to the .jks file
PLAYCORE_KEYSTORE_PASSWORD
PLAYCORE_KEY_ALIAS
PLAYCORE_KEY_PASSWORD
```

The Release workflow reads them from repository secrets `ANDROID_KEYSTORE_BASE64`,
`ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` and `ANDROID_KEY_PASSWORD`. With no secrets set,
the published APK is debug-signed: installable, not store-ready.

## Releases

`.github/workflows/release-apk.yml` builds the release APK on GitHub's runners and publishes it as a
GitHub release. Trigger it by pushing a tag, or by running the workflow manually:

```bash
git tag v0.1.0 && git push origin v0.1.0
```

Or from the Actions tab: **Release APK → Run workflow**, with a version such as `0.1.0`.
Every run also uploads the APK as a workflow artifact and writes `SHA256SUMS.txt`.

`.github/workflows/ci.yml` runs the unit tests and builds a debug APK on every push to `main` and
every pull request.

## Architecture

Single Gradle module, package-separated. That is deliberate for Phase 1: a multi-module build adds
configuration cost and failure modes before there is enough code to justify it. The package
boundaries are the ones a later split would follow.

```
app/src/main/java/app/playcore/
├── core/model/          plain data types and the Metric/Capability contracts
├── core/designsystem/   colour, type, shape and spacing tokens; PlaycoreTheme
├── core/monitoring/     DeviceMonitor (battery, memory, storage, network, display, CPU)
├── core/permissions/    CapabilityManager: status per capability + settings intents
├── data/local/          Room entities, DAOs, database, migration policy
├── data/preferences/    DataStore-backed SettingsRepository
├── data/                InstalledAppScanner, GameRepository
├── domain/              LibraryFilter (pure search/sort/filter, unit tested)
├── di/                  AppContainer (manual dependency injection)
└── ui/                  navigation, components, and one package per screen + ViewModel
```

Dependency injection is manual (`AppContainer`) rather than Hilt: it needs no annotation processor,
adds no build-time code generation, and every dependency is a singleton with an obvious lifetime.
Swapping in Hilt later is a contained change because nothing else constructs these objects.

## Permissions

| Permission | Why | When it is requested |
| --- | --- | --- |
| `ACCESS_NETWORK_STATE` | Report the network transport on the dashboard | At install; reads state only, sends nothing |
| `POST_NOTIFICATIONS` | Foreground-service status in later phases | When a notification feature is used |
| `PACKAGE_USAGE_STATS` | Detect the running game and record play time | When you open usage access from Tools or Settings |
| `SYSTEM_ALERT_WINDOW` | Floating panels and HUDs in later phases | When you enable overlays; never at startup |
| `READ_MEDIA_IMAGES` | Custom game covers in Phase 2 | When you pick a cover |

`QUERY_ALL_PACKAGES` is deliberately **not** declared. A `<queries>` entry for the `MAIN`/`LAUNCHER`
intent is enough to see every app with a launcher icon, which is all the library needs.
There is no `INTERNET` permission: PLAYCORE makes no network requests.

## Testing

- JVM unit tests: `LibraryFilter` search, sort and filter behaviour, plus entity mapping and
  settings defaults. Run with `gradle :app:testDebugUnitTest`; this is what CI executes.
- Instrumented tests (`app/src/androidTest`): Room database behaviour and Compose navigation. These
  need a device or emulator and are **not** run by CI.

See [docs/FEATURE_STATUS.md](docs/FEATURE_STATUS.md) for exactly what is implemented, what is
planned and what has actually been tested.

## Licence

Apache License 2.0. See [LICENSE](LICENSE) and [LICENSES/THIRD-PARTY-NOTICES.md](LICENSES/THIRD-PARTY-NOTICES.md).
