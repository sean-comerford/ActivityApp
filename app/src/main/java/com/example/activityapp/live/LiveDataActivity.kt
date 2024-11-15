package com.example.activityapp.live

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.activityapp.R
import com.example.activityapp.services.ClassificationService
import com.example.activityapp.utils.Constants
import com.example.activityapp.utils.RESpeckLiveData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class LiveDataActivity : AppCompatActivity() {

    // Graph variables
    private lateinit var dataSet_res_accel_x: LineDataSet
    private lateinit var dataSet_res_accel_y: LineDataSet
    private lateinit var dataSet_res_accel_z: LineDataSet

    private var time = 0f
    private lateinit var allRespeckData: LineData
    private lateinit var respeckChart: LineChart

    // BroadcastReceiver for live data and classification results
    private lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    private lateinit var classificationUpdateReceiver: BroadcastReceiver
    private lateinit var looperRespeck: Looper

    // Intent filters
    private val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
    private val classificationFilter = IntentFilter(Constants.ACTION_CLASSIFICATION_UPDATE)

    // UI Components
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

        // Set up the chart
        setupCharts()

        // Set click listeners for classification control buttons
        startClassificationButton.setOnClickListener {
            startClassification()
        }

        stopClassificationButton.setOnClickListener {
            stopClassification()
        }

        // Set up the BroadcastReceiver for live data
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {
                    val liveData =
                        intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as RESpeckLiveData

                    // Update graph with new sensor data
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ

                    time += 1
                    updateGraph("respeck", x, y, z)
                }
            }
        }

        // Register the live data receiver on a background thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)

        // Set up the BroadcastReceiver for classification updates
        classificationUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Constants.ACTION_CLASSIFICATION_UPDATE) {
                    val activity = intent.getStringExtra("activity")
                    val socialSignal = intent.getStringExtra("socialSignal")

                    // Update UI elements with classification results
                    activityTextView.text = activity ?: "Processing..."
                    socialSignalTextView.text = socialSignal ?: "Processing..."
                }
            }
        }

        // Register the classification results receiver
        registerReceiver(classificationUpdateReceiver, classificationFilter)
    }

    private fun setupCharts() {
        time = 0f

        // Initialize datasets for Respeck chart
        val entries_res_accel_x = ArrayList<Entry>()
        val entries_res_accel_y = ArrayList<Entry>()
        val entries_res_accel_z = ArrayList<Entry>()

        dataSet_res_accel_x = LineDataSet(entries_res_accel_x, "Accel X")
        dataSet_res_accel_y = LineDataSet(entries_res_accel_y, "Accel Y")
        dataSet_res_accel_z = LineDataSet(entries_res_accel_z, "Accel Z")

        dataSet_res_accel_x.setDrawCircles(false)
        dataSet_res_accel_y.setDrawCircles(false)
        dataSet_res_accel_z.setDrawCircles(false)

        dataSet_res_accel_x.color = ContextCompat.getColor(this, R.color.red)
        dataSet_res_accel_y.color = ContextCompat.getColor(this, R.color.green)
        dataSet_res_accel_z.color = ContextCompat.getColor(this, R.color.blue)

        val dataSetsRes = ArrayList<ILineDataSet>()
        dataSetsRes.add(dataSet_res_accel_x)
        dataSetsRes.add(dataSet_res_accel_y)
        dataSetsRes.add(dataSet_res_accel_z)

        allRespeckData = LineData(dataSetsRes)
        respeckChart.data = allRespeckData
        respeckChart.invalidate()
    }

    private fun startClassification() {
        val intent = Intent(this, ClassificationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Log.d("LiveDataActivity", "Started ClassificationService")

        // Update TextViews to indicate processing
        activityTextView.text = "Processing..."
        socialSignalTextView.text = "Processing..."
    }

    private fun stopClassification() {
        val intent = Intent(this, ClassificationService::class.java)
        stopService(intent)
        Log.d("LiveDataActivity", "Stopped ClassificationService")

        // Update the text views after classification has stopped
        activityTextView.text = "Please start classification"
        socialSignalTextView.text = "Please start classification"
    }

    private fun updateGraph(graph: String, x: Float, y: Float, z: Float) {
        if (graph == "respeck") {
            dataSet_res_accel_x.addEntry(Entry(time, x))
            dataSet_res_accel_y.addEntry(Entry(time, y))
            dataSet_res_accel_z.addEntry(Entry(time, z))

            runOnUiThread {
                allRespeckData.notifyDataChanged()
                respeckChart.notifyDataSetChanged()
                respeckChart.invalidate()
                respeckChart.setVisibleXRangeMaximum(150f)
                respeckChart.moveViewToX(respeckChart.lowestVisibleX + 40)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(respeckLiveUpdateReceiver)
        unregisterReceiver(classificationUpdateReceiver)
        looperRespeck.quit()
    }
}
