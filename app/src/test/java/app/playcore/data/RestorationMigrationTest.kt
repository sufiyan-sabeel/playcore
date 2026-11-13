package app.playcore.data

import app.playcore.data.local.MigrationSql
import java.sql.DriverManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Executes the real migration statements against a real SQLite engine, starting from a v2
 * fixture. This proves the SQL is valid and preserves existing data without needing a
 * device or an emulator. It does not replace the on-device Room migration test.
 */
class RestorationMigrationTest {

    private fun connection() = DriverManager.getConnection("jdbc:sqlite::memory:")

    @Test
    fun `v2 to v3 keeps library data and adds the restoration tables`() {
        connection().use { conn ->
            conn.createStatement().use { st ->
                MigrationSql.V2_FIXTURE.forEach { st.execute(it) }
                st.execute(
                    "INSERT INTO library_games (packageName, label, isFavorite, categoryId, lastLaunchedAt, addedAt, totalPlayMs) " +
                        "VALUES ('com.game', 'Game', 1, 7, 100, 50, 3600000)"
                )
                st.execute("INSERT INTO categories (name) VALUES ('Racing')")
                st.execute(
                    "INSERT INTO game_profiles (packageName, overlayStyle, cubeMode, enabledCards, dndEnabled, " +
                        "brightnessLock, refreshRateHz, cpuMode, notes, updatedAt) " +
                        "VALUES ('com.game', 'ENERGY_CUBE', 'BALANCE', 'CROSSHAIR', 1, 0, 90, 'Rise', 'note', 42)"
                )
            }

            conn.createStatement().use { st -> MigrationSql.V2_TO_V3.forEach { st.execute(it) } }

            conn.createStatement().use { st ->
                st.executeQuery("SELECT COUNT(*) FROM library_games").use { rs ->
                    rs.next(); assertEquals(1, rs.getInt(1))
                }
                st.executeQuery("SELECT label, isFavorite, totalPlayMs FROM library_games").use { rs ->
                    rs.next()
                    assertEquals("Game", rs.getString(1))
                    assertEquals(1, rs.getInt(2))
                    assertEquals(3600000L, rs.getLong(3))
                }
                st.executeQuery("SELECT COUNT(*) FROM categories").use { rs ->
                    rs.next(); assertEquals(1, rs.getInt(1))
                }
                st.executeQuery("SELECT notes, refreshRateHz FROM game_profiles").use { rs ->
                    rs.next()
                    assertEquals("note", rs.getString(1))
                    assertEquals(90, rs.getInt(2))
                }
            }

            assertEquals(
                listOf("id", "packageName", "startedAt", "endedAt", "state"),
                columns(conn, "profile_sessions"),
            )
            assertEquals(
                listOf(
                    "sessionId", "target", "originalKind", "originalValue", "appliedKind",
                    "appliedValue", "applyStatus", "restoreStatus", "failureCode",
                    "failureDetail", "updatedAt",
                ),
                columns(conn, "restoration_records"),
            )

            conn.createStatement().use { st ->
                st.execute(
                    "INSERT INTO profile_sessions (id, packageName, startedAt, endedAt, state) " +
                        "VALUES ('s1', 'com.game', 1, NULL, 'ACTIVE')"
                )
                st.execute(
                    "INSERT INTO restoration_records (sessionId, target, originalKind, originalValue, " +
                        "appliedKind, appliedValue, applyStatus, restoreStatus, failureCode, failureDetail, updatedAt) " +
                        "VALUES ('s1', 'do_not_disturb', 'INT', '1', 'INT', '2', 'APPLIED', 'PENDING', NULL, NULL, 5)"
                )
            }
            val duplicate = runCatching {
                conn.createStatement().use { st ->
                    st.execute(
                        "INSERT INTO restoration_records (sessionId, target, originalKind, originalValue, " +
                            "appliedKind, appliedValue, applyStatus, restoreStatus, failureCode, failureDetail, updatedAt) " +
                            "VALUES ('s1', 'do_not_disturb', 'INT', '1', 'INT', '2', 'APPLIED', 'PENDING', NULL, NULL, 6)"
                    )
                }
            }
            assertTrue("the composite primary key must reject a duplicate", duplicate.isFailure)

            conn.createStatement().use { st ->
                st.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type = 'index' AND name = 'index_restoration_records_sessionId'"
                ).use { rs -> assertTrue(rs.next()) }
            }
        }
    }

    @Test
    fun `migration statements are idempotent`() {
        connection().use { conn ->
            conn.createStatement().use { st -> MigrationSql.V2_FIXTURE.forEach { st.execute(it) } }
            conn.createStatement().use { st ->
                MigrationSql.V2_TO_V3.forEach { st.execute(it) }
                MigrationSql.V2_TO_V3.forEach { st.execute(it) }
            }
            assertEquals(
                listOf("id", "packageName", "startedAt", "endedAt", "state"),
                columns(conn, "profile_sessions"),
            )
        }
    }

    private fun columns(conn: java.sql.Connection, table: String): List<String> {
        val result = mutableListOf<String>()
        conn.createStatement().use { st ->
            st.executeQuery("PRAGMA table_info($table)").use { rs ->
                while (rs.next()) result += rs.getString("name")
            }
        }
        return result
    }
}
