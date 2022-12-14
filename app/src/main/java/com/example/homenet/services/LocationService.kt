package com.example.homenet.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.homenet.utils.Util
import com.google.android.gms.location.*
import java.io.FileNotFoundException
import java.util.*

class LocationService : Service() {

  private lateinit var fusedLocationClient: FusedLocationProviderClient
  var latitude: Double = 0.0
  var longitude: Double = 0.0

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreate() {
    requestLocationUpdates()
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
    startTimer()

    return START_STICKY
  }

  override fun onBind(intent: Intent): IBinder? {
    // We don't provide binding, so return null
    return null
  }

  private fun startTimer() {
    var timer: Timer? = Timer()
    val timerTask = object : TimerTask() {
      @RequiresApi(Build.VERSION_CODES.M)
      override fun run() {
        try {
          if (Util.arePointsNear(
              arrayOf(latitude, longitude),
              Util.getHomeLocation(this@LocationService.applicationContext).toTypedArray()
            )) { Util.sendVicinityNotification(this@LocationService.applicationContext) }
        } catch (error: FileNotFoundException) {
          ContextCompat.getMainExecutor(this@LocationService.applicationContext).execute {
            Toast.makeText(
              this@LocationService.applicationContext,
              "Please set your home location first",
              Toast.LENGTH_SHORT
            ).show()
          }
          timer?.cancel()
          timer = null
        }
      }
    }

    if (timer != null)
      timer?.schedule(
        timerTask,
        0,
        Util.LOCATION_INTERVAL
      )
  }

  private fun requestLocationUpdates() {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    val permission = ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.ACCESS_FINE_LOCATION
    )
    if (permission == PackageManager.PERMISSION_GRANTED) {
      fusedLocationClient.requestLocationUpdates(Util.locationRequest(), object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
          val location: Location? = locationResult.lastLocation
            latitude = location?.latitude!!
            longitude = location.longitude
        }
      }, null)
    }
  }

}