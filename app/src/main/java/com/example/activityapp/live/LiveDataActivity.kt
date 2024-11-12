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


//ADDED FOR ML
import com.example.activityapp.MLclassification.ActivityClassifier
import com.example.activityapp.MLclassification.SocialSignalClassifier





class LiveDataActivity : AppCompatActivity() {

    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet

    //lateinit var dataSet_thingy_accel_x: LineDataSet
    //lateinit var dataSet_thingy_accel_y: LineDataSet
    //lateinit var dataSet_thingy_accel_z: LineDataSet

    var time = 0f
    lateinit var allRespeckData: LineData

    //lateinit var allThingyData: LineData


    lateinit var respeckChart: LineChart
    //lateinit var thingyChart: LineChart

    // global broadcast receiver so we can unregister it
    // Listens for live data broadcasts from Respeck
    lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    //lateinit var thingyLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperRespeck: Looper
    //lateinit var looperThingy: Looper

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
    //val filterTestThingy = IntentFilter(Constants.ACTION_THINGY_BROADCAST)

    //ADDED FOR ML
    private lateinit var activityClassifier: ActivityClassifier
    private lateinit var socialSignalClassifier: SocialSignalClassifier

    // Initialises UI elements, chart of live data and classifiers
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use activity_live_data.xml in the layout directory as its layout
        setContentView(R.layout.activity_live_data)

        // Initialize classifiers //ADDED FOR ML
        activityClassifier = ActivityClassifier(this)
        socialSignalClassifier = SocialSignalClassifier(this)


        setupCharts()

        // set up the broadcast receiver
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {

                    val liveData =
                        intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as RESpeckLiveData
                    Log.d("Live", "onReceive: liveData = " + liveData)

                    // get all relevant intent contents
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ

                    time += 1
                    updateGraph("respeck", x, y, z)

                    // Classify activity based on the accelerometer data
                    //val activity = classifyActivity(x, y, z)


                    //ADDED FOR ML
                    // Classify activity. Will be null until the buffer fills up
                    val activity = activityClassifier.addSensorData(x, y, z)

                    // Classify social signal. Will be null until buffer fills up
                    val socialSignal = socialSignalClassifier.addSensorData(x, y, z)

                    runOnUiThread {
                        // Update Activity Classification TextView in layout.xml
                        val activityTextView: TextView = findViewById(R.id.activity_classification)
                        // If activity is not full, activityTextView.text will show the classification label
                        activityTextView.text = activity ?: "Processing..."

                        // Update Social Signal Classification TextView
                        val socialSignalTextView: TextView = findViewById(R.id.social_signal_classification)
                        socialSignalTextView.text = socialSignal ?: "Processing..."
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
    fun setupCharts() {
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

    fun updateGraph(graph: String, x: Float, y: Float, z: Float) {
        // take the first element from the queue
        // and update the graph with it
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


    //ADDED FOR ML
    private fun handleSensorData(x: Float, y: Float, z: Float) {
        // Classify activity
        val activity = activityClassifier.addSensorData(x, y, z)
        if (activity != null) {
            runOnUiThread {
                findViewById<TextView>(R.id.activity_classification).text = activity
            }
        }

        // Classify social signal
        val socialSignal = socialSignalClassifier.addSensorData(x, y, z)
        if (socialSignal != null) {
            runOnUiThread {
                findViewById<TextView>(R.id.social_signal_classification).text = socialSignal
            }
        }
    }



}
