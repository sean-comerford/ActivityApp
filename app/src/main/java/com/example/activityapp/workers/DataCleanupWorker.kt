// File: DataCleanupWorker.kt
package com.example.activityapp.workers

import android.content.Context
import androidx.work.*
import com.example.activityapp.data.AppDatabase
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit // Import for TimeUnit

class DataCleanupWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    private val db = AppDatabase.getInstance(context) // Add getInstance() if you need a singleton

    override suspend fun doWork(): Result {
        val retentionDate = getRetentionDate()
        db.activityLogDao().deleteOldLogs(retentionDate)
        db.socialSignalLogDao().deleteOldLogs(retentionDate)
        return Result.success()
    }

    private fun getRetentionDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}

// Scheduling the cleanup
fun scheduleDataCleanup(context: Context) {
    val dataCleanupRequest = PeriodicWorkRequestBuilder<DataCleanupWorker>(1, TimeUnit.DAYS).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "data_cleanup",
        ExistingPeriodicWorkPolicy.KEEP,
        dataCleanupRequest
    )
}
