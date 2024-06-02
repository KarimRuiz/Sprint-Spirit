package com.example.sprintspirit.features.dashboard.profile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.CardUserRunBinding
import com.example.sprintspirit.features.run.data.RunData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import java.text.SimpleDateFormat
import java.util.Date

class ProfileRunAdapter(
    var runlist: MutableList<RunData>,
    var deleteCallback : (RunData) -> Unit,
    var postCallback : (RunData) -> Unit,
    var deletePostCallback : (RunData) -> Unit
) : RecyclerView.Adapter<ProfileRunAdapter.ProfileRunHolder>(){


    class ProfileRunHolder(val binding: CardUserRunBinding,
                           val deleteCallback: (RunData) -> Unit,
                           var postCallback : (RunData) -> Unit,
                           var deletePostCallback: (RunData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root), OnMapReadyCallback{

        private val mapView: MapView = binding.mapView
        private lateinit var map: GoogleMap
        private lateinit var path: MutableList<LatLng>
        private lateinit var run: RunData

        init{
            with(mapView){
                onCreate(null)
                getMapAsync(this@ProfileRunHolder)
            }
        }

        fun bind(get: RunData){
            run = get
            //Path
            path = mutableListOf()
            for(pos in get.points!!){
                for((date, geoPoint) in pos){
                    path.add(LatLng(geoPoint.latitude, geoPoint.longitude))
                }
            }

            //Time
            val firstTime = get.points!!.first().keys.first().toLong()
            val lastTime = get.points!!.last().keys.first().toLong()
            val time: Double = (lastTime - firstTime) / 60000.0 //ms to min

            binding.tvDistanceValue.text = get.distance.toString() + " km"
            val timeString = String.format("%d:%02d", (time*60).toInt()/60, (time*60).toInt()%60)
            binding.tvTimeValue.text = timeString + " min"
            binding.tvPaceValue.text = String.format("%.2f", (get.distance / time)) + " min/km"
            binding.tvDateValue.text = SimpleDateFormat("dd-MM-yy").format(Date(firstTime))
            if(get.public){
                binding.sivRunIsPosted.visibility = View.VISIBLE
            }else{
                binding.sivRunIsPosted.visibility = View.GONE
            }

            binding.menuSettingsListener = menuSettingsListener()
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
                //moveCamera(CameraUpdateFactory.newLatLngZoom(path[0], 13f))
                moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                mapType = GoogleMap.MAP_TYPE_HYBRID

                val pathColor = ContextCompat.getColor(binding.root.context, R.color.run_path)
                val options = PolylineOptions().addAll(path).color(pathColor)
                map.addPolyline(options)
                // setOnMapClickListener {}
            }
        }

        override fun onMapReady(googleMap: GoogleMap) {
            map = googleMap ?: return
            setMapLocation()
        }

        private fun showRunMenu(v: View){
            val popupMenu = PopupMenu(v.context, v)
            popupMenu.inflate(R.menu.menu_run_popup)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId){
                    R.id.post_menu_delete ->{
                        deleteCallback(run)
                        true
                    }
                    R.id.post_menu_switch -> {
                        postCallback(run)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
            popupMenu.show()
        }

        private fun showPostMenu(v: View) {
            val popupMenu = PopupMenu(v.context, v)
            popupMenu.inflate(R.menu.menu_post_popup)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId){
                    R.id.post_menu_delete ->{
                        deleteCallback(run)
                        true
                    }
                    R.id.post_menu_delete_post -> {
                        deletePostCallback(run)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
            popupMenu.show()
        }

        //LISTENERS
        private fun menuSettingsListener() = View.OnClickListener {
            if(!run.public){
                showRunMenu(it)
            }else{
                showPostMenu(it)
            }
        }

    }

    fun deleteListener(callback: (RunData) -> Unit){
        deleteCallback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileRunHolder {
        val binding = CardUserRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileRunHolder(binding, deleteCallback, postCallback, deletePostCallback)
    }

    override fun onBindViewHolder(holder: ProfileRunHolder, position: Int) {
        holder.bind(runlist.get(position))
    }

    override fun getItemCount(): Int {
        return runlist.size
    }

    fun removeRun(run: RunData) {
        val position = runlist.indexOf(run)
        if(position != -1){
            runlist.removeAt(position)
            notifyItemChanged(position)
        }
    }

    fun getRunAt(position: Int) = runlist[position]
    fun getPosOfRun(run: RunData) = runlist.indexOf(run)

}