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
import com.example.sprintspirit.util.Utils
import com.example.sprintspirit.util.Utils.kphToMinKm
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
    var deletePostCallback : (RunData) -> Unit,
    var goToRunDetail: (RunData) -> Unit,
    var goToPost: (RunData) -> Unit,
    val showMap: Boolean = true,
) : RecyclerView.Adapter<ProfileRunAdapter.ProfileRunHolder>(){


    class ProfileRunHolder(val binding: CardUserRunBinding,
                           val deleteCallback: (RunData) -> Unit,
                           var postCallback : (RunData) -> Unit,
                           var deletePostCallback: (RunData) -> Unit,
                           var goToRunDetail: (RunData) -> Unit,
                           var goToPost: (RunData) -> Unit,
                           val showMap: Boolean
    ) : RecyclerView.ViewHolder(binding.root), OnMapReadyCallback{

        private val mapView: MapView = binding.mapView
        private lateinit var map: GoogleMap
        private lateinit var path: MutableList<LatLng>
        private lateinit var run: RunData

        init{
            with(mapView){
                onCreate(null)
                onResume()
                getMapAsync(this@ProfileRunHolder)
            }
        }

        fun bind(get: RunData){
            run = get
            //Path
            path = mutableListOf()
            val points = Utils.shortenList(get.points!!)
            for(pos in points){
                for((date, geoPoint) in pos){
                    path.add(LatLng(geoPoint.latitude, geoPoint.longitude))
                }
            }

            if(showMap){
                binding.mapView.visibility = View.VISIBLE
                binding.tvYouNeedInternet.visibility = View.GONE
            }else{
                binding.mapView.visibility = View.GONE
                binding.tvYouNeedInternet.visibility = View.VISIBLE
            }

            //Time
            val firstTime = get.points!!.first().keys.first().toLong()
            val lastTime = get.points!!.last().keys.first().toLong()
            val time: Double = (lastTime - firstTime) / 60000.0 //ms to min

            binding.tvDistanceValue.text = String.format("%.2f", get.distance) + " km"
            val timeString = String.format("%d:%02d", (time*60).toInt()/60, (time*60).toInt()%60)
            binding.tvTimeValue.text = timeString + " min"
            val pace = get.averageSpeed().kphToMinKm()
            binding.tvPaceValue.text = String.format("%.2f", pace) + " min/km"
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

            //build zoom level
            val builder = LatLngBounds.builder()
            path.forEach{
                builder.include(it)
            }
            val bounds = builder.build()
            val padding = 150

            with(map){
                //moveCamera(CameraUpdateFactory.newLatLngZoom(path[0], 13f))
                moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                mapType = GoogleMap.MAP_TYPE_HYBRID

                val pathColor = ContextCompat.getColor(binding.root.context, R.color.run_path)
                val options = PolylineOptions().addAll(path).color(pathColor)
                map.addPolyline(options)
                    if(!run.public){
                        this.setOnMapClickListener {
                            goToRunDetail(run)
                        }
                    }else{
                        this.setOnMapClickListener {
                            goToPost(run)
                        }
                    }
            }
        }

        override fun onMapReady(googleMap: GoogleMap) {
            map = googleMap ?: return
            if(showMap) setMapLocation()
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
                    R.id.post_menu_go_to_post -> {
                        goToPost(run)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileRunHolder {
        val binding = CardUserRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ProfileRunHolder(binding, deleteCallback, postCallback, deletePostCallback, goToRunDetail, goToPost, showMap)
        holder.setIsRecyclable(false)
        return holder
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
            notifyDataSetChanged()
        }
    }

    fun getRunAt(position: Int) = runlist[position]
    fun getPosOfRun(run: RunData) = runlist.indexOf(run)
    fun unPost(run: RunData) {
        val position = runlist.indexOf(run)
        if(position != -1){
            runlist[position].public = false
            notifyDataSetChanged()
        }
    }

}