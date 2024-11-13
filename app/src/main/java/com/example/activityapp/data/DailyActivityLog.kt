// File: DailyActivityLog.kt
package com.example.activityapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_activity_log")
data class DailyActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // Format: YYYY-MM-DD
    val activityType: String, // e.g., "Walking", "Sitting"
    val durationInSeconds: Int = 0 // Cumulative time spent on the activity for the day
)
