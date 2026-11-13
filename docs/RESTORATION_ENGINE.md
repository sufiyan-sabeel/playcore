# Profile restoration engine

Applies a per-game loadout, records what it changed, and puts the device back. Built to be
testable without a device: every platform call goes through one port, every write through another.

## Architecture

```
ui/details/GameDetailViewModel  ──┐
                                  │  startSession(package, desired) / restoreSession(id)
domain/restoration/RestorationEngine
   ├── SystemControlPort  ← system/AndroidSystemControlAdapter   (DND, brightness; refresh rate unsupported)
   └── RestorationStore   ← data/local/RoomRestorationStore      (profile_sessions, restoration_records)
```

The engine is pure Kotlin with no Android import. That is what lets the whole state machine — including
crash recovery — be unit tested with fakes, and it is why the domain tests need no root, no Shizuku and
no device.

### Domain types

| Type | Purpose |
| --- | --- |
| `SystemTarget` | The settings PLAYCORE may touch: `DO_NOT_DISTURB`, `BRIGHTNESS`, `REFRESH_RATE`. An enum with a stable string key, so a typo cannot invent a setting. |
| `TargetValue` | `BooleanValue`, `IntValue`, `TextValue`, or **`Unknown`**. `Unknown` is first-class and is never treated as a confirmed original. |
| `ApplyStatus` | `PENDING_APPLY`, `APPLIED`, `APPLIED_UNVERIFIED`, `SKIPPED_NO_PERMISSION`, `SKIPPED_UNSUPPORTED`, `SKIPPED_UNREADABLE_ORIGINAL`, `FAILED_PERSIST`, `FAILED_APPLY` |
| `RestoreStatus` | `NOT_REQUIRED`, `PENDING`, `RESTORED`, `RESTORED_UNVERIFIED`, `SKIPPED_CHANGED_EXTERNALLY`, `FAILED_RESTORE`, `UNRESOLVED` |
| `ProfileSession` | One game session: id, package, start, end, `ACTIVE`/`COMPLETED` |
| `RestorationRecord` | One setting in one session: original, applied, both statuses, and a persisted `FailureInfo` |
| `FailureInfo` | A stable code plus a human detail, stored so a failure survives a restart |

## Safety order, enforced in the engine

Applying, per target:

1. **Supported?** If the platform has no public setter → `SKIPPED_UNSUPPORTED`, no write.
2. **Permitted?** If the user has not granted the access → `SKIPPED_NO_PERMISSION`, no write.
3. **Read the original.** Unreadable → `SKIPPED_UNREADABLE_ORIGINAL`, no write. An unknown original is
   never applied over, because it could not be restored.
4. **Persist the snapshot.** If the record cannot be stored → `FAILED_PERSIST`, and the device is not
   touched. This is the ordering that matters: storage first, device second.
5. **Apply**, then **read back** where the platform allows it. A mismatch is `APPLIED_UNVERIFIED`, not a
   claimed success.
6. **Record** the applied value, the status and any failure.

Restoring, per record:

1. The record must belong to the session being restored.
2. Read the current value; unreadable → `UNRESOLVED`, kept for inspection.
3. If the current value differs from what PLAYCORE applied, someone else changed it →
   `SKIPPED_CHANGED_EXTERNALLY`, and it is left alone.
4. An `Unknown` original → `UNRESOLVED`, never guessed.
5. Permission revoked since the apply → `FAILED_RESTORE` with `PERMISSION_LOST`.
6. Restore, read back, and record `RESTORED` or `RESTORED_UNVERIFIED`.
7. Repeated calls are no-ops: settled records are not touched, so a second restore performs zero writes.

`FAILED_RESTORE` and `UNRESOLVED` remain **retryable**. That distinction came out of a real test failure
during development: treating a failed restore as settled stranded the device in the changed state,
because the record dropped out of the retry set.

## Recovery

`recoverStaleSessions(staleAfterMs)` walks sessions still `ACTIVE`, restores them, and closes only those
whose records all reached a settled state. Anything unresolved stays `ACTIVE` and visible. It runs once
at startup from `PlaycoreApplication.onCreate` on an IO scope, which is how a crash, a task kill or a
reboot is handled.

## Database

Room v3. `MIGRATION_2_3` adds two tables and one index; no destructive fallback exists.

```
profile_sessions(id TEXT PK, packageName TEXT, startedAt INTEGER, endedAt INTEGER, state TEXT)
restoration_records(sessionId TEXT, target TEXT, originalKind TEXT, originalValue TEXT,
                    appliedKind TEXT, appliedValue TEXT, applyStatus TEXT, restoreStatus TEXT,
                    failureCode TEXT, failureDetail TEXT, updatedAt INTEGER,
                    PRIMARY KEY(sessionId, target))
    index: index_restoration_records_sessionId
```

Values are stored with an explicit kind tag (`BOOLEAN`/`INT`/`TEXT`/`UNKNOWN`) so a boolean can never
come back as the text `true` and be written as the wrong type.

## Files

| File | Role |
| --- | --- |
| `domain/restoration/RestorationModels.kt` | Typed models, statuses, outcomes |
| `domain/restoration/SystemControlPort.kt` | `SystemControlPort` and `RestorationStore` ports |
| `domain/restoration/RestorationEngine.kt` | Apply, restore, recovery, safety rules |
| `data/local/RestorationEntities.kt` | Room entities, codec, mappers |
| `data/local/RestorationDao.kt` | DAO |
| `data/local/RoomRestorationStore.kt` | Store implementation |
| `data/local/DatabaseMigrations.kt` | `MIGRATION_2_3`, version 3 |
| `data/local/PlaycoreDatabase.kt` | Entities and DAO registered |
| `system/AndroidSystemControlAdapter.kt` | DND via `NotificationManager`, brightness via `Settings.System` |
| `di/AppContainer.kt` | Wiring |
| `PlaycoreApplication.kt` | Startup recovery |
| `ui/details/GameDetailViewModel.kt` | Apply/restore actions, profile → desired map |
| `ui/details/GameDetailScreen.kt` | Session panel |
| `app/src/test/java/app/playcore/restoration/RestorationEngineTest.kt` | 14 domain tests |

## Tests

14 domain tests, no device required. Run with `gradle :app:testDebugUnitTest`.

Successful apply and restore · apply rejected · restore rejected then retried · missing permission ·
unsupported target · storage failure aborts before the device is touched · unreadable original ·
repeated restore is a no-op · value changed externally is not overwritten · applied-but-unverified is
recorded honestly · sessions isolated from each other · stale-session recovery after a simulated
process restart · unresolved session kept visible · session-open failure.

## Known limitations

- **Only two targets are writable, and only one is wired from the profile.** DND is applied from
  `GameProfile.dndEnabled`. Brightness is supported by the adapter but the profile stores no target
  level, so nothing is written — the UI says so. Refresh rate is reported `SKIPPED_UNSUPPORTED` because
  Android has no public setter.
- **Session end is not automatic.** Nothing detects the game closing, so restore is a user action. That
  needs foreground-game detection (usage access) and belongs with the overlay service work.
- **DND writes `INTERRUPTION_FILTER_PRIORITY`**, the conventional gaming setting. It is a judgement, not
  an upstream fact.
- **Read-back is not proof.** A platform can accept a write and still not change behaviour; the engine
  records what it can verify and nothing more.
- **No device test has been run.** Everything below is unverified on hardware.

## Manual Android validation

1. Install a build, open a game's details, enable Do Not Disturb in its profile, tap **Apply loadout**.
   Grant notification-policy access if prompted, then confirm DND turns on and the message reads
   "Applied 1 · skipped …".
2. Tap **Restore**. Confirm DND returns to its previous state and the message reads "Restored 1".
3. Apply, then change DND from the system shade, then tap **Restore**. Expect "left alone 1" — the
   external change is not overwritten.
4. Apply, then force-stop PLAYCORE from Android settings, then reopen it. The startup recovery should
   restore the value; check `adb logcat` for no crash and confirm DND is back.
5. Apply with notification-policy access revoked. Expect "skipped 1" and no change to DND.
6. Set a refresh-rate preference in the profile and apply. Expect it counted as skipped with
   `SKIPPED_UNSUPPORTED`, and no change to the display.
