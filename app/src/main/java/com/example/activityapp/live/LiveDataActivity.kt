package com.example.activityapp.live

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.activityapp.R
import com.example.activityapp.services.ClassificationService
import com.github.mikephil.charting.charts.LineChart

class LiveDataActivity : AppCompatActivity() {

    private lateinit var startClassificationButton: Button
    private lateinit var stopClassificationButton: Button
    private lateinit var activityTextView: TextView
    private lateinit var socialSignalTextView: TextView
    private lateinit var respeckChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_data)

        // Initialize UI components
        startClassificationButton = findViewById(R.id.start_classification_button)
        stopClassificationButton = findViewById(R.id.stop_classification_button)
        activityTextView = findViewById(R.id.activity_classification)
        socialSignalTextView = findViewById(R.id.social_signal_classification)
        respeckChart = findViewById(R.id.respeck_chart)

        // Set up the initial chart setup
        setupCharts()

        // Set click listeners for start and stop classification buttons
        startClassificationButton.setOnClickListener {
            startClassification()
        }

        stopClassificationButton.setOnClickListener {
            stopClassification()
        }
    }

    private fun startClassification() {
        // Start the ClassificationService
        val intent = Intent(this, ClassificationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Log.d("LiveDataActivity", "Started ClassificationService")

        activityTextView.text = "Classification Started"
        socialSignalTextView.text = "Classification Started"
    }

    private fun stopClassification() {
        // Stop the ClassificationService
        val intent = Intent(this, ClassificationService::class.java)
        stopService(intent)
        Log.d("LiveDataActivity", "Stopped ClassificationService")

        activityTextView.text = "Classification Stopped"
        socialSignalTextView.text = "Classification Stopped"
    }

    private fun setupCharts() {
        // Initialize the chart components as needed
    }
}
