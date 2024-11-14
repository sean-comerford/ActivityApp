package com.example.activityapp.logging

import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.activityapp.R
import com.example.activityapp.data.AppDatabase
import com.example.activityapp.data.DailyActivityLog
import com.example.activityapp.data.DailySocialSignalLog
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import com.example.activityapp.data.*


class LogViewActivity : AppCompatActivity() {

    private lateinit var datePicker: DatePicker
    private lateinit var showLogsButton: Button
    private lateinit var activityBarChart: BarChart
    private lateinit var socialSignalBarChart: BarChart
    private lateinit var db: AppDatabase
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_view)

        datePicker = findViewById(R.id.date_picker)
        showLogsButton = findViewById(R.id.show_logs_button)
        activityBarChart = findViewById(R.id.activity_bar_chart)
        socialSignalBarChart = findViewById(R.id.social_signal_bar_chart)

        db = AppDatabase.getInstance(applicationContext)

        showLogsButton.setOnClickListener {
            val selectedDate = getSelectedDate()
            displayLogsForDate(selectedDate)
        }
    }

    private fun getSelectedDate(): String {
        val day = datePicker.dayOfMonth
        val month = datePicker.month + 1 // DatePicker returns month zero-based
        val year = datePicker.year
        return "$year-${String.format("%02d", month)}-${String.format("%02d", day)}"
    }

    private fun displayLogsForDate(date: String = "2024-11-14") {
        lifecycleScope.launch {
            val activityLogDao = db.activityLogDao()
            val activityLogs = activityLogDao.getLogsForDate(date)

            val socialSignalLogDao = db.socialSignalLogDao()
            val socialSignalLogs = socialSignalLogDao.getLogsForDate(date)

            Log.d("LogViewActivity", "Retrieved activity logs: $activityLogs")
            Log.d("LogViewActivity", "Retrieved social signal logs: $socialSignalLogs")

            showActivityDataInChart(activityLogs)
            showSocialSignalDataInChart(socialSignalLogs)

            val allLogs = activityLogDao.getLogsForDate(date)
            Log.d("ActivityLogger", "All activity logs for today: $allLogs")
        }
    }

    private fun showActivityDataInChart(activityLogs: List<DailyActivityLog>) {
        val activityEntries = mutableListOf<BarEntry>()
        val activityLabels = mutableListOf<String>()

        for ((index, log) in activityLogs.withIndex()) {
            val hours = log.durationInSeconds / 3600f // Convert seconds to hours
            activityEntries.add(BarEntry(index.toFloat(), hours))
            activityLabels.add(log.activityType)
        }

        val activityDataSet = BarDataSet(activityEntries, "Activity Logs")
        val activityData = BarData(activityDataSet)
        activityBarChart.data = activityData
        activityBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(activityLabels)
        activityBarChart.invalidate() // Refresh the chart
    }

    private fun showSocialSignalDataInChart(socialSignalLogs: List<DailySocialSignalLog>) {
        val socialSignalEntries = mutableListOf<BarEntry>()
        val socialSignalLabels = mutableListOf<String>()

        for ((index, log) in socialSignalLogs.withIndex()) {
            val hours = log.durationInSeconds / 3600f // Convert seconds to hours
            socialSignalEntries.add(BarEntry(index.toFloat(), hours))
            socialSignalLabels.add(log.socialSignalType)
        }

        val socialSignalDataSet = BarDataSet(socialSignalEntries, "Social Signal Logs")
        val socialSignalData = BarData(socialSignalDataSet)
        socialSignalBarChart.data = socialSignalData
        socialSignalBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(socialSignalLabels)
        socialSignalBarChart.invalidate() // Refresh the chart
    }



    private fun logAllDatabaseEntries() {
        lifecycleScope.launch {
            val allActivityLogs = withContext(Dispatchers.IO) { db.activityLogDao().getAllLogs() }
            val allSocialSignalLogs = withContext(Dispatchers.IO) { db.socialSignalLogDao().getAllLogs() }

            Log.d("LogViewActivity", "All activity logs: $allActivityLogs")
            Log.d("LogViewActivity", "All social signal logs: $allSocialSignalLogs")
        }
    }


}
