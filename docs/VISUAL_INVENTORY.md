# PLAYCORE — visual reference inventory and parity checklist

Evidence gathered before any UI rebuild. Every row is graded **Verified** (seen in a real
screenshot or extracted from the shipped APK), **Inferred** (consistent with evidence but not seen),
or **Needs reference** (no evidence yet; must not be guessed).

## 1. Evidence base

| Source | What it yielded |
| --- | --- |
| ZKMNEXT GameSpace 1.2.9 APK (18,881,081 bytes, sha256 73861d7a…c30e1) | 566 string resources, 611 colour resources, 646 dimension resources, 59 services, 28 permissions |
| Extracted app-specific resources | 503 strings not belonging to AndroidX/Material (`gs_*` 160, `gsx_*` 285, `cs_*` 30) |
| `docs/studio-previews/*.png` (5 of 11 viewed) | Actual rendered screens: Character room (dark), Dressing room (light), Settings (light), MTP studio (portrait), Empty room (dark) |
| `docs/*.md` (33 pages) | Documented behaviour, mode names, overlay descriptions |

Only 5 of the 11 published screenshots show UI, and all five are Character Studio screens. The
launcher screens — home, library, Super Base, overlays — have **no** published screenshot.

## 2. Verified information architecture

The application is organised around **three modes** and **one hub**, not around a bottom tab bar.

| Element | Evidence | Grade |
| --- | --- | --- |
| Modes: Explore, Handheld, Gamepad | `gs_mode_explore`, `gs_mode_handheld`, `gs_mode_gamepad`; `gsx_help_7_t` "Handheld or Explore" | Verified |
| Explore = portrait, one-handed; Handheld = landscape console with "Mecha look" | `gsx_help_7_d` | Verified |
| **Super Base** — "Command deck for plugins, stats and atmosphere" | `gsx_sb_title`, `gsx_sb_sub` | Verified |
| Super Base tiles (8): Game Center, Game Library, Plugin Library, Control Panel, Custom Mode, Atmosphere, Core Reactor, Root Tools | `gsx_sb_center`, `_library`, `_plugins`, `_control`, `_custom`, `_atmos`, `_core`, `_root` | Verified |
| Tile subtitles: "Playtime, rank and settings", "Add games to the console", "Helpers for in-game play", "Global profile", "Backdrop and LED lighting", "Tap to switch CPU mode" | same family | Verified |
| Overlay styles (4): Energy Cube, Compact panel, Nostalgia, X Mode | `gs_style_cube`, `_compact`, `_nostalgia`, `_xmode` | Verified |
| Energy Cube card set (19): Counter, Crosshair, Dock side, Performance, Equalizer, Gamepad, Info, Projection, Rotation Lock, Screen Rec, Screenshot, Super Snap, Stopwatch, WiFi, DND, Bypass, Bright lock, No banner, Optimize | `gs_hub_card_*` | Verified |
| Energy Cube modes: Balance, Rise, Eco | `gs_hub_mode_*` | Verified |
| Energy Cube thermal states: Cool, Warm, Hot | `gs_hub_cool`, `_warm`, `_hot` | Verified |
| Energy Cube status lines: "Game Space active", "Standard performance", "Notifications active", "Interruptions blocked", "Recording in progress" | `gs_hub_line_*` | Verified |
| Plugin Library (20 plugins) | `gsx_pn_*` + descriptions in `gsx_pd_*` | Verified |
| Control Panel groups: Display, GPU, Network, Audio, Functions, Game Mode, Screen Refresh Rate | `gsx_cp_*` | Verified |
| Custom Mode: CPU/GPU governor, min/max frequency, cluster scope | `gsx_cm_*` | Verified |
| Atmosphere: 10 backdrops (Aurora, Carbon, Circuit, Grid, Hex, Matrix, Neon, Reactor, Starfield, Vortex), particles, vignette, LED effects (7) | `gsx_bd_*`, `gsx_at_*`, `gsx_led_*` | Verified |
| Genres: All Games, ACT, FPS, MOBA | `gsx_genre_*` | Verified |
| Ranks: Rookie, Veteran, Elite, Master, Legend | `gsx_rank_0..4` | Verified |
| Help topics (7) | `gsx_help_*_t` | Verified |
| Permissions copy: Requirements, Display over other apps, Usage access, Privileged shell, DND access | `gs_perm_*` | Verified |
| Handheld lobby strings: "Your lobby is empty", "Game profile", "Per-game loadout", "Switch to Explore", "Start", "Not played yet", "Can't open this game" | `gs_hh_*` (20 strings) | Verified |
| Thermal protection panel: "Adaptive Thermal Protection", "Performance Lite", "Auto Mode", "Controlled by" | `cs_*` (30 strings) | Verified |
| FPS monitor as a foreground notification | `fps_notification_title` | Verified |

### Plugin Library — verified names

Auxiliary Line · Card Draw Assist · Data Panel · Data Panel (CPS/MPM) · Double Tap Assist ·
Fan Control · Free to Display · Gyro Manipulation · Gravity X · Hunt Mode · Keymap · Macro Recorder ·
MTP · Motivation to Play · Press and Hold Assist · Scout Mode · Screen Extraction ·
High-Sensitivity Wheels · Fast Stopwatch · Superior Pic Quality · Touch Indicator

## 3. Verified visual language

Read from the five rendered screenshots. This is the design language PLAYCORE must reproduce.

| Trait | Observation |
| --- | --- |
| Shape language | Chamfered (cut-corner) panels and parallelogram tabs/buttons with 1px violet strokes. Almost no rounded corners; small badges only. |
| Layout | Landscape-first, full-bleed scene or gradient. Chrome floats over it: header top-left, utility icons top-right, icon rail left, action bar bottom. |
| Header | Angular back chevron in a light frame; bold **italic** title; 9–10px uppercase letterspaced subtitle with a slash separator ("ZKM / CHARACTER SYSTEM"). |
| Section headers | A small parallelogram banner with a ✦ glyph, sitting on the top edge of its panel. |
| Toggles | Parallelogram ON / OFF with a sliding thumb block, violet when on. |
| Tab strip | Row of parallelogram buttons; selected = filled violet with light text, unselected = outlined. |
| Cards | 4-column grid; 1px border, selected card gets a violet outline; caption below in bold italic plus a small status badge ("PREVIEW", "IN USE"). |
| Typography | Heavy geometric **italic** for display; uppercase letterspaced medium for labels; regular 12–13px for body copy. |
| Dark palette | Deep purple-black scene, starfield, violet accents, white display text, grey-violet secondary text. |
| Light palette | Pale lavender-to-white gradient, near-black text, violet accents, white panels. |
| Empty state | Centred glyph, italic headline, one line of body copy, single outlined parallelogram CTA. |
| Bottom bar | Four square icon tiles with tiny uppercase captions beneath. |
| Micro-labels | Corner stamps such as "ZK / 02" and footer stamps "ZKM / ORBITAL COMPANION" in italic micro type. |

## 4. What has no reference yet

No screenshot exists for these, and their layout must not be invented:

1. Explore home screen (the everyday dashboard).
2. Handheld lobby (landscape console).
3. Gamepad mode screen.
4. Super Base hub.
5. Game Library / Apps grid.
6. Game details / profile editor.
7. Energy Cube overlay in-game.
8. Compact panel overlay.
9. Control Panel.
10. Custom Mode.
11. Plugin Library list.
12. Atmosphere screen.
13. Game Center (playtime, rank).
14. Monitor / dashboard.
15. Settings root screen for the launcher (only the Character Studio settings were captured).
16. Album, Task Manager, Backup.

## 5. Visual parity checklist

| Screen | Structure | Layout | Colour | Type | Status |
| --- | --- | --- | --- | --- | --- |
| Character room | Verified | Verified | Verified | Verified | Verified |
| Dressing room | Verified | Verified | Verified | Verified | Verified |
| Character settings | Verified | Verified | Verified | Verified | Verified |
| MTP studio | Verified | Verified | Verified | Verified | Verified |
| Empty room | Verified | Verified | Verified | Verified | Verified |
| Explore home | Verified (strings) | Needs reference | Partially | Partially | Blocked on screenshot |
| Handheld lobby | Verified (strings) | Needs reference | Partially | Partially | Blocked on screenshot |
| Gamepad mode | Verified (strings) | Needs reference | Partially | Partially | Blocked on screenshot |
| Super Base | Verified (8 tiles) | Needs reference | Partially | Partially | Blocked on screenshot |
| Game Library | Verified (strings) | Needs reference | Partially | Partially | Blocked on screenshot |
| Energy Cube | Verified (19 cards) | Needs reference | Partially | Partially | Blocked on screenshot |
| Control Panel | Verified (groups) | Needs reference | Partially | Partially | Blocked on screenshot |
| Plugin Library | Verified (20 plugins) | Needs reference | Partially | Partially | Blocked on screenshot |
| Monitor | Verified (docs) | Needs reference | Partially | Partially | Blocked on screenshot |

## 6. Branding plan

Replace: application name (PLAYCORE), launcher icon (angular hex mark, already shipped), splash,
about screen, notification titles, settings header, build metadata, documentation, release assets.

Do not carry over: the ZEA character, the "ZKM" and "ZKMNEXT" names and wordmarks, the
"ZK / 02" and "ZKM / ORBITAL COMPANION" stamps, character artwork, and the Oxanium-derived logo
treatment. The *pattern* of a slash-separated uppercase subtitle is a layout convention and may be
reused with PLAYCORE wording ("PLAYCORE / GAME SYSTEM").

## 7. Gap between the current build and this reference

The Phase 1 preview shipped a rounded Material 3 dashboard with five bottom-navigation tabs
(Home, Library, Dashboard, Tools, Settings). That is **not** the reference structure: the reference
uses three modes, a Super Base hub of eight tiles, and a chamfered HUD visual language. Bringing the
build to parity is a shell rebuild, not a restyle, and it needs the screenshots in section 4 to be
done without guessing.

## 8. Measured palette

Sampled from the eight published screenshots (dominant and saturated-pixel histograms), so these are
measured values rather than approximations.

| Token | Value | Where it appears |
| --- | --- | --- |
| Dark base | `#181030` | Deepest scene background |
| Dark surface | `#201838` / `#282040` | Panels and mid-scene |
| Dark elevated | `#302048` / `#382850` | Raised panels, card fills |
| Accent violet | `#8060E8` | Selected tabs, ON toggles, CTA fills |
| Accent light | `#A080F0` / `#9880F0` | Hover/secondary fills, gradients |
| Accent deep | `#483090` | Strokes, badge fills, pressed states |
| Light surface | `#F8F8F8` / `#F0F0F8` | Light-mode panels |
| Light tint | `#E8E0F8` / `#F0E8F8` | Light-mode gradient base |
| Secondary text (dark) | `#A0A0B0` | Captions on dark surfaces |

These replace the teal-on-graphite tokens currently in `core/designsystem/Theme.kt`, which were an
invention of the Phase 1 preview and match nothing in the reference.

## 9. Implementation status per screen (build 0.2.0)

Evidence levels are unchanged: VERIFIED means seen in a reference screenshot; INFERRED means the
destination and every label are verified but the composition is PLAYCORE's; BLOCKED means there is
not enough evidence to reproduce it.

| Screen | Evidence | State in build | Notes |
| --- | --- | --- | --- |
| Explore home | INFERRED | Built | Header, mode tabs, session panel, recents, console panel |
| Game library | INFERRED | Built | Adaptive 4-column grid, search, sort tabs, add-app dialog |
| Monitor | INFERRED | Built | Real readings only; unavailable values carry the reason |
| Super Base hub | INFERRED | Built | Eight verified tiles with Ready / Preview badges |
| Game Center | INFERRED | Built | Playtime, rank ladder, most played — all from Room |
| Game Library tile | INFERRED | Built | Library list with playtime and profile count |
| Plugin Library | INFERRED | Built | Twenty verified plugins, each with its access requirement |
| Control Panel | INFERRED | Built | Seven verified groups, gated on backend rather than fake switches |
| Custom Mode | INFERRED | Built | Read-only; states that limits need a privileged shell |
| Atmosphere | INFERRED | Built | Ten verified backdrops, particles, vignette; LED marked unsupported |
| Core Reactor | INFERRED | Built | Balance / Rise / Eco stored; writes flagged as needing privilege |
| Root Tools | INFERRED | Built | Root, Shizuku and kernel capability report |
| Energy Cube screen | INFERRED | Built | Four styles, nineteen cards, real floating-panel preview |
| Handheld lobby | INFERRED | Built | Verified labels: "Your lobby is empty", "Per-game loadout", "Start" |
| Gamepad mode | INFERRED | Built | Real controller detection, focus-order list |
| Help | VERIFIED content | Built | Seven verified topics, bodies written for PLAYCORE |
| Game profile editor | INFERRED | Built | Overlay style, panel mode, DND, brightness lock, refresh rate, cards |
| Settings | INFERRED | Built | Theme, library defaults, monitor interval, permissions, about |
| Character Studio screens | VERIFIED | Out of scope | Character system is upstream identity; PLAYCORE ships none |
| Energy Cube full card layout | BLOCKED | Transport only | The floating panel is real; the 19-card composition is not drawn |

The floating panel is a real `WindowManager` overlay behind the display-over-other-apps permission.
It shows the selected style name and a session marker. It is deliberately not presented as the
finished Energy Cube.
