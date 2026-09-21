package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        QuranBookmarkEntity::class,
        ReadingHistoryEntity::class,
        AzkarProgressEntity::class,
        DuaaFavoriteEntity::class,
        PrayerLogEntity::class,
        UserSettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class NoorDatabase : RoomDatabase() {

    abstract fun noorDao(): NoorDao

    companion object {
        @Volatile
        private var instance: NoorDatabase? = null

        fun getInstance(context: Context): NoorDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoorDatabase::class.java,
                    "noor_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
