package edu.nd.pmcburne.hwapp.one

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context


// DB class working w/ SQLite storage
@Database(entities = [GameEntity::class], version = 1)
abstract class AppDB : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object { // one db connection only
        @Volatile
        private var INSTANCE: AppDB? = null // return instance or make new one if null

        fun getDatabase(context: Context): AppDB {
            return INSTANCE ?: synchronized(this) {
                // only one thread builds db at a time
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "basketball_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}