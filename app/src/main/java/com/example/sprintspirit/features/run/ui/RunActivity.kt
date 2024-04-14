package com.example.sprintspirit.features.run.ui

import android.os.Bundle
import com.example.sprintspirit.databinding.ActivityRunBinding
import com.example.sprintspirit.ui.BaseActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class RunActivity : BaseActivity(), OnMapReadyCallback {

    private var myMap: GoogleMap? = null
    private lateinit var binding: ActivityRunBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        //mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        val sydney = LatLng(-34.0, 151.0)
        myMap!!.addMarker(MarkerOptions().position(sydney).title("Sydney"))
        myMap!!.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }


}