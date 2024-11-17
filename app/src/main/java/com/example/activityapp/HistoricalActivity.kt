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

import android.util.TypedValue
import android.widget.LinearLayout
import android.view.ViewGroup


import android.widget.ImageButton





class HistoricalActivity : AppCompatActivity() {

    private lateinit var datePicker: DatePicker
    private lateinit var btnViewActivity: Button
    private lateinit var activityChart: HorizontalBarChart
    private lateinit var socialSignalChart: HorizontalBarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historical_activity)

        // Set up the back button
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // Finish this activity and go back to the previous one
        }




        datePicker = findViewById(R.id.date_picker)
        btnViewActivity = findViewById(R.id.btn_view_activity)
        activityChart = findViewById(R.id.chart_activity)
        socialSignalChart = findViewById(R.id.chart_social_signal)

        // Restrict the DatePicker to disable future dates
        datePicker.maxDate = System.currentTimeMillis()


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

        if (data.isEmpty()) {
            // Show a message when there is no data
            chart.clear() // Clear existing data
            chart.setNoDataText("No activity data available for this day")
            chart.setNoDataTextColor(android.graphics.Color.GRAY) // Optional: Customize text color
            chart.invalidate() // Refresh the chart
            return
        }

        val entries = data.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.second)
        }

        val barDataSet = BarDataSet(entries, label).apply {
            setDrawValues(false) // Disable value display on bars
            valueTextSize = 12f
        }

        val barData = BarData(barDataSet).apply {
            barWidth = 0.5f // Set a consistent bar thickness
        }

        val labels = data.map { it.first }

        // Determine the maximum value dynamically for the time axis
        val maxValue = (data.maxOfOrNull { it.second } ?: 0f).coerceAtMost(24f) + 0.5f

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.TOP // Position labels at the top
            granularity = 1f
            textSize = 12f
            setDrawGridLines(false) // Disable grid lines along the X-axis (horizontal visually)
            setAvoidFirstLastClipping(true) // Prevent category labels from clipping
        }

        chart.axisLeft.apply {
            axisMinimum = 0f // Start at 0
            axisMaximum = maxValue // Dynamically set based on data, capped at 24
            granularity = 0.5f // Steps of 0.5 hours
            setDrawGridLines(true) // Enable Y-axis grid lines (vertical visually)
        }

        chart.axisRight.isEnabled = false // Disable right axis for clarity
        chart.setPinchZoom(false) // Disable pinch zooming
        chart.setDoubleTapToZoomEnabled(false) // Disable zooming via double-tap

        chart.description.isEnabled = false // Disable description text
        chart.legend.isEnabled = false // Disable legend for a cleaner look

        // Enable highlighting and interaction
        chart.setHighlightPerTapEnabled(true)
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    // Get the time value of the selected bar
                    val timeValueInHours = it.y
                    val hours = timeValueInHours.toInt()
                    val minutes = ((timeValueInHours - hours) * 60).toInt()
                    val seconds = (((timeValueInHours - hours) * 60 - minutes) * 60).toInt()

                    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                    // Show precise time value as a toast
                    Toast.makeText(
                        chart.context,
                        "Activity Time: $formattedTime (hh:mm:ss)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNothingSelected() {
                // No action needed when no bar is selected
            }
        })

        // Dynamically adjust the chart height based on the number of categories
        val baseHeight = 60 // Height per category in dp
        val totalHeight = (data.size * baseHeight).coerceAtLeast(200) // Minimum height 200dp
        val heightInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            totalHeight.toFloat(),
            chart.context.resources.displayMetrics
        ).toInt()

        chart.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            heightInPx
        )

        chart.data = barData
        chart.notifyDataSetChanged() // Notify the chart about the data change
        chart.invalidate() // Refresh the chart
    }









}

