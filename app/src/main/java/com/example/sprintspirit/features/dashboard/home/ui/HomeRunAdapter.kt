package com.example.sprintspirit.features.dashboard.home.ui

import android.content.Context
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
                    val onOpenChat: (Post) -> Unit
) : RecyclerView.Adapter<HomeRunAdapter.HomeRunHolder>() {

    class HomeRunHolder(val binding: CardHomeRunBinding,
                        val context: Context,
                        val goToChat: (Post) -> Unit
        ) : RecyclerView.ViewHolder(binding.root), OnMapReadyCallback{
        private val mapView: MapView = binding.cardHomeRunMap
        private lateinit var map: GoogleMap
        private lateinit var path: MutableList<LatLng>

        init{
            with(mapView){
                onCreate(null)
                getMapAsync(this@HomeRunHolder)
            }
        }

        fun bind(get: Post){
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

            binding.tvUsername.text = get.userData.username
            binding.tvDistanceValue.text = get.distance.toString() + " km"
            val timeString = String.format("%d:%02d", (time*60).toInt()/60, (time*60).toInt()%60)
            binding.tvTimeValue.text = timeString + " min"
            binding.tvPaceValue.text = String.format("%.2f", (time / get.distance)) + " min/km"
            binding.tvDateValue.text = SimpleDateFormat("dd-MM-yy").format(get.startTime)
            //description
            if(get.description.isNotBlank()){
                binding.tvDescription.text = get.description
                binding.tvDescription.visibility = View.VISIBLE
            }

            binding.tvGoToChat.setOnClickListener {
                goToChat(get)
            }

            //Profile Image
            Glide.with(context)
                .load(get.userData.profilePictureUrl)
                .into(binding.ivHomeProfilePicture)
                .onLoadFailed(AppCompatResources.getDrawable(context, R.drawable.ic_account))

        }

        fun setMapLocation(){
            if(!::map.isInitialized) return

            //Build zoom level
            val builder = LatLngBounds.builder()
            builder.include(path.first())
            builder.include(path.last())
            val bounds = builder.build()
            val padding = 75

            with(map){
                moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                mapType = GoogleMap.MAP_TYPE_HYBRID

                val pathColor = ContextCompat.getColor(binding.root.context, R.color.run_path)
                val options = PolylineOptions().addAll(path).color(pathColor)
                map.addPolyline(options)
                // setOnMapClickListener {}
            }
        }

        override fun onMapReady(googleMap: GoogleMap) {
            map = googleMap
            setMapLocation()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRunHolder {
        val binding = CardHomeRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeRunHolder(binding, context, onOpenChat)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: HomeRunHolder, position: Int) {
        holder.bind(postList.get(position))
    }


}