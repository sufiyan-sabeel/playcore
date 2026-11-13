package app.playcore.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration policy for PLAYCORE.
 *
 * Every schema change ships with an explicit [Migration]. Destructive fallback is
 * never enabled: losing a curated library is not an acceptable upgrade path.
 */
object DatabaseMigrations {
    const val VERSION = 3

    /** v1 -> v2: playtime column on library entries, plus per-game profiles. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE library_games ADD COLUMN totalPlayMs INTEGER NOT NULL DEFAULT 0")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `game_profiles` (
                    `packageName` TEXT NOT NULL,
                    `overlayStyle` TEXT NOT NULL,
                    `cubeMode` TEXT NOT NULL,
                    `enabledCards` TEXT NOT NULL,
                    `dndEnabled` INTEGER NOT NULL,
                    `brightnessLock` INTEGER NOT NULL,
                    `refreshRateHz` INTEGER,
                    `cpuMode` TEXT,
                    `notes` TEXT NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`packageName`)
                )
                """.trimIndent()
            )
        }
    }

    /** v2 -> v3: session tracking and per-setting restoration records. */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `profile_sessions` (
                    `id` TEXT NOT NULL,
                    `packageName` TEXT NOT NULL,
                    `startedAt` INTEGER NOT NULL,
                    `endedAt` INTEGER,
                    `state` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `restoration_records` (
                    `sessionId` TEXT NOT NULL,
                    `target` TEXT NOT NULL,
                    `originalKind` TEXT NOT NULL,
                    `originalValue` TEXT,
                    `appliedKind` TEXT,
                    `appliedValue` TEXT,
                    `applyStatus` TEXT NOT NULL,
                    `restoreStatus` TEXT NOT NULL,
                    `failureCode` TEXT,
                    `failureDetail` TEXT,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`sessionId`, `target`)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_restoration_records_sessionId` " +
                    "ON `restoration_records` (`sessionId`)"
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
