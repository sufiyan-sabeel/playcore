# Implementation roadmap — safe scope while the licence question is open

Five areas that need no upstream code. Each one lists what is **verified**, what PLAYCORE will build,
what is **unknown about the original** (and therefore not claimed), and the acceptance tests. Nothing
here reproduces upstream behaviour that has not been evidenced.

Evidence codes: **[doc]** = upstream documentation page; **[str]** = string resource in the 1.2.9 APK;
**[man]** = component declared in the APK manifest; **[spec]** = your own requirement document, not
evidence of upstream behaviour.

---

## 1. Per-game profiles and restoration

**Verified.** Profiles are per-game and managed from the Apps area **[doc: modes.md "Manage per-app
profiles and profile transfers through Apps"]**. A separate global set of options lives in the Control
Panel **[str: gsx_help_3_d]**. At least one setting is documented as re-applied at session start and
returned to normal automatically afterwards — saturation **[str: gsx_sat_switch_d, gsx_sat_note]**.
Profile transfer exists between apps **[doc: modes.md]**.

**Unknown about the original.** Which system settings a profile actually writes; the storage format;
whether a profile is applied on foreground detection or on launch; how restoration survives process
death or a reboot; whether profiles are exported individually or as a bundle.

**Build.** Extend the Room schema (already at v2) with `profile_restorations` — one row per setting a
session changed, holding the original value, the applied value, the timestamp and the state
(`APPLIED`, `RESTORED`, `FAILED`, `STALE`). Activation applies only settings the device actually
exposes and the user has granted access to: Do Not Disturb with notification-policy access, brightness
with `WRITE_SETTINGS`, refresh rate where the display supports it. Everything else is stored and shown
as "stored only — needs access", never as applied. On session end, restore from the journal; on next
start, restore anything left `APPLIED` before offering a new session. Never write a setting without
first reading and journalling its original value.

**Acceptance tests.** Unit: the journal state machine (apply → restore, apply → crash → stale recovery,
partial failure leaves no row marked restored). Instrumented: Room migration 2 → 3, and a restore
cycle against a fake settings writer. Manual: change DND for a profile, launch, exit, confirm the
original state returns.

**Gate.** DND and brightness paths need user-granted access; without it the feature degrades to storage
plus an honest status, which is the correct behaviour rather than a fake success.

---

## 2. Overlay service lifecycle

**Verified.** Overlay services are a real part of the product **[man: GameBarService,
GameSessionOverlayService, EnergyCubeSplashService, FreeDisplayPanelService, MiniMonitorService and
others]**. Foreground-service types are declared for special use, media playback, media projection and
microphone **[man]**. Hidden overlays stop drawing and telemetry sampling pauses while the screen is
off **[doc: README, "What's new in ZE2 v25"]**. Dock side and panel size are configurable, and long
panels scroll **[doc: overlays.md]**.

**Unknown about the original.** Restart policy after a process kill; how a panel binds to a game
session; rotation behaviour; whether the panel runs one service per style or one service with a style
mode.

**Build.** A single `OverlayService` with `foregroundServiceType="specialUse"`, a low-priority
notification, and an explicit state machine (`IDLE → STARTING → VISIBLE → PAUSED → STOPPING`). The
`WindowManager` panel that already exists becomes its renderer. `ACTION_SCREEN_OFF` detaches the view
and sets `PAUSED`; `ACTION_SCREEN_ON` reattaches if the session is still active. `onDestroy` always
removes the view — no leaked windows. Starting twice is a no-op; stopping twice is a no-op.

**Acceptance tests.** Unit: the state reducer, including stop-while-starting and screen-off-while-idle.
Instrumented: service starts, notification appears, view attaches, stop removes it, and a second start
does not duplicate the view. Manual: enable the panel, lock the screen, unlock, confirm it returns.

**Gate.** Needs the display-over-other-apps permission. Without it the service refuses to start and
says why.

---

## 3. Backup and restore

**Verified.** Local backup and recovery exist as a documented area **[doc: backup.md, "Keep local
backups under your control"]**; there is no account and no cloud requirement **[doc: privacy.md]**.

**Unknown about the original.** Container format, contents, version field, validation rules, and
whether profiles or media are included.

**Build.** A versioned JSON container written through the Storage Access Framework
(`ACTION_CREATE_DOCUMENT` / `ACTION_OPEN_DOCUMENT`), so no broad storage permission is needed.
Contents: `schemaVersion`, `createdAt`, `appVersion`, library entries, categories, per-game profiles,
and settings. Import validates `schemaVersion`, rejects a newer version with a clear message, applies
inside one Room transaction, and reports counts. No credentials, no device identifiers, no media files.

**Acceptance tests.** Unit: round-trip serialisation, unknown-field tolerance, rejection of a future
schema version, rejection of a truncated file. Instrumented: export then import into a cleared
database reproduces the same row counts.

---

## 4. Task Manager

**Verified.** A Task Manager is named in the upstream README, and process-monitoring services exist
**[man: FloatingProcessService, ThreadMonitorService]**; the brief requires system apps to be protected
from destructive actions **[spec]**.

**Unknown about the original.** What the list contains (all processes, user apps, or games only); what
actions it offers; whether it can act at all without a privileged backend.

**Build.** List only apps the user can act on — never system packages, never PLAYCORE itself — with
memory and process data where Android exposes it without privilege. Force-stop is offered only when a
privileged backend is present; otherwise the row shows "needs Shizuku or root" and the action is
disabled, not silently failing. Freezing is out of scope until a backend exists.

**Acceptance tests.** Unit: the eligibility filter (system packages excluded, self excluded, packages
without a launcher excluded). Instrumented: the list renders and the privileged action is disabled on a
device without a backend.

**Gate.** Any real kill or freeze needs Shizuku or root. Phase 5, not this milestone.

---

## 5. Album

**Verified.** An Album with screenshot organisation is named in the upstream README; the APK requests
`READ_MEDIA_IMAGES` and `READ_MEDIA_VIDEO` **[man]**, and a screen-recording service exists
**[man: ScreenRecordingService]**.

**Unknown about the original.** Grouping rules, filter set, whether it indexes all device media or only
its own output, and whether sharing is in-app or delegated to the system sheet.

**Build.** Query `MediaStore` for images and videos, filtered to the user's chosen scope: PLAYCORE's own
recording folder by default, with an opt-in to the whole gallery under the scoped media permissions.
Group by date and, where a capture's folder or metadata identifies it, by game; otherwise "Ungrouped",
rather than guessing. Filters: type, date, game. Preview in-app; share through `ACTION_SEND`. Deleting
is not offered in this milestone.

**Acceptance tests.** Unit: grouping and sort logic, empty-library state. Instrumented: a `MediaStore`
query returns rows on a device with media present.

**Gate.** Needs the media permission for the gallery scope; the own-folder scope needs nothing.

---

## Sequencing

| Order | Work | Why here | Blocked by |
| --- | --- | --- | --- |
| 1 | Profile restoration journal (schema v3 + state machine) | Highest value, pure logic, testable without a device | Nothing |
| 2 | Overlay service lifecycle | Replaces the current preview-only panel with a real service | Overlay permission to observe |
| 3 | Backup and restore | Self-contained, no permissions, immediately useful | Nothing |
| 4 | Album (own-folder scope first) | Uses standard APIs, honest about scope | Media permission for gallery scope |
| 5 | Task Manager (read-only list) | Low risk, but the useful half needs privilege | Shizuku/root for any action |

## Not building in this milestone

Character Studio, ZEA, Energy Cube card composition, plugin execution, kernel or governor writes,
charging bypass, fan control, saturation control, app freezing, and any behaviour the evidence does not
describe. Each is either upstream identity, blocked on a privileged backend, or unknown enough that
implementing it would be invention rather than parity.
