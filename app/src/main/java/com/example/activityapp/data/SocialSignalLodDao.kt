// File: SocialSignalLogDao.kt
package com.example.activityapp.data

import androidx.room.*

@Dao
interface SocialSignalLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: DailySocialSignalLog)

    @Query("SELECT * FROM daily_social_signal_log WHERE date = :date AND socialSignalType = :socialSignalType")
    suspend fun getLogForSocialSignal(date: String, socialSignalType: String): DailySocialSignalLog?

    @Query("SELECT * FROM daily_social_signal_log WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<DailySocialSignalLog>

    @Query("DELETE FROM daily_social_signal_log WHERE date < :retentionDate")
    suspend fun deleteOldLogs(retentionDate: String)
}
