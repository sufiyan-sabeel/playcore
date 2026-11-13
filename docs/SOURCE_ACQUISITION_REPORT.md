# Source acquisition investigation (Path A)

No code was modified for this investigation, and nothing was obtained. Status: **not acquired**.

## 1. Contact channel

| Channel | Address | Evidence | Quality |
| --- | --- | --- | --- |
| Official channel | t.me/ZUANVFXPROJECT3 | README channel table, "Official channel" | Primary |
| Pre-release news | t.me/ZuanvfxArch | README channel table | Announcements only |
| Discussion group | t.me/zuanvfx01project3chat | README "Feedback and support" | Noisy |
| Kernel Manager group | t.me/zuankernelmanagernextgroup | README channel table | Relevant to the GPL project |
| GitHub issues | /zkmnext-gamespace/issues | 4 open, 0 maintainer replies | Public record, unanswered |
| GitHub profile | github.com/zuanvfx02 | No email, no site, bio only | Not a channel |

Recommendation: Telegram official channel plus one GitHub issue as a dated record.

## 2. The discovery

`zuan-kernel-manager-next` — same account, public, **GPL-3.0**, full LICENSE text (35,149 bytes),
per-file headers "Copyright (c) 2026 ZKM / GPL-3.0-or-later" — contains a `feature/gamespace/`
package that implements the Game Space gaming layer:

| File | Lines | Content |
| --- | --- | --- |
| GameHubOverlay.kt | 1,189 | Energy Cube hub overlay: chevron bands, CPU/GPU gauges, brightness and volume wedges |
| EnergyCubeSplash.kt | 859 | The five-second Energy Cube start animation |
| HandheldMecha.kt | 620 | Handheld design system: cut-corner shapes, spring-press buttons, HUD dialogs |
| HandheldSuperBase.kt | 192 | Super Base deck: Core Reactor, Plugin Library, Game Library, Game Center, Atmosphere, Control Panel |
| CpuModeEngine.kt | 153 | Balance/Dynamic/Performance with real sysfs writes and an honest no-root fallback |
| Handheld*.kt (7 more) | — | Center, Control Panel, Custom Mode, Dialogs, GameSpace, Hub UI, Plugin Library |
| services | — | Crosshair, GameBar, Stopwatch, GameDetector |
| shell layer | — | ShizukuShell, KernelRootRuntime, kernel node policy |

Corrections this forces on the earlier report: navigation-compose **is** used by this author, so the
missing marker in the GameSpace APK is more likely R8 obfuscation than a custom shell; and the design
language reconstructed from screenshots is confirmed in code.

## 3. Licensing evidence

| Artefact | Evidence | Reading |
| --- | --- | --- |
| GameSpace README | Asserts GPL-3.0-or-later | An assertion, not a grant |
| GameSpace LICENSE | Absent; API returns licence: null | No licence text in the repository |
| GameSpace APK | Only AndroidX Apache-2.0 notices | No GPL notice in the binary |
| Native libraries | Five .so, no source, no statement | Unresolved |
| ZKM Next LICENSE | Full GPLv3 text in root | A real grant |
| ZKM Next file headers | Copyright (c) 2026 ZKM / GPL-3.0-or-later | Clear per-file provenance |
| ZEA artwork and marks | No licence found | Excluded from reuse |

Unresolved and not guessed: whether GameSpace's binaries are GPL-3.0; whether the .so files are
covered; whether ZEA is licensed; whether the ZKM/ZKMNEXT names block a rebrand; and whether the GPL
header on the gamespace package is intended to extend to code that originated in GameSpace.

## 4. Recommended next steps

1. Send `SOURCE_REQUEST_MESSAGE.md` to the official Telegram channel; open one GitHub issue with it.
2. Ask whether GameSpace is GPL-3.0 and where corresponding source is published.
3. Ask whether the GPL grant on `feature/gamespace/` extends to a rebranded app.
4. Decide the licence before importing a line: GPL-3.0 or clean-room. There is no middle option.
5. If GPL is acceptable: fork the gamespace package, keep every header, publish PLAYCORE's source.
6. If no reply in ~14 days, proceed under the GPL grant that already exists.
7. Keep Character Studio, ZEA and the ZKM wordmarks out regardless.

## 5. Fallback

Path B stays valid and nothing built so far is wasted. Its ceiling changes: with the GPL source
readable, inferred details have a reference — but only if PLAYCORE adopts GPL, because copying GPL
structure into an Apache-2.0 project is the contamination the licence prevents. Two honest variants:
clean-room Apache-2.0 with screenshots only, or GPL-3.0 with the real source. Choose deliberately.
