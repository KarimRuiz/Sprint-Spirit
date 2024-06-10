package com.example.sprintspirit.features.post_detail.ui

import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.FragmentPostDetailBinding
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.post_detail.PostDetailActivity
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.ui.custom.ReportDialog
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.example.sprintspirit.util.Utils
import com.example.sprintspirit.util.Utils.distanceBetween
import com.example.sprintspirit.util.Utils.kphToMinKm
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.example.sprintspirit.util.Utils.toGeoPoint
import com.github.ybq.android.spinkit.style.ChasingDots
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import java.text.SimpleDateFormat

class PostDetailFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var viewModel: PostDetailViewModel

    private lateinit var post: Post
    private lateinit var map: GoogleMap
    private lateinit var path: MutableList<LatLng>
    private lateinit var speeds: MutableList<Double>
    private var minSpeed: Double = Double.MAX_VALUE
    private var maxSpeed: Double = Double.MIN_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireInternet(compulsory = true)
        viewModel = ViewModelProvider(this).get(PostDetailViewModel::class.java)
        navigator = SprintSpiritNavigator(requireContext())

        post = PostDetailActivity.post!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        subscribeUi(binding as FragmentPostDetailBinding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentPostDetailBinding) {
        val loadingAnim = ChasingDots()
        binding.skvLoadingView.setIndeterminateDrawable(loadingAnim)

        createPathList()

        binding.goToChat = View.OnClickListener {
            navigator.navigateToChat(
                activity = activity,
                post = post
            )
        }

        binding.onUserClick = View.OnClickListener {
            navigator.navigateToProfileDetail(
                activity = activity,
                userId = post.user
            )
        }

        with(binding.postDetailMap){
            onCreate(null)
            getMapAsync(this@PostDetailFragment)
        }
        binding.tvUsername.text = post.userData.username
        binding.tvTitle.text = post.title
        binding.tvDescription.movementMethod = ScrollingMovementMethod.getInstance()
        binding.tvDescription.text = post.description
        binding.tvDate.text = SimpleDateFormat("dd-MM-yy").format(post.startTime)

        binding.postDetailDistance.text = String.format("%.2f",post.distance) + " km"
        val firstTime = post.points!!.first().keys.first().toLong()
        val lastTime = post.points!!.last().keys.first().toLong()
        val time: Double = (lastTime - firstTime) / 60000.0
        val timeString = String.format("%d:%02d", (time*60).toInt()/60, (time*60).toInt()%60)
        binding.postDetailTime.text = timeString + " min"

        //Pace
        binding.tvPaceAvg.text = String.format("%.2f", post.averageSpeed().kphToMinKm())

        if(post.userData.profilePictureUrl == null){
            Glide.with(requireContext())
                .load(R.drawable.ic_account)
                .into(binding.ivHomeProfilePicture)
                .onLoadFailed(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_account))
        }else{
            Glide.with(requireContext())
                .load(post.userData.profilePictureUrl)
                .into(binding.ivHomeProfilePicture)
                .onLoadFailed(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_account))
        }

        if(sharedPreferences.isAdmin){
            binding.btnReport.visibility = View.GONE
            binding.btnDelete.visibility = View.VISIBLE
        }

        if(!viewModel.postExists(post.id)){
            Toast.makeText(
                activity,
                "Esta publicación ya no existe",
                Toast.LENGTH_LONG
            ).show()
            navigator.goBack(activity)
        }

        binding.onReport = onReportPost()
        binding.onDelete = onDeletePost()

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
                (binding as FragmentPostDetailBinding).skvLoadingView.visibility = View.GONE
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
        logd("Total points: ${post.points!!.size}")
        val geoPoints = Utils.shortenList(post.points!!)
        var lastLatLng: LatLng? = null
        var lastTime: Long? = null

        for (pos in geoPoints) {
            for ((timeString, geoPoint) in pos) {
                val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
                val time = timeString.toLong()

                if (lastLatLng != null && lastTime != null) {
                    val distance = distanceBetween(lastLatLng.toGeoPoint(), latLng.toGeoPoint())
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

    private fun onReportPost() = object : View.OnClickListener {
        override fun onClick(v: View?) {
            ReportDialog(
                context = requireContext(),
                type = "post",
                id = post.id
            ).showDialog()
        }
    }

    private fun onDeletePost() = object : View.OnClickListener {
        override fun onClick(v: View?) {
            viewModel.deletePost(post)
            Toast.makeText(
                activity,
                "Publicación eliminada.",
                Toast.LENGTH_LONG
            ).show()
            navigator.goBack(activity)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setUpMap()
    }

    override fun onResume() {
        super.onResume()
        (binding as FragmentPostDetailBinding).postDetailMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        (binding as FragmentPostDetailBinding).postDetailMap.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        (binding as FragmentPostDetailBinding).postDetailMap.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        (binding as FragmentPostDetailBinding).postDetailMap.onLowMemory()
    }

}
