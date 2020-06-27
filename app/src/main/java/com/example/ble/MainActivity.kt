package com.example.ble

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity() {

    private var btn_start: Button? = null
    private var btn_stop: Button? = null
    private var textView: TextView? = null
    private var broadcastReceiver: BroadcastReceiver? = null


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_start = findViewById(R.id.button) as Button
        btn_stop = findViewById(R.id.button2) as Button
        textView = findViewById(R.id.textView)

        runtime_permissions()

       btn_start!!.setOnClickListener {
           startGpsService()
       }
       btn_stop!!.setOnClickListener {
           stopGpsService()
       }

    }

    private fun runtime_permissions(): Boolean {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
            return true
        }
        return false
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            else {
                runtime_permissions()
            }
        }
    }

    fun startGpsService(seconds:Int? = null){
            val gpsIntent = Intent(this@MainActivity, GPS_Service::class.java)
        if (seconds != null) gpsIntent.putExtra("parameter",seconds)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(gpsIntent)
        }
        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    val latitude = intent.extras!!["latitude"]?.toString()?.toFloat()
                    val longitude = intent.extras!!["longitude"]?.toString()?.toFloat()

                    textView!!.text = "$longitude,$latitude"
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("location_update"))
    }
    fun stopGpsService(){
        val gpsServiceIntent = Intent(this, GPS_Service::class.java)
        stopService(gpsServiceIntent)
        if (broadcastReceiver !=null){
            unregisterReceiver(broadcastReceiver)
        }

    }
}