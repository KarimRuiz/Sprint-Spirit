package com.example.sprintspirit.features.run.ui

import android.os.Bundle
import com.example.sprintspirit.databinding.ActivityRunBinding
import com.example.sprintspirit.ui.BaseActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView


class RunActivity : BaseActivity() {

    private lateinit var mapView: MapView
    private lateinit var binding: ActivityRunBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRunBinding.inflate(layoutInflater)

        mapView = MapView(this)
        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(37.1, 3.6))
                .pitch(0.0)
                .zoom(10.0)
                .bearing(0.0)
                .build()
        )
        binding.mapView.addView(mapView)
        setContentView(binding.root)
    }

}