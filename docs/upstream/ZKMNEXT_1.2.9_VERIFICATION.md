# ZKMNEXT GameSpace 1.2.9 — clone and APK verification record

Verification only. Nothing was modified, rebuilt or published. The APK was downloaded read-only and
hashed; the upstream repository was cloned and read.

## Phase 1 — clone result

| Item | Value |
| --- | --- |
| Remote | https://github.com/zuanvfx02/zkmnext-gamespace.git |
| Branch | `main` (only branch; `origin/HEAD` → `origin/main`) |
| Commit | `8d183e87dbc163053fde8d782f9f88df2b686bfc` |
| Commit author | ZUANVFX02 <zuanvfx01github@gmail.com> |
| Commit date | Thu 12 Nov 2026 15:01:17 +0700 |
| Commit subject | "Add files via upload" |
| History | 23 commits, 66 tracked files, 68 paths ever present |
| Tags | 1.0.0, 1.1.0, 1.2.0, 1.2.1, 1.2.9 (note: tag 1.2.0 has no matching release) |
| Android Gradle project | **None.** No `.kt`, `.java`, `.gradle`, `.kts`, `.so` or `gradlew` file has ever existed in any of the 23 commits |

The tag-to-commit map is not one tag per build: 1.0.0 and 1.1.0 point at `911e6ff`, while 1.2.0,
1.2.1 and 1.2.9 all point at `8bb5848`. The repository is a release channel, not a source tree.

## Phase 2 — official APK verification

| Item | Value |
| --- | --- |
| Release | tag `1.2.9`, published 9 Nov 2026 |
| Asset name | `app-release.apk` (the same filename is reused in all four releases) |
| Size | 18,881,081 bytes |
| SHA-256 | `73861d7a58f8dc0e014f57fa380641027a98b895b3fad737e394fa03949c30e1` |
| Publisher checksum | **None published.** GitHub's stored asset digest is `sha256:73861d7a…c30e1`, computed by GitHub at upload, and it matches our computed hash |
| Archive integrity | `zipfile.testzip()` clean, 771 entries, no corrupt member |
| Package | `com.zkmnext.gamespace` |
| Label | GameSpace |
| versionName / versionCode | 1.2.9 / 14 |
| minSdk / targetSdk | 29 (Android 10) / 35 |
| ABIs | arm64-v8a, armeabi-v7a |
| DEX | single `classes.dex`, plus a second dex inside `assets/main.jar` |
| Signature | v1, v2 and v3 all present |
| Signing certificate SHA-1 | `4D:D3:E4:4D:7F:32:19:28:8C:A0:6A:A7:90:DB:CF:67:24:F9:F7:A8` |
| Signing certificate SHA-256 | `B601FB0F67D7D67A190E51E34893CCEE0B0C491F76C281CF6F21E1095DAD9EBA` |
| Certificate validity | 4 Nov 2026 → 29 Oct 2051, CN=release, self-signed |

Other releases, for completeness: 1.0.0 (31 Oct 2026, 14,685,293 B, 5,341 downloads), 1.1.0
(3 Nov 2026, 16,027,980 B, 9,831), 1.2.1 (8 Nov 2026, 18,754,105 B, 1,987), 1.2.9 (9 Nov 2026,
18,881,081 B, 9,010).

## Phase 3 — source availability decision

**Case B applies: the Android development source does not exist in the repository or its history.**
Therefore, per the brief: no Gradle project was invented, nothing was compiled, documentation was not
treated as source, no APK was fabricated, and no workflow was created that pretends to build one.

## The licensing finding that blocks redistribution

The git history contains a decisive commit:

| Commit | Date | Subject | Effect |
| --- | --- | --- | --- |
| `505219d` | 21 Oct 2026 | Initial commit | Repository created **with a GPLv3 LICENSE file, 674 lines** |
| `f9581ae` | 29 Oct 2026 | "Delete LICENSE" | LICENSE removed — 674 deletions |
| `911e6ff` | 31 Oct 2026 | README revision | First APK release (1.0.0) published **two days after the licence was deleted** |

So: the project carried a real GNU GPL v3 licence file for eight days before any binary was
distributed, then removed it, and every APK ever published — 1.0.0 through 1.2.9 — shipped from a
repository with no licence file. No tag contains a LICENSE. The README still asserts
GPL-3.0-or-later and still says the corresponding-source obligation remains in force, while the APK
itself carries no GPL notice (only AndroidX Apache-2.0 notices under META-INF).

Two readings are possible and neither authorises a mirror:

1. **The project is GPL-3.0-or-later** (as the README states). Then redistribution is permitted, but
   GPL-3.0 section 6 requires the conveyed binary to be accompanied by the corresponding source, a
   valid written offer, or an offer relayed from someone entitled to give one. No public source
   exists, so none of those can be satisfied.
2. **No licence applies** (the file was deleted before release). Then the work is all rights
   reserved and redistribution is not authorised at all.

Under either reading, publishing the APK as a GitHub release in another repository would be a
licence violation or an unauthorised distribution. That is why nothing was published.

## Phase 4 — destination repository state (inspected, unmodified)

| Item | Value |
| --- | --- |
| Destination | github.com/sufiyan-sabeel/playcore (owner permissions: admin) |
| Branch | `main` at `127e683` |
| Tags | `v0.1.0`, `v0.2.0` — both PLAYCORE's own builds |
| Releases | v0.1.0 and v0.2.0, PLAYCORE only; no upstream mirror exists |
| Action taken | None. No branch, tag, release or asset was created, overwritten or deleted |
