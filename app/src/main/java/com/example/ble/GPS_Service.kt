package com.example.ble

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

class GPS_Service : Service() {


    private val gpsBinder = GpsBinder()
    private var listener: LocationListener? = null
    private lateinit var locationManager: LocationManager

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotification(): Notification? {
        val channel =
            NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
        val builder =
            Notification.Builder(applicationContext, "channel_01")
                .setAutoCancel(true)
        //builder.notification.flags = Notification.FLAG_ONGOING_EVENT
        return builder.build()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("message","Gps service running")
        startForeground(12345678, getNotification())
        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                val i = Intent("location_update")
                i.putExtra("latitude",location.latitude)
                i.putExtra("longitude",location.longitude)
                Log.d("coordinates",location.longitude.toString()+ location.latitude.toString())
                Toast.makeText(
                    this@GPS_Service, " ${location.latitude}, ${location.longitude}",
                    Toast.LENGTH_SHORT ).show()
                sendBroadcast(i)


            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?){}
            override fun onProviderEnabled(s: String) {  }
            override fun onProviderDisabled(s: String) {}
        }
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val extras = intent!!.extras
        if (extras == null) {
            Log.d("parameter", "null")
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0f, listener)
        }else {
            Log.d("parameter", "not null")
            val seconds = (extras.getInt("parameter")*1000).toLong()
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, seconds, 0f, listener)
        }

        return START_NOT_STICKY
    }
    override fun onDestroy() {
        Toast.makeText(baseContext, "Gps service not running", Toast.LENGTH_SHORT ).show()
        stopForeground(true)
        stopSelf()
        locationManager!!.removeUpdates(listener)
        super.onDestroy()

    }

    override fun onBind(intent: Intent?): IBinder {
        return gpsBinder
    }

    override fun onRebind(intent: Intent?) {
        stopSelf()
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    inner class GpsBinder: Binder(){
        fun getGpsService(): GPS_Service = this@GPS_Service
    }
}
