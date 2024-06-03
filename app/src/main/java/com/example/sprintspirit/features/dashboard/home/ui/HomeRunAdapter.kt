package com.example.sprintspirit.features.dashboard.home.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.CardHomeRunBinding
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.util.Utils.kphToMinKm
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import java.text.SimpleDateFormat

class HomeRunAdapter(var postList:List<Post>,
                     val context: Context,
                     val onOpenPost: (Post) -> Unit,
                    val onOpenChat: (Post) -> Unit,
                    val showUser: Boolean = true
) : RecyclerView.Adapter<HomeRunAdapter.HomeRunHolder>() {

    class HomeRunHolder(val binding: CardHomeRunBinding,
                        val context: Context,
                        val onOpenPost: (Post) -> Unit,
                        val goToChat: (Post) -> Unit,
                        val showUser: Boolean = true
        ) : RecyclerView.ViewHolder(binding.root), OnMapReadyCallback{
        private val mapView: MapView = binding.cardHomeRunMap
        private lateinit var map: GoogleMap
        private lateinit var path: MutableList<LatLng>
        private lateinit var post: Post

        init{
            with(mapView){
                onCreate(null)
                getMapAsync(this@HomeRunHolder)
            }
        }

        fun bind(get: Post){
            post = get
            //Path
            path = mutableListOf()
            val geoPoints = get.points!!
            for(pos in geoPoints){
                for((date, geoPoint) in pos){
                    path.add(LatLng(geoPoint.latitude, geoPoint.longitude))
                }
            }

            //Time
            val firstTime = geoPoints.first().keys.first().toLong()
            val lastTime = geoPoints.last().keys.first().toLong()
            val time: Double = (lastTime - firstTime) / 60000.0 //ms to min

            if(showUser){
                binding.tvUsername.text = get.userData.username

                //Profile Image
                Glide.with(context)
                    .load(get.userData.profilePictureUrl)
                    .into(binding.ivHomeProfilePicture)
                    .onLoadFailed(AppCompatResources.getDrawable(context, R.drawable.ic_account))
            }else{
                binding.llCardPostHeader.visibility = View.GONE
            }

            binding.tvDistanceValue.text = String.format("%.2f", get.distance) + " km"
            val timeString = String.format("%d:%02d", (time*60).toInt()/60, (time*60).toInt()%60)
            binding.tvTimeValue.text = timeString + " min"
            val pace = get.averageSpeed().kphToMinKm()
            binding.tvPaceValue.text = String.format("%.2f", pace) + " min/km"
            binding.tvDateValue.text = SimpleDateFormat("dd-MM-yy").format(get.startTime)

            binding.onPostClick = View.OnClickListener {
                onOpenPost(get)
            }

            //title
            Log.d("HomeRunAdapter", "Title: ${get.title}")
            binding.tvTitle.text = get.title
            binding.tvTitle.visibility = View.VISIBLE
            //description
            if(get.description.isNotBlank()){
                binding.tvDescription.text = get.description
                binding.tvDescription.visibility = View.VISIBLE
            }

            binding.tvGoToChat.setOnClickListener {
                goToChat(get)
            }

        }

        fun setMapLocation(){
            if(!::map.isInitialized) return

            //build zoom level
            val builder = LatLngBounds.builder()
            path.forEach{
                builder.include(it)
            }
            val bounds = builder.build()
            val padding = 75

            with(map){
                moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                mapType = GoogleMap.MAP_TYPE_HYBRID

                val pathColor = ContextCompat.getColor(binding.root.context, R.color.run_path)
                val options = PolylineOptions().addAll(path).color(pathColor)
                map.addPolyline(options)
                setOnMapClickListener {
                    onOpenPost(post)
                }
            }
        }

        override fun onMapReady(googleMap: GoogleMap) {
            map = googleMap
            setMapLocation()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRunHolder {
        val binding = CardHomeRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeRunHolder(binding, context, onOpenPost, onOpenChat, showUser)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: HomeRunHolder, position: Int) {
        holder.bind(postList.get(position))
    }


}