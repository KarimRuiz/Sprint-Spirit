package com.example.sprintspirit.features.run.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import com.example.sprintspirit.databinding.ActivityRunBinding
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.LocationPermissionHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
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


class RunActivity : BaseActivity() {

    private lateinit var mapView: MapView
    private lateinit var binding: ActivityRunBinding
    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private var permissionsListener: PermissionsListener = object : PermissionsListener{
        override fun onExplanationNeeded(permissionsToExplain: List<String>) {
            TODO("Not yet implemented")
        }
        override fun onPermissionResult(granted: Boolean) {
            if (granted) {
                setUpMap()
            } else {
                // User denied the permission
            }
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
        binding = ActivityRunBinding.inflate(layoutInflater)

        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            setUpMap()
        }

        setContentView(binding.root)
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
            location.locationPuck = createDefault2DPuck(withBearing = true)
            location.enabled = true
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
        binding.mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

}