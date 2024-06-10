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
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.ActivityRunBinding
import com.example.sprintspirit.features.run.location.LocationService
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.example.sprintspirit.util.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.collection.LLRBNode
import com.google.firebase.firestore.GeoPoint
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.IntervalSettings
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addGeoJSONSourceFeatures
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.removeGeoJSONSourceFeatures
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport


class RunActivity : BaseActivity(), //PermissionsListener//,
    WarnPermissionsDialog.WarnPermissionsListener
    {

    companion object{
        private const val STOP_VIBRATION_TIME = 100L
        private const val START_VIBRATION_TIME = 1000L

        const val CAME_FROM_NOTIFICATION = "RunActivityCameFromNotification"

        private var coordinates: MutableList<Map<String, GeoPoint>> = mutableListOf()

        private lateinit var routeSource: GeoJsonSource
        private var routeCoordinates: MutableList<Point> = mutableListOf()
    }

    private lateinit var viewModel: RunViewModel
    private lateinit var mapView: MapView
    private lateinit var binding: ActivityRunBinding

    private var locationProvider: DeviceLocationProvider? = null
    private var isReceiverRegistered = false

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
                if(sharedPreferences.isRunning){
                    val location =
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(LocationService.LOCATION_INTENT, android.location.Location::class.java)
                    }else{
                            intent.extras?.getParcelable(LocationService.LOCATION_INTENT) as? android.location.Location
                    }
                    val timeStamp = System.currentTimeMillis()
                    val runPoint = GeoPoint(location!!.latitude, location.longitude)
                    val map = mapOf<String, GeoPoint>().let {
                        it.plus(Pair(timeStamp.toString(), runPoint))
                    }
                    coordinates.add(map)
                    sharedPreferences.addCoord(timeStamp.toString(), runPoint)
                    logd("Location added to run: " + runPoint)

                    val point = Point.fromLngLat(location.longitude, location.latitude)
                    routeCoordinates.add(point)
                    updateRouteLine()
                }
            }
        }
    }

     private fun updateRouteLine(){
         logd("UPDATED ROUTE LINE")
         val featureCollection = FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(LineString.fromLngLats(routeCoordinates))))
         routeSource.feature(Feature.fromGeometry(LineString.fromLngLats(routeCoordinates)))
     }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        //jump to the current indicator position
        val cameraOptions = CameraOptions.Builder()
            .center(it)
            .build()
        binding.mapView.mapboxMap.setCamera(cameraOptions)
        //set the gestures plugin's focal point to the current indicator location.
        binding.mapView.gestures.focalPoint = binding.mapView.mapboxMap.pixelForCoordinate(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = RunViewModel(prefs = sharedPreferences)
        binding = ActivityRunBinding.inflate(layoutInflater)
        navigator = SprintSpiritNavigator(this)

        coordinates.clear()

        requestPermissions()

        routeSource = GeoJsonSource.Builder("route-source").build()

        locationProvider?.addLocationObserver(locationObserver)

        subscribeUi(binding)

        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        logd("onResume(): ${sharedPreferences.isRunning}")
        setButtonRecording(binding, sharedPreferences.isRunning)
    }

    override fun onStop() {
        super.onStop()

        binding.mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    private fun checkLocationEnabled() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGpsEnabled ){
            //GPS is not enabled, show dialog to prompt user to enable it
            EnableLocationDialog(this).show(supportFragmentManager, "ENABLE_LOCATION_DIALOG")
        }
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
        if(sharedPreferences.isRunning){
            //Continue recording?...
            //if not: sharedPreferences.isRunning = false
        }

        binding.recordRunButton.setOnClickListener(recordListener(this))

        setUpMap()
    }

    private fun showStopConfirmationDialog(onConfirm: () -> Unit, onCancel: () -> Unit){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(ContextCompat.getString(this, R.string.Confirmation))
        builder.setMessage(ContextCompat.getString(this, R.string.Are_you_sure_stop_recording))

        builder.setPositiveButton(ContextCompat.getString(this, R.string.Confirm)) { dialog, which ->
            onConfirm()
        }
        builder.setNegativeButton(ContextCompat.getString(this, R.string.Cancel)) { dialog, which ->
            onCancel()
        }

        builder.show()
    }

    private fun postRun() {
        try{
            if(canRunBeUploaded()){
                viewModel.saveRun(sharedPreferences.routePath, calculateDistance())

                sharedPreferences.isRunning = false
                navigator.navigateToHome(
                    activity = this
                )
            }else{
                sharedPreferences.isRunning = false
                val reason = getReasonNotUpload()
                if(reason != -1){
                    logd("run cannot be uploaded")
                    val minDistance = (RunViewModel.MIN_DISTANCE * 1000).toInt()
                    ErrorDialog(this.getString(reason, minDistance)).show(supportFragmentManager, "ERROR_DIALOG")
                }else{
                    ErrorDialog("Error subiendo la ruta.").show(supportFragmentManager, "ERROR_DIALOG")
                }
            }
        }catch(e: Exception){
            sharedPreferences.isRunning = false
            val reason = getReasonNotUpload()
            if(reason != -1){
                logd("run cannot be uploaded")
                val minDistance = (RunViewModel.MIN_DISTANCE * 1000).toInt()
                ErrorDialog(this.getString(reason, minDistance)).show(supportFragmentManager, "ERROR_DIALOG")
            }else{
                ErrorDialog(e.message?: "").show(supportFragmentManager, "ERROR_DIALOG")
            }
        }
    }

    private fun recordListener(context: Context) = object : View.OnClickListener {
        override fun onClick(v: View?) {
            logd("iSRunning: ${sharedPreferences.isRunning}")
            if(sharedPreferences.isRunning){
                showStopConfirmationDialog(onConfirm = {
                    Intent(applicationContext, LocationService::class.java).apply {
                        action = LocationService.ACTION_STOP
                        startService(this)
                    }
                    // Unregister receiver if registered
                    if (isReceiverRegistered) {
                        unregisterReceiver(locationReceiver)
                        isReceiverRegistered = false
                    }

                    logd("Posting run...")
                    postRun()

                    Utils.vibrate(context, STOP_VIBRATION_TIME)

                    if(!isInternetAvailable()){
                        showNoInternetWarningOnUpload()
                    }
                    routeCoordinates.clear()
                    sharedPreferences.routePath = mutableListOf()
                    locationProvider?.removeLocationObserver(locationObserver)
                    binding.mapView.mapboxMap.getStyle { style ->
                        style.removeStyleSource("route-source")
                        style.removeStyleLayer("route-layer")
                    }
                }, onCancel = { })
            }else{
                Intent(applicationContext, LocationService::class.java).apply{
                    action = LocationService.ACTION_START
                    startService(this)
                    val filter = IntentFilter(LocationService.LOCATION_INTENT)
                    ContextCompat.registerReceiver(this@RunActivity, locationReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
                    isReceiverRegistered = true
                }

                Utils.vibrate(context, START_VIBRATION_TIME)

                sharedPreferences.isRunning = true
            }
            setButtonRecording(binding, sharedPreferences.isRunning)
        }
    }

    private fun setButtonRecording(binding: ActivityRunBinding, recording: Boolean){
        logd("Is recording? $recording")
        if(!recording){
            binding.flRunRecordRedDot.visibility = View.GONE
            binding.recordRunStatus.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.recordRunStatus.text = ContextCompat.getString(this, R.string.Record)
            binding.recordRunButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record_button))
        }else{
            binding.flRunRecordRedDot.visibility = View.VISIBLE
            binding.recordRunStatus.setTextColor(ContextCompat.getColor(this, R.color.stop))
            binding.recordRunStatus.text = "STOP"
            binding.recordRunButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record_button_running))
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
            mapboxMap.loadStyle(Style.OUTDOORS) { style ->
                routeSource = GeoJsonSource
                    .Builder("route-source")
                    .featureCollection(FeatureCollection.fromFeatures(arrayOf()))
                    .build()
                style.addSource(routeSource)

                val routeLayer = LineLayer("route-layer", "route-source").apply{
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                    lineOpacity(1.0)
                    lineWidth(10.0)
                    lineColor("#A7DCDC")
                }
                style.addLayer(routeLayer)
            }
        }
        binding.mapView.addView(mapView)
    }

    private fun pathToCoords(): List<GeoPoint>{
        val coords: MutableList<GeoPoint> = mutableListOf()
        sharedPreferences.routePath.forEach {
            coords.add(it.values.first())
        }
        return coords.toList()
    }

    private fun showNoInternetWarningOnUpload(){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(ContextCompat.getString(this, R.string.No_internet))
            builder.setMessage(ContextCompat.getString(this, R.string.Route_will_upload_when_internet))

            builder.setNeutralButton(ContextCompat.getString(this, R.string.Confirm)){_, _ ->}

            builder.show()
        }

    override fun onStart() {
        super.onStart()
        binding.mapView.location
            .addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    override fun onAbsentPermissionsNeutralClick(dialog: DialogFragment) {
        finish()
    }

    private fun calculateDistance(): Double {
            val points = mutableListOf<GeoPoint>()
            sharedPreferences.routePath.forEach {
                points.add(it.values.first())
            }
            if (points.size < 2) return 0.0

            var totalDistance = 0.0
            for (i in 0 until points.size - 1) {
                totalDistance += Utils.distanceBetween(points[i], points[i + 1])
            }
            return totalDistance
    }

    fun canRunBeUploaded(): Boolean{
        return sharedPreferences.routePath.size > 2
                && calculateDistance() > RunViewModel.MIN_DISTANCE
    }

    fun getReasonNotUpload(): Int{
        val distance = calculateDistance()
        logd("Run distance: ${distance}, min distance: ${RunViewModel.MIN_DISTANCE}")
        if(distance < RunViewModel.MIN_DISTANCE){
            return R.string.Run_min_distance
        }
        return -1
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
            builder.setMessage(requireContext().getString(R.string.Accept_location_permissions))
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
            builder.setMessage(context?.getString(R.string.Enable_location))
                .setPositiveButton("Enable") { dialog, id ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    activity.startActivity(intent)
                }
                .setNegativeButton(context?.getString(R.string.Cancel) ?: "Cancelar") { dialog, id ->
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
                .setNeutralButton("Ok") { dialog, id ->
                    SprintSpiritNavigator(requireActivity()).navigateToHome(
                        activity = requireActivity()
                    )
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}