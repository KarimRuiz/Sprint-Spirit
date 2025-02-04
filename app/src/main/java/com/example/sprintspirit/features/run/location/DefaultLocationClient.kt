package com.example.sprintspirit.features.run.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.example.sprintspirit.data.Preferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {
    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if(!context.hasLocationPermission()){
                throw LocationClient.LocationException("Missing location permission")
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            //val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) //DONT KNOW IF WE REALLY NEED THIS
            if(!isGpsEnabled){// && !isNetworkEnabled){
                throw LocationClient.LocationException("GPS is disabled!")
            }

            val request = if(interval >= LocationRefreshRate.HIGH.getMilli()) {
                                com.google.android.gms.location.LocationRequest
                                    .Builder(Priority.PRIORITY_HIGH_ACCURACY, interval).build()
                            }else if(interval >= LocationRefreshRate.NORMAL.getMilli()){
                                com.google.android.gms.location.LocationRequest
                                    .Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, interval).build()
                            }else{
                                com.google.android.gms.location.LocationRequest
                                    .Builder(Priority.PRIORITY_LOW_POWER, interval).build()
                            }

            val locationCallback = object : LocationCallback(){
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let{ location ->
                        launch { send(location) }
                    }
                }
            }

            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}