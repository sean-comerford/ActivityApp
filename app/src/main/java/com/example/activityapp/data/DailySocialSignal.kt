// File: DailySocialSignalLog.kt
package com.example.activityapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_social_signal_log")
data class DailySocialSignalLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // Format: YYYY-MM-DD
    val socialSignalType: String, // e.g., "Coughing", "Breathing Normal"
    val durationInSeconds: Int = 0 // Cumulative time spent on the social signal for the day
)
