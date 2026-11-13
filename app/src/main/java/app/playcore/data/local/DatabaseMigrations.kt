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
    const val VERSION = 2

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

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2)
}
