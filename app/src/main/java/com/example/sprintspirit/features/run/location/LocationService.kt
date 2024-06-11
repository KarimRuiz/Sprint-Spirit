package com.example.sprintspirit.features.run.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.features.run.ui.RunActivity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        /* THIS IS THE OLD LOCATION HANDLER. IT WAS REMOVED AS IT PRODUCES LOTS OF NOISE */
        /*locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )*/

        locationClient = LocalLocationClient(
            applicationContext,
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        )
    }

    private fun createNotificationChannel() {
        val channelName = "Running Channel"
        val channelDescription = "Channel for running notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
            description = channelDescription
            vibrationPattern = longArrayOf(0L)
            enableVibration(false)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val runActivityIntent = Intent(this, RunActivity::class.java).apply {
            putExtra(RunActivity.CAME_FROM_NOTIFICATION, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            runActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.Tracking_route))
            .setContentText(applicationContext.getString(R.string.Tracking_location))
            .setSmallIcon(R.drawable.ic_logo_no_text)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val refreshRate = Preferences(applicationContext).locationRefreshRate
        locationClient.getLocationUpdates(refreshRate)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                val updatedNotification = notification.setContentText(
                    "${applicationContext.getString(R.string.Tracking_location)}"
                )
                notificationManager.notify(1, updatedNotification.build())

                val intent = Intent(LOCATION_INTENT)
                intent.putExtra(LOCATION_INTENT, location)
                sendBroadcast(intent)
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun stop() {
        //stopForeground(STOP_FOREGROUND_REMOVE)
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object{
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        const val LOCATION_INTENT = "LocationService.Location"

        const val CHANNEL_ID = "RUNNING_CHANNEL"
    }

}