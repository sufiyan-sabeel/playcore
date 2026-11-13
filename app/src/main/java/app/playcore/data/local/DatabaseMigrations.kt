package app.playcore.data.local

import androidx.room.migration.Migration

/**
 * Migration policy for PLAYCORE.
 *
 * - Every schema change ships with an explicit [Migration] here. Destructive
 *   fallback is never enabled, because losing a user's library is not an
 *   acceptable upgrade path.
 * - Version 1 is the initial schema, so there is nothing to migrate yet.
 * - A schema test lives in androidTest (DatabaseMigrationTest) and must be
 *   extended with each new version.
 */
object DatabaseMigrations {
    const val VERSION = 1

    val ALL: Array<Migration> = emptyArray()
}
