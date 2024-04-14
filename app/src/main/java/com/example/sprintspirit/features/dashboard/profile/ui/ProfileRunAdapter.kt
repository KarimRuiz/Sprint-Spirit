package com.example.sprintspirit.features.dashboard.profile.ui

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sprintspirit.databinding.CardUserRunBinding
import com.example.sprintspirit.features.run.data.RunData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class ProfileRunAdapter(var runlist:List<RunData>) : RecyclerView.Adapter<ProfileRunAdapter.ProfileRunHolder>(){

    class ProfileRunHolder(val binding: CardUserRunBinding) : RecyclerView.ViewHolder(binding.root), OnMapReadyCallback{

        private val mapView: MapView = binding.mapView
        private lateinit var map: GoogleMap
        private lateinit var path: MutableList<LatLng>

        init{
            with(mapView){
                onCreate(null)
                getMapAsync(this@ProfileRunHolder)
            }
        }

        fun bind(get: RunData){
            //Path
            path = mutableListOf()
            for(pos in get.points!!){
                for((date, geoPoint) in pos){
                    path.add(LatLng(geoPoint.latitude, geoPoint.longitude))
                }
            }

            //Time
            val firstTime = get.points.first().keys.first().toLong()
            val lastTime = get.points.last().keys.first().toLong()
            val time: Double = (lastTime - firstTime) / 60000.0 //ms to min

            binding.tvDistanceValue.text = get.distance.toString() + " km"
            val timeString = String.format("%d:%02d", (time*60).toInt()/60, (time*60).toInt()%60)
            binding.tvTimeValue.text = timeString + " min"
            binding.tvPaceValue.text = String.format("%.2f", (get.distance / time)) + " min/km"
        }

        fun setMapLocation(){
            if(!::map.isInitialized) return
            with(map){
                moveCamera(CameraUpdateFactory.newLatLngZoom(path[0], 13f))
                mapType = GoogleMap.MAP_TYPE_HYBRID
                val options = PolylineOptions().addAll(path).color(Color.GREEN)
                map.addPolyline(options)
                // setOnMapClickListener {}
            }
        }

        fun setMapLocation(latitude: Double, longitude: Double) {
            Log.d("ProfileRunAdapter", "Map loading...")
            binding.mapView.getMapAsync { googleMap ->
                googleMap.setOnMapLoadedCallback {
                    Log.d("ProfileRunAdapter", "Map loaded, adding marker...")
                    val location = LatLng(latitude, longitude)
                    googleMap.addMarker(MarkerOptions().position(location).title("Your Location"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 20f))
                    Log.d("ProfileRunAdapter", "Map loaded.")
                }
            }
        }

        override fun onMapReady(googleMap: GoogleMap) {
            map = googleMap ?: return
            setMapLocation()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileRunHolder {
        val binding = CardUserRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileRunHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileRunHolder, position: Int) {
        holder.bind(runlist.get(position))
    }

    override fun getItemCount(): Int {
        return runlist.size
    }

}