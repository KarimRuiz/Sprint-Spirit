package com.example.sprintspirit.features.run.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.sprintspirit.databinding.ActivityRunBinding
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.features.run.location.LocationService
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.LocationPermissionHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.IntervalSettings
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.Anchor
import com.mapbox.maps.extension.style.light.generated.flatLight
import com.mapbox.maps.extension.style.light.setLight
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.ScrollMode
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.DefaultLocationProvider
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport
import java.lang.ref.WeakReference
import java.util.Date


class RunActivity : BaseActivity(), PermissionsListener {

    private lateinit var viewModel: RunViewModel
    private lateinit var mapView: MapView
    private lateinit var binding: ActivityRunBinding
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private val locatonService = LocationServiceFactory.getOrCreate()
    private var locationProvider: DeviceLocationProvider? = null

    val request = LocationProviderRequest.Builder()
        .interval(IntervalSettings.Builder().interval(0L).minimumInterval(0L).maximumInterval(0L).build())
        .displacement(0F)
        .accuracy(AccuracyLevel.HIGH)
        .build()

    val locationObserver = LocationObserver { locations ->
        logd("GOT LOCATION")
        for(location in locations){
            val point = Point.fromLngLat(location.longitude, location.latitude)
            val cameraOptions = CameraOptions.Builder()
                .center(point)
                .build()

            binding.mapView.mapboxMap.setCamera(cameraOptions)
            binding.mapView.gestures.focalPoint = binding.mapView.mapboxMap.pixelForCoordinate(point)
        }
    }

    val locationReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let{
                if(viewModel.isRunning()){
                    val location =
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(LocationService.LOCATION_INTENT, android.location.Location::class.java)
                    }else{
                            intent.extras?.getParcelable(LocationService.LOCATION_INTENT) as? android.location.Location
                    }
                    val timeStamp = System.currentTimeMillis()
                    val runPoint = GeoPoint(location!!.latitude, location.longitude)
                    viewModel.addCoord(runPoint, timeStamp)
                    logd("Location added to run: " + runPoint)
                }
            }
        }
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        // Jump to the current indicator position
        val cameraOptions = CameraOptions.Builder()
            .center(it)
            .build()
        binding.mapView.mapboxMap.setCamera(cameraOptions)
        // Set the gestures plugin's focal point to the current indicator location.
        binding.mapView.gestures.focalPoint = binding.mapView.mapboxMap.pixelForCoordinate(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = RunViewModel(prefs = sharedPreferences)
        binding = ActivityRunBinding.inflate(layoutInflater)

        permissionsManager = PermissionsManager(this)
        permissionsManager.requestLocationPermissions(this)
        logd("isBackgroundLocationPermissionGranted: ${PermissionsManager.isBackgroundLocationPermissionGranted(this)}")
        val result = locatonService.getDeviceLocationProvider(request)

        if(result.isValue){
            setUpMap()
            locationProvider = result.value!!
        }else{
            loge("Failed to get devices location provider")
        }
        locationProvider?.addLocationObserver(locationObserver)

        subscribeUi(binding)

        setContentView(binding.root)
    }

    private fun subscribeUi(binding: ActivityRunBinding) {
        logd("Requesting permissions...")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            0
        )
        binding.recordRunButton.setOnClickListener {
            if(viewModel.isRunning()){
                Intent(applicationContext, LocationService::class.java).apply{
                    action = LocationService.ACTION_STOP
                    startService(this)
                    unregisterReceiver(locationReceiver)
                }

                binding.recordRunStatus.text = "Record run"
                if(viewModel.runCanBeUploaded()) postRun()
            }else{
                Intent(applicationContext, LocationService::class.java).apply{
                    action = LocationService.ACTION_START
                    startService(this)
                    val filter = IntentFilter(LocationService.LOCATION_INTENT)
                    registerReceiver(locationReceiver, filter)
                }

                binding.recordRunStatus.text = "Recording..."
                viewModel.startRun()
            }
        }
    }

    private fun postRun() {
        viewModel.saveRun()
    }

    private fun setUpMap(){
        mapView = MapView(this)
        with(mapView) {
            location.locationPuck = createDefault2DPuck(withBearing = false) //bearing wont appear until the direction is solved
            location.enabled = true
            location.puckBearingEnabled = true
            location.puckBearing = PuckBearing.HEADING //this should be PuckBearing, but doesnt seem to be working
            gestures.scrollEnabled = true
            gestures.rotateEnabled = true
            viewport.transitionTo(
                targetState = viewport.makeFollowPuckViewportState(),
                transition = viewport.makeImmediateViewportTransition()
            )
            mapboxMap.loadStyle(Style.OUTDOORS)
        }
        binding.mapView.addView(mapView)
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.location
            .addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    override fun onStop() {
        super.onStop()
        locationProvider?.removeLocationObserver(locationObserver);
        binding.mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        logd("Explanation: just fucking accept")
    }

    override fun onPermissionResult(granted: Boolean) {
        logd("Permission granted: ${granted}")
    }

}