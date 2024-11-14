package com.example.activityapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.activityapp.bluetooth.BluetoothSpeckService
import com.example.activityapp.bluetooth.ConnectingActivity
import com.example.activityapp.live.LiveDataActivity
import com.example.activityapp.onboarding.OnBoardingActivity
import com.example.activityapp.utils.Constants
import com.example.activityapp.utils.Utils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    // Buttons for navigating to various activities
    lateinit var liveProcessingButton: Button
    lateinit var pairingButton: Button

    // Permissions
    lateinit var permissionAlertDialog: AlertDialog.Builder
    val permissionsForRequest = arrayListOf<String>()

    var blePermissionGranted = false
    var locationPermissionGranted = false
    var cameraPermissionGranted = false
    var readStoragePermissionGranted = false
    var writeStoragePermissionGranted = false

    // Broadcast receiver
    val filter = IntentFilter()
    var isUserFirstTime = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check whether the onboarding screen should be shown
        val sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        isUserFirstTime = !sharedPreferences.contains(Constants.PREF_USER_FIRST_TIME)
        if (isUserFirstTime) {
            sharedPreferences.edit().putBoolean(Constants.PREF_USER_FIRST_TIME, false).apply()
            startActivity(Intent(this, OnBoardingActivity::class.java))
        }

        liveProcessingButton = findViewById(R.id.activity_button)
        pairingButton = findViewById(R.id.ble_button)
        permissionAlertDialog = AlertDialog.Builder(this)

        setupClickListeners()
        setupPermissions()
        setupBluetoothService()

        // Register a broadcast receiver for respeck status
        filter.addAction(Constants.ACTION_RESPECK_CONNECTED)
        filter.addAction(Constants.ACTION_RESPECK_DISCONNECTED)
    }

    private fun setupClickListeners() {
        liveProcessingButton.setOnClickListener {
            val intent = Intent(this, LiveDataActivity::class.java)
            startActivity(intent)
        }

        pairingButton.setOnClickListener {
            val intent = Intent(this, ConnectingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupPermissions() {
        // Request necessary permissions
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionsForRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsForRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            blePermissionGranted = true
        }

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsForRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsForRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            locationPermissionGranted = true
        }

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsForRequest.add(Manifest.permission.CAMERA)
        } else {
            cameraPermissionGranted = true
        }

        if (permissionsForRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsForRequest.toTypedArray(), Constants.REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun setupBluetoothService() {
        val isServiceRunning = Utils.isServiceRunning(BluetoothSpeckService::class.java, applicationContext)
        val sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)

        if (sharedPreferences.contains(Constants.RESPECK_MAC_ADDRESS_PREF) && !isServiceRunning) {
            val simpleIntent = Intent(this, BluetoothSpeckService::class.java)
            this.startService(simpleIntent)
            Log.i("MainActivity", "Starting BluetoothSpeckService for reconnection.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val simpleIntent = Intent(this, BluetoothSpeckService::class.java)
        this.stopService(simpleIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.show_tutorial) {
            startActivity(Intent(this, OnBoardingActivity::class.java))
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
