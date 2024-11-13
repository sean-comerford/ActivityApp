// File: AppDatabase.kt
package com.example.activityapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DailyActivityLog::class, DailySocialSignalLog::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun socialSignalLogDao(): SocialSignalLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "activity_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
