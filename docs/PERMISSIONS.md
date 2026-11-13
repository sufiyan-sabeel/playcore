# Permissions and why PLAYCORE asks

PLAYCORE requests nothing at startup. Each row below is requested only when you open the feature
that needs it, and the Tools and Settings screens always show the current status.

| Permission | Feature it enables | Effect if you refuse |
| --- | --- | --- |
| `ACCESS_NETWORK_STATE` | Network transport on the dashboard | The network tile shows "Unavailable" |
| `POST_NOTIFICATIONS` | Foreground-service status (later phases) | No status notifications; nothing else changes |
| `PACKAGE_USAGE_STATS` | Foreground game detection and play time (Phase 4) | No automatic session detection; manual launches still record a timestamp |
| `SYSTEM_ALERT_WINDOW` | Floating panels, HUD, crosshair (Phase 4) | No overlays; the launcher, library and dashboard are unaffected |
| `READ_MEDIA_IMAGES` | Custom game covers (Phase 2) | PLAYCORE keeps using app icons |
| `MediaProjection` consent | Screen recording (Phase 6) | No recording; consent is asked per session |

## Not requested, on purpose

- `QUERY_ALL_PACKAGES` — a `<queries>` intent for `MAIN`/`LAUNCHER` gives visibility of every app
  with a launcher icon, which is all the library needs.
- `INTERNET` — PLAYCORE has no network code.
- `READ_EXTERNAL_STORAGE` — media is reached through scoped permissions and the document picker.
- Accessibility services — nothing in Phase 1 needs one.
- Root, Axeron or Shizuku operations — the backend is detected and reported, never used, in Phase 1.
