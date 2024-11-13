package com.example.activityapp.live

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.example.activityapp.R
import com.example.activityapp.utils.Constants
import com.example.activityapp.utils.RESpeckLiveData
import com.example.activityapp.utils.ThingyLiveData
import kotlin.collections.ArrayList
import android.widget.TextView
import androidx.room.Room
import com.example.activityapp.data.AppDatabase
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ADDED FOR ML
import com.example.activityapp.MLclassification.ActivityClassifier
import com.example.activityapp.MLclassification.SocialSignalClassifier
import com.example.activityapp.logging.ActivityLogger

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

    // ADDED FOR ML
    private lateinit var activityClassifier: ActivityClassifier
    private lateinit var socialSignalClassifier: SocialSignalClassifier

    // Define the Database and the Logger
    private lateinit var appDatabase: AppDatabase
    private lateinit var activityLogger: ActivityLogger

    // For storing latest classification results
    private var lastActivity: String? = null
    private var lastSocialSignal: String? = null

    // Initialises UI elements, chart of live data and classifiers
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_data)

        // Initialize the database
        appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "activity_app_db"
        ).build()

        // Initialize the logger with the database
        activityLogger = ActivityLogger(appDatabase)

        // Initialize classifiers
        activityClassifier = ActivityClassifier(this, activityLogger)
        socialSignalClassifier = SocialSignalClassifier(this, activityLogger)

        setupCharts()

        // Set initial classification results to "Processing"
        val activityTextView: TextView = findViewById(R.id.activity_classification)
        activityTextView.text = "Processing..."

        val socialSignalTextView: TextView = findViewById(R.id.social_signal_classification)
        socialSignalTextView.text = "Processing..."

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

                    // Classify activity based on the accelerometer data
                    val activity = activityClassifier.addSensorData(x, y, z)
                    // Update the latest classification result if it is different from the last one
                    if (activity != null && activity != lastActivity) {
                        lastActivity = activity
                        runOnUiThread {
                            activityTextView.text = activity
                        }
                    }

                    // Classify social signal. Will be null until buffer fills up
                    val socialSignal = socialSignalClassifier.addSensorData(x, y, z)
                    // Update the latest classification result if it is different from the last one
                    if (socialSignal != null && socialSignal != lastSocialSignal) {
                        lastSocialSignal = socialSignal
                        runOnUiThread {
                            socialSignalTextView.text = socialSignal
                        }
                    }
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

    // Initialises the chart for the Respeck
    private fun setupCharts() {
        respeckChart = findViewById(R.id.respeck_chart)

        // Respeck
        time = 0f
        val entries_res_accel_x = ArrayList<Entry>()
        val entries_res_accel_y = ArrayList<Entry>()
        val entries_res_accel_z = ArrayList<Entry>()

        dataSet_res_accel_x = LineDataSet(entries_res_accel_x, "Accel X")
        dataSet_res_accel_y = LineDataSet(entries_res_accel_y, "Accel Y")
        dataSet_res_accel_z = LineDataSet(entries_res_accel_z, "Accel Z")

        dataSet_res_accel_x.setDrawCircles(false)
        dataSet_res_accel_y.setDrawCircles(false)
        dataSet_res_accel_z.setDrawCircles(false)

        dataSet_res_accel_x.setColor(
            ContextCompat.getColor(
                this,
                R.color.red
            )
        )
        dataSet_res_accel_y.setColor(
            ContextCompat.getColor(
                this,
                R.color.green
            )
        )
        dataSet_res_accel_z.setColor(
            ContextCompat.getColor(
                this,
                R.color.blue
            )
        )

        val dataSetsRes = ArrayList<ILineDataSet>()
        dataSetsRes.add(dataSet_res_accel_x)
        dataSetsRes.add(dataSet_res_accel_y)
        dataSetsRes.add(dataSet_res_accel_z)

        allRespeckData = LineData(dataSetsRes)
        respeckChart.data = allRespeckData
        respeckChart.invalidate()
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
        looperRespeck.quit()
    }

    // ADDED FOR ML
    private fun handleSensorData(x: Float, y: Float, z: Float) {
        val activity = activityClassifier.addSensorData(x, y, z)
        if (activity != null) {
            runOnUiThread {
                findViewById<TextView>(R.id.activity_classification).text = activity
            }
        }

        val socialSignal = socialSignalClassifier.addSensorData(x, y, z)
        if (socialSignal != null) {
            runOnUiThread {
                findViewById<TextView>(R.id.social_signal_classification).text = socialSignal
            }
        }
    }
}
