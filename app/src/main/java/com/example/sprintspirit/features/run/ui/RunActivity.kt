package com.example.sprintspirit.features.run.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import com.example.sprintspirit.databinding.ActivityRunBinding
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
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


class RunActivity : BaseActivity() {

    private lateinit var viewModel: RunViewModel
    private lateinit var mapView: MapView
    private lateinit var binding: ActivityRunBinding
    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private var isRunning = false
    private var run: RunData? = null

    private val locatonService = LocationServiceFactory.getOrCreate()
    private var locationProvider: DeviceLocationProvider? = null

    val request = LocationProviderRequest.Builder()
        .interval(IntervalSettings.Builder().interval(0L).minimumInterval(0L).maximumInterval(0L).build())
        .displacement(0F)
        .accuracy(AccuracyLevel.HIGH)
        .build()

    val locationObserver = LocationObserver { locations ->
        logd("Location update received: " + locations)
        for(location in locations){
            val point = Point.fromLngLat(location.longitude, location.latitude)
            val cameraOptions = CameraOptions.Builder()
                .center(point)
                .build()

            val timeStamp = System.currentTimeMillis()
            val runPoint: Map<String, GeoPoint> = mapOf(timeStamp.toString() to GeoPoint(location.latitude, location.longitude))

            val mutablePoints = run?.points?.toMutableList() ?: mutableListOf()
            mutablePoints.add(runPoint)
            run?.points = mutablePoints

            binding.mapView.mapboxMap.setCamera(cameraOptions)
            binding.mapView.gestures.focalPoint = binding.mapView.mapboxMap.pixelForCoordinate(point)
        }
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        // Jump to the current indicator position
        val cameraOptions = CameraOptions.Builder()
            .center(it)
            .build()
        binding.mapView.mapboxMap.setCamera(cameraOptions)
        //binding.mapView.mapboxMap.loadStyle(Style.OUTDOORS) //TODO: if the style is correct, just delete it
        // Set the gestures plugin's focal point to the current indicator location.
        binding.mapView.gestures.focalPoint = binding.mapView.mapboxMap.pixelForCoordinate(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = RunViewModel()
        binding = ActivityRunBinding.inflate(layoutInflater)

        /*locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            setUpMap()
        }*/
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
        binding.recordRunButton.setOnClickListener {
            if(isRunning){
                binding.recordRunStatus.text = "Record run"
                if((run?.points?.size ?: 0) > 2) postRun()
                run = null
            }else{
                binding.recordRunStatus.text = "Recording..."
                run = RunData(
                    user = "/users/" + sharedPreferences.email?: "",
                    startTime = Date()
                )
            }
            isRunning = !isRunning
        }
    }

    private fun postRun() {
        viewModel.saveRun(RunResponse(run))
    }

    private fun setUpMap(){
        mapView = MapView(this)
        /*mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(37.1, 3.6))
                .pitch(0.0)
                .zoom(10.0)
                .bearing(0.0)
                .build()
        )*/
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
        /*binding.mapView.apply {
            location.enabled = true
            mapboxMap.loadStyle(Style.OUTDOORS) {
                // Disable scroll gesture, since we are updating the camera position based on the indicator location.
                gestures.scrollEnabled = true
                gestures.addOnMapClickListener { point ->
                    location
                        .isLocatedAt(point) { isPuckLocatedAtPoint ->
                            if (isPuckLocatedAtPoint) {
                                Toast.makeText(context, "Clicked on location puck", Toast.LENGTH_SHORT).show()
                            }
                        }
                    true
                }
                gestures.addOnMapLongClickListener { point ->
                    location.isLocatedAt(point) { isPuckLocatedAtPoint ->
                        if (isPuckLocatedAtPoint) {
                            Toast.makeText(context, "Long-clicked on location puck", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    true
                }
                val locationProvider = location.getLocationProvider() as DefaultLocationProvider
                locationProvider.addOnCompassCalibrationListener {
                    Toast.makeText(context, "Compass needs to be calibrated", Toast.LENGTH_LONG).show()
                }
            }
        }*/
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

}