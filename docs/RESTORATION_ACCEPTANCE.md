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

## 6. Navigation defect — isolation and closure

The failing test was traced link by link rather than guessed at. The interaction has eight steps, and
each now has its own assertion so a failure names the broken step.

| Step | What must be true | How it is checked | Result |
| --- | --- | --- | --- |
| 1 | Explore screen renders | `PLAYCORE`, `EXPLORE`, `HANDHELD`, `GAMEPAD` displayed | Pass |
| 2 | Library bar item is visible | `onNodeWithTag("nav_explore/library").assertIsDisplayed()` | Pass |
| 3 | User taps it | `performClick()` on the tagged item | Pass |
| 4 | Click handler runs | implied by step 5 | Pass |
| 5 | Navigation dispatched | back stack entry changes | Pass |
| 6 | Destination changes | Home-only marker `CONTINUE PLAYING` count is 0 | Pass |
| 7 | Library enters composition | `ON THE CONSOLE` (header subtitle) exists | Pass |
| 8 | Library content usable | `Search apps` placeholder exists | Pass |

**Root cause.** The test was tapping a *text node* — the caption inside the clickable column — instead of
the navigation item itself. Under instrumentation that tap did not reach the item's click handler, so the
destination never changed. The Super Base test only appeared to pass because the Explore home screen also
carries an in-page "Super Base" button with a colliding label, so its click landed on that button instead
of the bar.

Two genuine layout defects were found and fixed while isolating this, and both were real:

1. `ExploreHomeScreen`'s scrollable body had no `weight(1f)`, so it consumed the remaining height and the
   bottom bar was laid out off-screen — the tap target did not exist where the test was tapping.
2. Adding the weight exposed the second: the root column used `fillMaxWidth()`, so its height was
   unbounded and the weighted body collapsed to zero. Fixed to `fillMaxSize()`.

**Fix, without weakening the test.** The bottom-bar items now carry stable tags (`nav_<route>`), which is
a testability improvement rather than a behavioural change, and the test drives the *actual* navigation
item. Assertions were strengthened, not relaxed: it now also proves Home's marker disappears, which the
previous version never checked. No delays were introduced; `waitForIdle()` only waits for composition.

**Regression.** JVM suite 59/59 (CI run 31), instrumented suite 14/14 (emulator run 16, `Starting 14
tests` → `Finished 14 tests`, BUILD SUCCESSFUL), debug APK BUILD SUCCESSFUL.

## 7. Verdict — Phase 1 complete

All four closure criteria are met: the restoration tests pass, the real Room v2→v3 migration passes on an
emulator, the navigation regression passes on an emulator, and no existing functionality was removed (the
20 pre-existing JVM tests still pass alongside the 39 newer ones).

**Phase 1 is complete.** The limitations in section 5 stand unchanged: restore is still a manual action,
no physical-device run has been performed, cross-process safety is not claimed, and brightness and
refresh rate remain honest skips.
