# DRAFT — not published

**Status: blocked. Do not publish until the licence condition in section 6 is satisfied.**

This is the release text prepared for an *unchanged upstream APK* mirror, ready to use the moment
redistribution is authorised. It exists so that the only remaining step is a decision, not more work.

## Category

**Unchanged upstream release.** The asset is byte-identical to the file published at
`github.com/zuanvfx02/zkmnext-gamespace/releases/tag/1.2.9`. Nothing was rebuilt, re-signed, patched,
renamed inside the APK, or modified in any way. It is not a PLAYCORE build and carries no PLAYCORE
branding.

## Title

ZKMNEXT GameSpace 1.2.9 — official upstream APK (mirror)

## Asset

`app-release.apk` — 18,881,081 bytes
SHA-256 `73861d7a58f8dc0e014f57fa380641027a98b895b3fad737e394fa03949c30e1`

Verify before installing:

```bash
sha256sum app-release.apk
```

## What it is

GameSpace is an Android gaming launcher by **zuanvfx02** (ZUANVFX), with a game library, in-game
overlays, input plugins, device monitoring and a character companion. Package
`com.zkmnext.gamespace`, versionName 1.2.9, versionCode 14, minSdk 29 (Android 10), targetSdk 35,
arm64-v8a and armeabi-v7a.

## Attribution

Original application, documentation and releases: **zuanvfx02** —
https://github.com/zuanvfx02/zkmnext-gamespace. Official channel:
https://t.me/ZUANVFXPROJECT3. This mirror is not produced, endorsed or supported by the original
developer, and it is not an official ZKMNEXT release channel.

## Install

```bash
adb install app-release.apk
```

Or copy the file to the device and open it, allowing installs from that source. Android 10 or newer.

## Compatibility and known limitations

- Minimum Android 10; several features need newer versions or specific hardware.
- Overlay features need the display-over-other-apps permission; monitoring needs usage access.
- Kernel, charging and fan controls need root or Shizuku and hardware that exposes those interfaces.
- The APK is signed with a self-signed certificate (SHA-256
  `B601FB0F…9EBA`), so Play Protect may warn on sideloading. Verify the hash above before installing.
- Published upstream issue reports mention the high-sensitivity wheel and small-window launch
  problems in 1.1.0; this mirror makes no claim about fixes beyond what upstream shipped.

## Licence and redistribution status

The upstream README asserts GPL-3.0-or-later. The repository currently contains no LICENSE file: a
GPLv3 licence was present from the initial commit on 21 Oct 2026 and deleted on 29 Oct 2026, before
the first APK was published. No corresponding source has been published for any release.

**Section 6 — the blocker.** Publishing this APK requires satisfying GPL-3.0 section 6 (source
accompaniment or a valid written offer) or obtaining explicit written permission from the copyright
holder. Neither is available today. Until one is, this release stays unpublished.
