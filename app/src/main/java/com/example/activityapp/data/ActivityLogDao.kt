// File: ActivityLogDao.kt
package com.example.activityapp.data

import androidx.room.*

// Define the Data access object for the activity table
@Dao
interface ActivityLogDao {
    // If there is a conflict, i.e. entry with the same primary key already exists), existing entry will be replaced by the new one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // Function to insert or update log
    suspend fun insertOrUpdate(log: DailyActivityLog)

    @Query("SELECT * FROM daily_activity_log WHERE date = :date AND activityType = :activityType")
    suspend fun getLogForActivity(date: String, activityType: String): DailyActivityLog?

    @Query("SELECT * FROM daily_activity_log WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<DailyActivityLog>

    @Query("DELETE FROM daily_activity_log WHERE date < :retentionDate")
    suspend fun deleteOldLogs(retentionDate: String)
}
