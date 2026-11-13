# Restoration engine — hardening audit

Audit of the shipped engine before expanding it. Two real defects were found and fixed; both were
reachable through normal use, not only through concurrency.

## 1. Concurrent session safety

**Defect found.** Two sessions could chain on the same setting. Session A applied DND=2 over an
original of 1; session B then applied DND=3 over A's 2. Restoring B put back 2 — an intermediate value
from the middle of the chain — and A's record had been skipped as "changed externally", so nothing
ever restored 1. The device ended in a state the user never chose.

**Policy now implemented.** One owner per target at a time.

- At apply time the engine asks the store for every *open* record on that target across active
  sessions, oldest first.
- If any exist, the new session **inherits the oldest original** (the user's real value) and every
  older record is marked `SUPERSEDED` with a failure code `SUPERSEDED_BY_SESSION`.
- A superseded record is never restored: `needsRestore` is false for it, so recovery and manual
  restore both ignore it.
- If the inherited original is `Unknown`, the new session refuses to apply and records
  `INHERITED_ORIGINAL_UNKNOWN` — no confirmed original means nothing may be changed.
- `startSession` and `restoreSession` both run under a single `Mutex`, so apply, restore and recovery
  cannot interleave inside the process.

**Proven by test.** Two sessions on one target restore the true original; a new session may start while
an older one is unresolved; concurrent starts leave exactly one owner; superseded records are ignored
by recovery.

**Not proven.** Cross-process concurrency. PLAYCORE is single-process today, so the mutex is sufficient,
but a second process would need a database-level lock rather than an in-memory one.

## 2. Startup recovery reliability

| Property | Status |
| --- | --- |
| Idempotent | Yes — settled records are not touched; a second recovery performs zero writes (tested) |
| No duplicate writes on repeated recovery | Tested: write counter unchanged on the second pass |
| New application racing a pending recovery | Serialised by the mutex; tested with `async` on both paths |
| Process death between the platform write and persisting the applied value | Tested: the record stays `PENDING` with `applied == null`, and recovery restores the original |
| Process death during restoration | Tested: the record stays retryable, and the retry settles it |
| Database/system inconsistencies stay visible | `UNRESOLVED` records keep the session `ACTIVE` and are never discarded |

**Second defect found.** After a crash during restore, the platform value was already back to the
original but the record had not been updated. The next attempt compared the current value with the
*applied* value, saw a mismatch and reported `SKIPPED_CHANGED_EXTERNALLY` — leaving the session open
forever with a stale record. The engine now settles a record as `RESTORED` when the current value
already equals the original, before it considers an external change.

## 3. DND correctness

The audit instruction — do not assume DND on/off equals `INTERRUPTION_FILTER_PRIORITY` — is correct and
was acted on. The value is stored as the **integer interruption filter**, never as a boolean, so:

- The original is captured as whatever filter is active: `ALL` (1), `PRIORITY` (2), `NONE` (3) or
  `ALARMS` (4).
- All four are round-tripped exactly, one test per filter.
- Restoration writes back the exact original integer.
- A user change made during the session (for example switching to alarms-only) is detected and left
  alone.
- Permission revoked between apply and restore yields `FAILED_RESTORE` with `PERMISSION_LOST`, the
  value is untouched, and the session stays open for a retry.

**Still a judgement, not a fact:** PLAYCORE applies `INTERRUPTION_FILTER_PRIORITY` when a profile asks
for Do Not Disturb. Upstream's documentation does not say which filter GameSpace sets, so this is a
PLAYCORE choice and is recorded as such.

## 4. Persistence and migration

The migration statements now live in `MigrationSql.V2_TO_V3` and are executed by a **real SQLite
engine** in a JVM test, starting from a v2 fixture built by `MigrationSql.V2_FIXTURE`:

- existing `library_games`, `categories` and `game_profiles` rows survive with their values intact;
- `profile_sessions` and `restoration_records` exist with exactly the columns the entities expect;
- the composite primary key rejects a duplicate `(sessionId, target)`;
- `index_restoration_records_sessionId` exists;
- running the statements twice is harmless.

This proves the SQL, not Room's own migration path. The on-device `MigrationTestHelper` test remains
outstanding and is listed under remaining risks.

Transactional consistency: each record write is a single-row upsert, and the apply sequence is ordered so
a partial write is always recoverable — the snapshot is persisted before the platform is touched, and
the applied value is persisted after. A crash between them leaves a record that recovery can finish.

## 5. Test results

| Suite | Count | Result |
| --- | --- | --- |
| Unit tests (JVM, no device) | 48 | Passed — CI run 43464231514, `BUILD SUCCESSFUL` |
| of which restoration domain | 14 | Passed |
| of which hardening (this audit) | 12 | Passed |
| of which migration (real SQLite) | 2 | Passed |
| of which pre-existing library/profile/filter | 20 | Passed (no regressions) |
| Instrumented tests | 8 | Written, **not run** — no emulator in CI |
| Debug APK | — | `BUILD SUCCESSFUL` in the same run |

New hardening tests: two sessions on one target; new session while older is unresolved; concurrent
starts; duplicate recovery; crash between write and persistence; crash during restoration; permission
revocation; user change to DND; recovery racing a new application; all four interruption filters;
persistence failure never reaching the device; superseded records ignored by recovery.

## 6. Device validation checklist

Automated evidence covers logic, state transitions and SQL. **Nothing below has been verified on a
device.** Do these in order and record the outcome.

1. Apply DND from a game profile. Confirm DND turns on and the message reads "Applied 1 · skipped …".
2. Restore. Confirm the filter returns to what it was — check Settings → Notifications → Do Not Disturb
   shows the previous mode, not merely "off".
3. Set DND to Alarms only first, then apply and restore. Confirm Alarms only comes back, not All.
4. Apply, change DND from the system shade, then restore. Expect "left alone 1" and the shade value
   preserved.
5. Apply, revoke notification-policy access, then restore. Expect a failure count of 1 and no change.
6. Apply, then force-stop PLAYCORE and reopen. Startup recovery should restore the value; check
   `adb logcat` for crashes and confirm the record is settled.
7. Apply two profiles in a row on the same device (two games), then restore both. Confirm the first
   session's record reads `SUPERSEDED` and the device ends at the true original.
8. Install over a build with an existing library (v2 database). Confirm the library and profiles are
   intact after the upgrade — this is the on-device migration check the JVM test cannot replace.
9. Reboot the device with a session still open, then launch PLAYCORE. Confirm recovery runs and the
   value is restored.

## 7. Remaining risks

1. **No device validation.** Every claim above is logic- and SQL-level.
2. **Room's migration path is untested on a device** — only the SQL is proven. An emulator job or a
   manual step is still required.
3. **Restore is manual.** Nothing detects a game ending, so a user can forget to restore. Foreground
   detection (usage access) plus the overlay service is the fix, and it is not built.
4. **Cross-process concurrency** is out of scope while the app is single-process.
5. **Brightness remains unwired** — the adapter supports it, but the profile stores no target level, so
   no control writes it. Refresh rate stays `SKIPPED_UNSUPPORTED`.
6. **The DND filter choice is PLAYCORE's**, not verified upstream behaviour.
7. **`sqlite-jdbc` is a test-only dependency** used to execute migration SQL on the JVM. It is not in
   the APK.
