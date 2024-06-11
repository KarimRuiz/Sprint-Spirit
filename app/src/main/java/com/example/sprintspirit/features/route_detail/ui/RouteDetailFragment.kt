package com.example.sprintspirit.features.route_detail.ui

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.databinding.FragmentRouteDetailBinding
import com.example.sprintspirit.features.dashboard.DashboardViewModel
import com.example.sprintspirit.features.post_detail.ui.PostDetailViewModel
import com.example.sprintspirit.features.route_detail.RouteDetailActivity
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.example.sprintspirit.util.Utils
import com.example.sprintspirit.util.Utils.kphToMinKm
import com.example.sprintspirit.util.Utils.toGeoPoint
import com.github.ybq.android.spinkit.style.ChasingDots
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import java.text.SimpleDateFormat

class RouteDetailFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var viewModel: DashboardViewModel

    private lateinit var route: RunData
    private lateinit var map: GoogleMap
    private lateinit var path: MutableList<LatLng>
    private lateinit var speeds: MutableList<Double>
    private var minSpeed: Double = Double.MAX_VALUE
    private var maxSpeed: Double = Double.MIN_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireInternet(compulsory = true)
        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        viewModel.email = Preferences(requireContext()).email
        navigator = SprintSpiritNavigator(requireContext())

        route = RouteDetailActivity.route!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRouteDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        subscribeUi(binding as FragmentRouteDetailBinding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentRouteDetailBinding) {
        val loadingAnim = ChasingDots()
        binding.skvLoadingView.setIndeterminateDrawable(loadingAnim)

        createPathList()
        binding.tvDate.text = SimpleDateFormat("dd-MM-yy").format(route.startTime)

        binding.postDetailDistance.text = String.format("%.2f",route.distance) + " km"
        val firstTime = route.points!!.first().keys.first().toLong()
        val lastTime = route.points!!.last().keys.first().toLong()
        val time: Double = (lastTime - firstTime) / 60000.0
        val timeString = String.format("%d:%02d", (time*60).toInt()/60, (time*60).toInt()%60)
        binding.postDetailTime.text = timeString + " min"

        //Pace
        binding.tvPaceAvg.text = String.format("%.2f", route.averageSpeed().kphToMinKm())

        binding.onDelete = View.OnClickListener {
            showDeleteConfirmationDialog(onConfirm = {
                viewModel.deleteRun(route)
                navigator.navigateToHome(
                    activity = activity,
                    preserveStack = false
                )
            }, onCancel = {})

        }

        binding.onPost = View.OnClickListener {
            navigator.navigateToPostRun(
                activity = activity,
                run = route,
                preserveStack = false
            )
        }

        with(binding.postDetailMap){
            onCreate(null)
            getMapAsync(this@RouteDetailFragment)
        }
    }

    private fun showDeleteConfirmationDialog(onConfirm: () -> Unit, onCancel: () -> Unit){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(ContextCompat.getString(requireContext(), R.string.Confirmation))
        builder.setMessage(ContextCompat.getString(requireContext(), R.string.Are_you_sure_delete_run))

        builder.setPositiveButton(ContextCompat.getString(requireContext(), R.string.Confirm)) { dialog, which ->
            onConfirm()
        }
        builder.setNegativeButton(ContextCompat.getString(requireContext(), R.string.Cancel)) { dialog, which ->
            onCancel()
        }

        builder.show()
    }

    private fun setUpMap() {
        if(!::map.isInitialized) return

        //initial zoom level
        val builder = LatLngBounds.builder()
        path.forEach{
            builder.include(it)
        }
        val bounds = builder.build()
        val padding = 75

        with(map){
            moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            mapType = GoogleMap.MAP_TYPE_HYBRID

            drawColoredPath()

            setOnMapClickListener {}
            setOnMapLoadedCallback {
                (binding as FragmentRouteDetailBinding).skvLoadingView.visibility = View.GONE
            }
        }
    }

    private fun drawColoredPath() {
        for (i in 0 until path.size - 1) {
            val start = path[i]
            val end = path[i + 1]
            val speed = speeds.getOrNull(i) ?: 0.0

            val color = getColorForSpeed(speed)
            val options = PolylineOptions()
                .add(start, end)
                .color(color)
                .width(5f)

            map.addPolyline(options)
        }
    }

    private fun getColorForSpeed(speed: Double): Int {
        val normalizedSpeed = (speed - minSpeed) / (maxSpeed - minSpeed)
        return interpolateColor(Color.RED, Color.GREEN, normalizedSpeed)
    }

    private fun interpolateColor(colorStart: Int, colorEnd: Int, factor: Double): Int {
        val startA = Color.alpha(colorStart)
        val startR = Color.red(colorStart)
        val startG = Color.green(colorStart)
        val startB = Color.blue(colorStart)

        val endA = Color.alpha(colorEnd)
        val endR = Color.red(colorEnd)
        val endG = Color.green(colorEnd)
        val endB = Color.blue(colorEnd)

        val a = (startA + factor * (endA - startA)).toInt()
        val r = (startR + factor * (endR - startR)).toInt()
        val g = (startG + factor * (endG - startG)).toInt()
        val b = (startB + factor * (endB - startB)).toInt()

        return Color.argb(a, r, g, b)
    }

    private fun createPathList() {
        path = mutableListOf()
        speeds = mutableListOf()
        logd("Total points: ${route.points!!.size}")
        val geoPoints = Utils.shortenList(route.points!!)
        var lastLatLng: LatLng? = null
        var lastTime: Long? = null

        for (pos in geoPoints) {
            for ((timeString, geoPoint) in pos) {
                val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
                val time = timeString.toLong()

                if (lastLatLng != null && lastTime != null) {
                    val distance =
                        Utils.distanceBetween(lastLatLng.toGeoPoint(), latLng.toGeoPoint())
                    val timeDiff = (time - lastTime) / 1000.0 / 3600.0 // convert milliseconds to hours
                    val speed = distance / timeDiff
                    speeds.add(speed)

                    if (speed < minSpeed) minSpeed = speed
                    if (speed > maxSpeed) maxSpeed = speed
                }

                path.add(latLng)
                lastLatLng = latLng
                lastTime = time
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setUpMap()
    }

}