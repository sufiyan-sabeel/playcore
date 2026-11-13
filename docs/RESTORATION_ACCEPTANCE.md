# Restoration engine — final acceptance

Evidence is separated throughout: **JVM** means the unit tests in `app/src/test`, **emulator** means
`app/src/androidTest` executed on an API 29 x86_64 emulator by the `Instrumented tests` workflow.

## 1. SUPERSEDED ownership — state transitions

One owner per target. A newer session inherits the **oldest** original and every older record becomes
`SUPERSEDED`, which is never restored.

```
A applies T (original O)                B applies T (original = A's applied value vA)
        │                                        │
        ▼                                        ▼
record(A,T): original=O, applied=vA      takeOwnership(T, B)
restoreStatus=PENDING                    ├─ open records = {A}
                                         ├─ inherited original = O      ← the user's value
                                         ├─ A.restoreStatus = SUPERSEDED
                                         └─ record(B,T): original=O, applied=vB, PENDING
                                                        │
        ┌───────────────────────────────┬───────────────┴────────────────┐
        ▼                               ▼                                ▼
B restores                        A restores                     both die → recovery
current == vB                     A is SUPERSEDED                B restores O
→ write O, RESTORED               → nothing to do, session        A has no work, session
→ session COMPLETED                 closes                        closes; one write only
```

Failure branches: `PERMISSION_LOST` → `FAILED_RESTORE`, session stays `ACTIVE`, retry allowed.
`CURRENT_UNREADABLE` → `UNRESOLVED`, kept visible. Supersede write fails → the new session does **not**
apply and records `OWNERSHIP_NOT_RECORDED`, so two owners can never exist.

| Scenario | Expected | Test |
| --- | --- | --- |
| A starts before B | B inherits O | `game A starts before game B…` |
| A ends before B | A restores nothing; B restores O | `game A ends before game B…` |
| B ends before A | B restores O; A then restores nothing | `game B ends before game A…` |
| Both end unexpectedly | Recovery closes both, one write, device at O | `both sessions end unexpectedly…` |
| Crash during restoration | Retry lands on O | `crash during restoration of a superseded chain…` |
| New session while older unresolved | New inherits O, older superseded | `new session starts while an older session is unresolved` |
| Multiple sessions, same target | Exactly one owner, original O | `four sessions on one target…` |

## 2. Startup recovery

| Property | Evidence |
| --- | --- |
| Runs before any conflicting application | `RestorationCoordinator` gate; test asserts the new session inherits O, which is only possible if recovery ran first |
| One pass for concurrent callers | `coordinator performs one recovery pass for concurrent callers` (asserts `activeSessions()` called once) |
| Failed records stay visible | `UNRESOLVED` keeps the session `ACTIVE`; `recovery keeps unresolved sessions visible…` |
| Never restores a superseded record | `recovery never restores a superseded record` |
| No duplicate writes on repeat | `duplicate recovery performs no second write` |
| Failed ownership update cannot lose ownership | `a failed supersede write prevents the apply` |

## 3. Single-process guarantee

The `Mutex` inside `RestorationEngine` and the one-shot gate in `RestorationCoordinator` serialise work
**inside one process only**. Verified by inspection of `AndroidManifest.xml`: one `<application>`, one
`<activity>`, and no `<service>`, `<receiver>`, `<provider>`, `android:process` attribute or
multi-instance Room invalidation service anywhere in the app. The database and the engine therefore
exist in exactly one process.

**Cross-process safety is not claimed.** If a second process is ever added (an overlay service is the
likely candidate in Phase 3), the in-memory mutex stops protecting the data and a database-level lock or
single-owner process design becomes necessary.

## 4. Test matrix — what ran where

| Suite | Where | Count | Result |
| --- | --- | --- | --- |
| Domain, hardening, acceptance, migration SQL | JVM (CI run 28) | 59 | Passed |
| Room v2→v3 migration, DND adapter, recovery over real Room | API 29 emulator (run 14) | 14 total | **13 passed, 1 failed** |

Instrumented detail — all six restoration tests passed on the emulator:

- `roomMigrationFromV2PreservesLibraryData` — real v2 database file opened by Room v3; library, profile
  and category rows intact; new tables usable.
- `dndAdapterReportsSupportAndCapturesTheFilter` — real `NotificationManager` read.
- `dndWriteIsRefusedWithoutPolicyAccess` — a write without policy access reports failure and changes nothing.
- `refreshRateIsReportedUnsupported`.
- `engineOverRealRoomStoreSurvivesAFreshInstance` — persistence plus recovery across a fresh engine.
- `engineRecordsPermissionSkipsOnTheRealAdapter`.

**The one failure:** `NavigationTest.libraryDestinationOpensLibraryScreen` — after tapping the bottom-bar
Library tile, the Library destination does not compose within the test. No app exception appears in the
log. While chasing it I found and fixed two genuine layout defects (the Explore home scroll body had no
weight, so the bottom bar could be pushed off-screen; the root column then had unbounded height, which
collapsed the weighted body). The failure persists after both fixes, so the cause is not yet isolated —
either a remaining layout issue or a test-interaction problem with tapping a caption inside a clickable
column.

## 5. Feature completion status

| Item | Status |
| --- | --- |
| Typed restoration domain | Complete, JVM-verified |
| Safety order (supported → permitted → read → persist → apply → read back → record) | Complete, JVM-verified |
| Single-owner SUPERSEDED policy | Complete, JVM-verified (7 scenarios) |
| Startup recovery with ordering barrier | Complete, JVM-verified; recovery over real Room emulator-verified |
| Room v2→v3 migration | SQL verified on JVM; **Room path emulator-verified** |
| DND capture and restore fidelity (4 filters) | JVM-verified; adapter read/write behaviour emulator-verified |
| Permission revocation handling | JVM-verified |
| Brightness target | Adapter supports it; profile stores no target level, so nothing writes it — deliberate |
| Refresh-rate target | Reported `SKIPPED_UNSUPPORTED` — no public setter |
| Automatic session end | Not built — needs foreground detection (usage access) plus the overlay service |
| Navigation smoke tests | **1 of 3 failing on emulator** |
| Cross-process safety | Not claimed |

## 6. Verdict

The acceptance criteria are **not fully satisfied**. Everything the restoration engine claims is
verified at both levels, including the on-device Room migration, which was the main gap in the previous
audit. But one instrumented navigation test fails, and a failing smoke test on the app shell is not
something to wave through: it may be a real defect in the bottom navigation that a user would hit.

**Phase 1 is therefore not marked complete.** The remaining work is narrow and specific: isolate why the
bottom-bar tap does not navigate under instrumentation, fix it, and re-run the emulator suite to 14/14.
