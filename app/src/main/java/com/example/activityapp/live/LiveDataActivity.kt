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
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.content.BroadcastReceiver
import android.os.Looper
import android.content.IntentFilter
import com.example.activityapp.utils.Constants
import android.content.Context
import com.example.activityapp.utils.RESpeckLiveData


class LiveDataActivity : AppCompatActivity() {

    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet

    var time = 0f
    lateinit var allRespeckData: LineData

    lateinit var respeckChart: LineChart

    // global broadcast receiver so we can unregister it
    lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperRespeck: Looper

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)


    private lateinit var startClassificationButton: Button
    private lateinit var stopClassificationButton: Button
    private lateinit var activityTextView: TextView
    private lateinit var socialSignalTextView: TextView

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
        // set up the broadcast receiver
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {
                    val liveData =
                        intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as RESpeckLiveData
                    Log.d("Live", "onReceive: liveData = $liveData")

                    // get all relevant intent contents
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ

                    time += 1
                    updateGraph("respeck", x, y, z)
                }
            }
        }
        // register receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        this.registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)
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
