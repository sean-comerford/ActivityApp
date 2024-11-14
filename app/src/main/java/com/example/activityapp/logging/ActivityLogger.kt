package com.example.activityapp.logging

import com.example.activityapp.data.*
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

// Responsible for logging and updating the duration of different activities and social signals on a daily basis
// Interacts with app's local database, AppDatabase
class ActivityLogger(private val db: AppDatabase) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Function to log activity data
    suspend fun logActivity(activityType: String, durationInSeconds: Int) {
        val date = dateFormat.format(Date())
        val activityLogDao = db.activityLogDao()
        val existingLog = activityLogDao.getLogForActivity(date, activityType)

        if (existingLog != null) {
            val updatedLog = existingLog.copy(durationInSeconds = existingLog.durationInSeconds + durationInSeconds)
            activityLogDao.insertOrUpdate(updatedLog)
            Log.d("ActivityLogger", "Updated activity log: Date: $date, Activity: $activityType, New Duration: ${updatedLog.durationInSeconds} seconds")
        } else {
            val newLog = DailyActivityLog(date = date, activityType = activityType, durationInSeconds = durationInSeconds)
            activityLogDao.insertOrUpdate(newLog)
            Log.d("ActivityLogger", "New activity logged: Date: $date, Activity: $activityType, Duration: $durationInSeconds seconds")
        }
    }

    suspend fun logSocialSignal(socialSignalType: String, durationInSeconds: Int) {
        val date = dateFormat.format(Date())
        val socialSignalLogDao = db.socialSignalLogDao()
        val existingLog = socialSignalLogDao.getLogForSocialSignal(date, socialSignalType)

        if (existingLog != null) {
            val updatedLog = existingLog.copy(durationInSeconds = existingLog.durationInSeconds + durationInSeconds)
            socialSignalLogDao.insertOrUpdate(updatedLog)
            //Log.d("ActivityLogger", "Updated social signal log: Date: $date, Social Signal: $socialSignalType, New Duration: ${updatedLog.durationInSeconds} seconds")
        } else {
            val newLog = DailySocialSignalLog(date = date, socialSignalType = socialSignalType, durationInSeconds = durationInSeconds)
            socialSignalLogDao.insertOrUpdate(newLog)
            //Log.d("ActivityLogger", "New social signal logged: Date: $date, Activity: $socialSignalType, Duration: $durationInSeconds seconds")
        }
    }
}
