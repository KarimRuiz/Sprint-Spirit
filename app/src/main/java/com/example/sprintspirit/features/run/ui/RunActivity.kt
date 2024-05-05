package com.example.sprintspirit.features.run.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.ActivityRunBinding
import com.example.sprintspirit.features.run.location.LocationService
import com.example.sprintspirit.ui.BaseActivity
import com.google.firebase.firestore.GeoPoint
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.IntervalSettings
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport


class RunActivity : BaseActivity(), //PermissionsListener//,
    WarnPermissionsDialog.WarnPermissionsListener
    {

    private lateinit var viewModel: RunViewModel
    private lateinit var mapView: MapView
    private lateinit var binding: ActivityRunBinding

    private val locatonService = LocationServiceFactory.getOrCreate()
    private var locationProvider: DeviceLocationProvider? = null
    private var isReceiverRegistered = false

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

        requestPermissions()

        locationProvider?.addLocationObserver(locationObserver)

        subscribeUi(binding)

        setContentView(binding.root)
    }

    private fun checkLocationEnabled() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGpsEnabled) {
            //GPS is not enabled, show dialog to prompt user to enable it
            EnableLocationDialog(this).show(supportFragmentManager, "ENABLE_LOCATION_DIALOG")
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocationEnabled()
    }

    private fun requestPermissions(){
        logd("Requesting permissions...")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            0
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            val fineLocationPermission =
                grantResults.getOrNull(permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION))
            if (fineLocationPermission == PackageManager.PERMISSION_GRANTED) {
                logd("Got permissions")
                setUpMap()
                checkLocationEnabled()
            } else {
                logd("Didn't get permissions")
                 warnUserOfPermissions()
            }
        }
    }

    private fun warnUserOfPermissions(){
        WarnPermissionsDialog(this).show(supportFragmentManager, "LOCATION_DIALOG")
    }

    private fun subscribeUi(binding: ActivityRunBinding) {
        binding.recordRunButton.setOnClickListener {
            logd("viewModel.isRunning(): ${viewModel.isRunning()}")
            if(viewModel.isRunning()){
                Intent(applicationContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_STOP
                    startService(this)
                }
                // Unregister receiver if registered
                if (isReceiverRegistered) {
                    unregisterReceiver(locationReceiver)
                    isReceiverRegistered = false
                }

                binding.recordRunStatus.text = "Record"
                binding.recordRunButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record_button))
                postRun()
            }else{
                Intent(applicationContext, LocationService::class.java).apply{
                    action = LocationService.ACTION_START
                    startService(this)
                    val filter = IntentFilter(LocationService.LOCATION_INTENT)
                    ContextCompat.registerReceiver(this@RunActivity, locationReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
                    isReceiverRegistered = true
                }

                binding.recordRunStatus.text = "Stop"
                binding.recordRunButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record_button_running))
                viewModel.startRun()
            }
        }
    }

    private fun postRun() {
        try{
            viewModel.saveRun()
        }catch(e: Exception){
            ErrorDialog(e.message?: "").show(supportFragmentManager, "ERROR_DIALOG")
        }
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

    override fun onAbsentPermissionsNeutralClick(dialog: DialogFragment) {
        finish()
    }

}

class WarnPermissionsDialog(
    var fragListener: WarnPermissionsListener
) : DialogFragment() {
    interface WarnPermissionsListener {
        fun onAbsentPermissionsNeutralClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Accept location permissions if you want to start recording an activity...")
                .setNeutralButton("Ok") { dialog, id ->
                    var intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    var uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.data = uri

                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(intent)
                    }else{
                        intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        uri = Uri.fromParts("package", requireContext().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }

                    fragListener.onAbsentPermissionsNeutralClick(this)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

class EnableLocationDialog(
    val activity: Activity
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Enable location if you want to start recording an activity...")
                .setPositiveButton("Enable") { dialog, id ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    activity.startActivity(intent)
                }
                .setNegativeButton("Cancel") { dialog, id ->
                    activity.finish()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

class ErrorDialog(
    val message: String
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(message)
                .setNeutralButton("Ok") { dialog, id -> }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}