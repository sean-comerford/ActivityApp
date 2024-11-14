package com.example.activityapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.activityapp.MLclassification.ActivityClassifier
import com.example.activityapp.MLclassification.SocialSignalClassifier
import com.example.activityapp.data.AppDatabase
import com.example.activityapp.logging.ActivityLogger
import com.example.activityapp.utils.Constants
import com.example.activityapp.utils.RESpeckLiveData
import com.example.activityapp.R

class ClassificationService : Service() {

    private lateinit var activityClassifier: ActivityClassifier
    private lateinit var socialSignalClassifier: SocialSignalClassifier
    private lateinit var activityLogger: ActivityLogger

    // BroadcastReceiver to handle sensor data
    private val respeckReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {
                val liveData = intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as? RESpeckLiveData
                liveData?.let {
                    handleSensorData(it.accelX, it.accelY, it.accelZ)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Start the service in the foreground with a notification
        startForegroundServiceWithNotification()

        // Initialize database and loggers
        val database = AppDatabase.getInstance(this)
        activityLogger = ActivityLogger(database)
        activityClassifier = ActivityClassifier(this, activityLogger)
        socialSignalClassifier = SocialSignalClassifier(this, activityLogger)

        // Register the BroadcastReceiver to receive live data
        val filter = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
        registerReceiver(respeckReceiver, filter, null, android.os.Handler(Looper.getMainLooper()))
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "ClassificationServiceChannel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel if running on Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Classification Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Classification Running")
            .setContentText("The app is classifying activities in the background.")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        startForeground(1, notification) // Start as a foreground service with a persistent notification
    }

    private fun handleSensorData(x: Float, y: Float, z: Float) {
        // Classify activity
        val activity = activityClassifier.addSensorData(x, y, z)
        if (activity != null) {
            Log.d("ClassificationService", "Activity classified: $activity")
        }

        // Classify social signal
        val socialSignal = socialSignalClassifier.addSensorData(x, y, z)
        if (socialSignal != null) {
            Log.d("ClassificationService", "Social signal classified: $socialSignal")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(respeckReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
