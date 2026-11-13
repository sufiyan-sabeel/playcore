package app.playcore.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GameEntity::class, CategoryEntity::class],
    version = DatabaseMigrations.VERSION,
    exportSchema = false,
)
abstract class PlaycoreDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        private const val NAME = "playcore.db"

        fun build(context: Context): PlaycoreDatabase =
            Room.databaseBuilder(context.applicationContext, PlaycoreDatabase::class.java, NAME)
                .addMigrations(*DatabaseMigrations.ALL)
                .build()
    }
}
