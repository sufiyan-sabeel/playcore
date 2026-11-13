# Source and fidelity report

Answers to the eight questions, recorded before further implementation. No code was changed for this report.

## 1. Is the original source code publicly available?

No. The repository at commit 8d183e8 contains 66 files and 3.2 MB: a README, 33 markdown pages, the
documentation site, eleven screenshots and one font licence. A recursive search for `.kt`, `.java`,
`.gradle`, `.xml`, `.so`, `.apk` and `.smali` returns nothing, and GitHub's language endpoint returns
an empty set. The README states: "The current development source is not publicly published."

## 2. Can the original developer provide it?

Unknown until they are asked. The README asserts GPL-3.0-or-later; if that is accurate, recipients of
the APKs already hold a licence to the corresponding source. If it is not accurate — the repository
ships no LICENSE file, and the APK carries only AndroidX Apache-2.0 notices — reuse requires explicit
permission. Request: the Gradle project, Compose sources and resources, Room schema, service and plugin
implementations, native sources for the five `.so` files, build configuration, a written licence
statement, and written permission for any identity asset. Not legal advice.

## 3. What exact language and frameworks are verified?

| Component | Evidence | Confidence |
| --- | --- | --- |
| Kotlin | `kotlin/` and `Lkotlin/` descriptors, kotlinx.coroutines, Compose | Strong |
| Jetpack Compose | 103 `androidx/compose/` references, no XML layouts in `res/` | Certain |
| Room | `androidx/room/`, `MultiInstanceInvalidationService` declared | Certain |
| DataStore | `androidx/datastore/`, `libdatastore_shared_counter.so` | Certain |
| WorkManager | 18 `androidx/work/` references, `SystemJobService` declared | Certain |
| Coil 3 | `coil3/` | Certain |
| Gson | `com/google/gson/` | Certain |
| DI | No dagger, hilt or koin markers — manual composition root | Strong |
| Navigation library | No `androidx/navigation/` marker — hand-rolled or obfuscated | Inferred |
| Shizuku / Axeron | `moe.shizuku` API_V23 permission, `frb.axeron` MANAGER permission, 121 `frb/axeron` refs | Certain |
| Native C/C++ | `libgamespace_core`, `libgamespace_daemon`, `libgamespace_touch`, `libzkm_engine`, `libzkmintegrity` | Certain |
| Accessibility service | 5 `android/accessibilityservice/` refs, `GameMagnificationService` | Strong |
| Screen capture | MediaProjection refs, `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Certain |
| Dynamic payload | `assets/main.jar` holds a second `classes.dex`; `assets/language_en.xml` | Certain |
| Build targets | minSdk 29, targetSdk 35, compileSdk 37, versionCode 14 | Certain |
| Ads / analytics | No Firebase, Play ads, AppsFlyer or Adjust markers | Strong |

PLAYCORE already matches the verified stack, so a later source drop would not force a rewrite.

## 4. Which parts can legally be reused?

Nothing from the APK — code, dex, native libraries or ZEA artwork — without permission. Strings, colour
values and screen names may be used as reference facts, which is what the inventory does. AndroidX,
Kotlin, Compose, Room and DataStore are Apache-2.0 with notices preserved; Oxanium is SIL OFL 1.1.
If GPL source arrives, reuse is possible but PLAYCORE would then have to be GPL-3.0 with corresponding
source published, which contradicts the Apache-2.0 choice currently in the repository.

## 5. Can the original UI be preserved?

Only to the depth of the evidence. Five published screenshots show real screens, all Character Studio.
They establish the design language — chamfered panels, parallelogram controls, ✦ banners, italic
display type, measured violet palette — and PLAYCORE reproduces it. There is no screenshot of Explore
home, Handheld lobby, Super Base, Energy Cube over a game, Control Panel or the library grid, so those
screens are inferred, not preserved.

## 6. Which features can be reproduced independently?

Library, launching, profiles, playtime, categories and favourites: yes. Three modes, Super Base, eight
tiles, atmosphere, help, settings: yes. Monitoring of battery, RAM, storage, network, refresh rate and
CPU model: yes. GPU load, per-core frequencies and non-battery temperatures: device-dependent. Overlay
transport, four styles and the nineteen-card inventory: yes; the in-game composition needs a screenshot.
Plugin inventory and per-game configuration: yes; execution needs controllers or privileged access.
Performance and thermal writes, fan, bypass, saturation: need a privileged shell and exposed hardware.
Task Manager listing: yes; freezing needs root or Shizuku. Album, music and notes: yes, on standard
APIs. Backup and diagnostics: yes, though the original format is undocumented.

## 7. Which features need privileged access?

| Feature | Needs | Why |
| --- | --- | --- |
| Overlays, crosshair, FPS HUD | `SYSTEM_ALERT_WINDOW` | User-granted permission, not privilege |
| Game detection, playtime | `PACKAGE_USAGE_STATS` | User-granted usage access |
| DND, brightness, rotation lock | `ACCESS_NOTIFICATION_POLICY`, `WRITE_SETTINGS` | Special access |
| Recording, projection | MediaProjection consent | Per-session consent |
| Keymap, Macro, Shoulder, Gravity X | Shizuku/root or AccessibilityService | `INJECT_EVENTS` is signature-level |
| Governors, frequency limits, schedulers | Root or Shizuku | sysfs writes outside the sandbox |
| Fan, bypass, saturation, LED | Root plus exposed hardware | Kernel nodes vary per device |
| App freezing, background limiter | Root or Shizuku | Cannot force-stop other apps |
| Per-process monitoring | Root or Shizuku | `/proc` visibility is restricted |

## 8. Best route to the closest possible recreation

**Path A** — obtain the source. Then PLAYCORE is a rebrand of the real codebase: keep the
implementation, change name, icon, splash, about and notification titles, drop ZEA and the ZKM
wordmarks, keep every notice, publish corresponding source if GPL. Days, not months, and exact by
construction.

**Path B** — source stays unavailable. The ceiling becomes "indistinguishable in structure and
behaviour", never "identical". Three things raise it, in order of value: four screenshots or one
recording (Explore home, Super Base, Handheld lobby, Energy Cube over a game); a device with the app
installed; and a written definition of the six features the reference names but never explains.

## What PLAYCORE will not do

Call an independent implementation the original source. Copy dex, native libraries or ZEA artwork.
Invent Axeron or kernel APIs. Mark parity without a test result. Present inferred layouts as verified.
