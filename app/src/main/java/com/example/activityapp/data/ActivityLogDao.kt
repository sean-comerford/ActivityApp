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
    fun getActivitiesByDate(date: String): List<DailyActivityLog>

    // Method to get the total count of entries
    @Query("SELECT COUNT(*) FROM daily_activity_log")
    suspend fun getCount(): Int

    // Method to delete the oldest half of the entries
    @Query("""
        DELETE FROM daily_activity_log 
        WHERE id IN (
            SELECT id FROM daily_activity_log 
            ORDER BY date ASC 
            LIMIT :halfCount
        )
    """)
    suspend fun deleteOldestHalf(halfCount: Int)
}
