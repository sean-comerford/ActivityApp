package com.example.activityapp

import android.app.Application
import android.content.Context
import androidx.work.*
import com.example.activityapp.workers.DataCleanupWorker
import java.util.concurrent.TimeUnit
import androidx.work.Configuration
import androidx.work.WorkManager
import android.util.Log


// Inherits from Application()
class AppInitializer : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleDataCleanup(this)
    }

    // Schedules a periodic background task to clean up data from the database
    private fun scheduleDataCleanup(context: Context) {
        // Create a task that will repeat every 15 days
        // PeriodicWorkRequest has a minimum interval of 15 minutes
        val dataCleanupRequest = PeriodicWorkRequestBuilder<DataCleanupWorker>(15, TimeUnit.DAYS).build()


        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "data_cleanup",
            // If task is already scheduled, don't duplicate it. Only one cleanup task will run every 15 days even if the app is restarted
            ExistingPeriodicWorkPolicy.KEEP,
            dataCleanupRequest
        )
    }
}
