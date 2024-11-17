package com.example.activityapp

import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import com.example.activityapp.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import android.widget.Toast



class HistoricalActivity : AppCompatActivity() {

    private lateinit var datePicker: DatePicker
    private lateinit var btnViewActivity: Button
    private lateinit var activityChart: HorizontalBarChart
    private lateinit var socialSignalChart: HorizontalBarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historical_activity)

        datePicker = findViewById(R.id.date_picker)
        btnViewActivity = findViewById(R.id.btn_view_activity)
        activityChart = findViewById(R.id.chart_activity)
        socialSignalChart = findViewById(R.id.chart_social_signal)

        btnViewActivity.setOnClickListener {
            val selectedDate = "${datePicker.year}-${datePicker.month + 1}-${datePicker.dayOfMonth}"
            fetchAndDisplayData(selectedDate)
        }
    }

    private fun fetchAndDisplayData(date: String) {
        lifecycleScope.launch {
            // Fetch activity logs and social signal logs for the selected date
            val activityLogs = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@HistoricalActivity).activityLogDao()
                    .getActivitiesByDate(date)
            }
            val socialSignalLogs = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@HistoricalActivity).socialSignalLogDao()
                    .getActivitiesByDate(date)
            }

            // Process data for charts
            val activityData = activityLogs.map { it.activityType to it.durationInSeconds / 3600f }
            val socialSignalData =
                socialSignalLogs.map { it.socialSignalType to it.durationInSeconds / 3600f }

            // Update Activity Chart
            populateChart(activityChart, activityData, "Activity Hours")

            // Update Social Signal Chart
            populateChart(socialSignalChart, socialSignalData, "Social Signal Hours")
        }
    }

    private fun populateChart(
        chart: HorizontalBarChart,
        data: List<Pair<String, Float>>,
        label: String
    ) {
        val entries = data.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.second)
        }

        val barDataSet = BarDataSet(entries, label).apply {
            setDrawValues(true)
            valueTextSize = 12f
        }

        val labels = data.map { it.first }

        // Determine the maximum value dynamically
        val maxValue = (data.maxOfOrNull { it.second } ?: 0f).coerceAtMost(24f)

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.TOP // Position labels at the top
            granularity = 1f
            textSize = 12f
        }

        chart.axisLeft.apply {
            axisMinimum = 0f // Start at 0
            axisMaximum = maxValue + 0.5f // Add 0.5 to ensure smallest value is at the end
            granularity = 0.5f // Steps of 0.5 hours
            setDrawGridLines(true) // Enable grid lines
            labelCount = ((maxValue * 2).toInt() + 1) // Dynamically adjust label count for 0.5-hour increments
        }

        chart.axisRight.isEnabled = false // Disable right axis for clarity

        chart.data = BarData(barDataSet)
        chart.invalidate() // Refresh the chart
    }






}

