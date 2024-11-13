// File: ActivityLogDao.kt
package com.example.activityapp.data

import androidx.room.*

@Dao
interface ActivityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: DailyActivityLog)

    @Query("SELECT * FROM daily_activity_log WHERE date = :date AND activityType = :activityType")
    suspend fun getLogForActivity(date: String, activityType: String): DailyActivityLog?

    @Query("SELECT * FROM daily_activity_log WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<DailyActivityLog>

    @Query("DELETE FROM daily_activity_log WHERE date < :retentionDate")
    suspend fun deleteOldLogs(retentionDate: String)
}
