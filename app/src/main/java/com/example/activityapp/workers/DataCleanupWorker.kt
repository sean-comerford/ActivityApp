package com.example.activityapp.workers

import android.content.Context
import androidx.work.*
import com.example.activityapp.data.AppDatabase
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit // Import for TimeUnit
import android.util.Log

// Defines background worker that periodically deletes files older than 30 days from the database.
// Coroutine subclass, designed to run in the background
class DataCleanupWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    // Get the singleton instance of the database
    // Singleton instance: Only one instance of the class can exist in the application
    private val db = AppDatabase.getInstance(context)

    override suspend fun doWork(): Result {
        Log.d("ActivityLogger", "Data cleanup has started at ${System.currentTimeMillis()}")
        try {
            // Calculate half of the entries for Activity Log
            val activityCount = db.activityLogDao().getCount()
            val activityHalfCount = activityCount / 2
            db.activityLogDao().deleteOldestHalf(activityHalfCount)

            // Calculate half of the entries for Social Signal Log
            val socialSignalCount = db.socialSignalLogDao().getCount()
            val socialSignalHalfCount = socialSignalCount / 2
            db.socialSignalLogDao().deleteOldestHalf(socialSignalHalfCount)

            Log.d("ActivityLogger", "Data cleanup has been performed at ${System.currentTimeMillis()}")

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("ActivityLogger", "Data cleanup failed: ${e.message}")
            return Result.failure()
        }
    }
}