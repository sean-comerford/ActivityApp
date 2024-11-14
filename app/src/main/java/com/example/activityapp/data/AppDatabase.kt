package com.example.activityapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// entities are the different tables in this database
@Database(entities = [DailyActivityLog::class, DailySocialSignalLog::class], version = 1)
// AppDatabase extends RoomDatabase().
abstract class AppDatabase : RoomDatabase() {
    // Define instances of the DAOs
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun socialSignalLogDao(): SocialSignalLogDao



    // companion object makes it possible to access the database instance from anywhere in the app
    companion object {
        @Volatile

        // Initialise instance of the AppDatabase class
        private var INSTANCE: AppDatabase? = null

        // Provides a single, shared instance of AppDatabase throughout the app. Ensures only one instance of the database is created
        fun getInstance(context: Context): AppDatabase {
            // synchronized(this) makes sure that only one thread at a time can initialise the database instance.
            return INSTANCE ?: synchronized(this) {
                // Room.databaseBuilder method creates the actual database
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