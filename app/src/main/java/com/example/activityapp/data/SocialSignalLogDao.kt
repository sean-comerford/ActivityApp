package com.example.activityapp.data

import androidx.room.*

// Define the Data access object for the social signal table
@Dao
interface SocialSignalLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: DailySocialSignalLog)

    @Query("SELECT * FROM daily_social_signal_log WHERE date = :date AND socialSignalType = :socialSignalType")
    suspend fun getLogForSocialSignal(date: String, socialSignalType: String): DailySocialSignalLog?

    @Query("SELECT * FROM daily_social_signal_log WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<DailySocialSignalLog>

    @Query("SELECT COUNT(*) FROM daily_social_signal_log")
    suspend fun getCount(): Int



    @Query("SELECT * FROM daily_social_signal_log")
    suspend fun getAllLogs(): List<DailySocialSignalLog>






    @Query("""
        DELETE FROM daily_social_signal_log 
        WHERE id IN (
            SELECT id FROM daily_social_signal_log 
            ORDER BY date ASC 
            LIMIT :halfCount
        )
    """)
    suspend fun deleteOldestHalf(halfCount: Int)
}
