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
            MigrationSql.V2_TO_V3.forEach { db.execSQL(it) }
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}

/**
 * The exact statements the migrations run, kept as data so a plain JVM test can
 * execute them against a real SQLite engine and prove they produce the schema the
 * entities expect — without a device or an emulator.
 */
object MigrationSql {
    val V2_TO_V3: List<String> = listOf(
        """
        CREATE TABLE IF NOT EXISTS `profile_sessions` (
            `id` TEXT NOT NULL,
            `packageName` TEXT NOT NULL,
            `startedAt` INTEGER NOT NULL,
            `endedAt` INTEGER,
            `state` TEXT NOT NULL,
            PRIMARY KEY(`id`)
        )
        """.trimIndent(),
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
        """.trimIndent(),
        "CREATE INDEX IF NOT EXISTS `index_restoration_records_sessionId` " +
            "ON `restoration_records` (`sessionId`)",
    )

    /** The v2 schema, used to build a real v2 fixture for the migration test. */
    val V2_FIXTURE: List<String> = listOf(
        """
        CREATE TABLE IF NOT EXISTS `library_games` (
            `packageName` TEXT NOT NULL,
            `label` TEXT NOT NULL,
            `isFavorite` INTEGER NOT NULL DEFAULT 0,
            `categoryId` INTEGER,
            `lastLaunchedAt` INTEGER,
            `addedAt` INTEGER NOT NULL DEFAULT 0,
            `totalPlayMs` INTEGER NOT NULL DEFAULT 0,
            PRIMARY KEY(`packageName`)
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS `categories` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `name` TEXT NOT NULL
        )
        """.trimIndent(),
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)",
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
        """.trimIndent(),
    )
}
