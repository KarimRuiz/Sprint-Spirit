package com.example.sprintspirit.features.post.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.adapters.TextViewBindingAdapter.AfterTextChanged
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.FragmentPostBinding
import com.example.sprintspirit.databinding.FragmentProfileBinding
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.example.sprintspirit.util.Utils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions

class PostFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var viewModel: PostViewModel
    private lateinit var run: RunData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = SprintSpiritNavigator(requireContext())
        viewModel = ViewModelProvider(this).get(PostViewModel::class.java)
        // TODO: Use the ViewModel

        run = PostActivity.runData!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        subscribeUi(binding as FragmentPostBinding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentPostBinding) {
        viewModel.email = sharedPreferences.email ?: ""
        viewModel.run = run
        viewModel.address = Utils.addressFromLocation(requireContext(), run.points?.firstOrNull()?.values?.firstOrNull()!!)

        with(binding.mapPostRun){
            onCreate(null)
            getMapAsync(this@PostFragment)
        }

        binding.edtPostTitle.addTextChangedListener(titleWatcher)
        binding.edtPostDescription.addTextChangedListener(descriptionWatcher)
        binding.onPostListener = postListener()
    }
    private fun setUpMap(map: GoogleMap) {
        val path = mutableListOf<LatLng>()
        for(pos in run.points!!){
            for((date, geoPoint) in pos){
                path.add(LatLng(geoPoint.latitude, geoPoint.longitude))
            }
        }

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

            val pathColor = ContextCompat.getColor(requireContext(), R.color.run_path)
            val options = PolylineOptions().addAll(path).color(pathColor)
            map.addPolyline(options)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        setUpMap(map)
    }

    //----------- LISTENERS AND OBSERVERS -------------

    private fun postListener(): View.OnClickListener {
        return View.OnClickListener {
            viewModel.postRun() //TODO: get if postRun was sucessful or not
            activity?.finish() //TODO: navigateBack in SprintSpiritNavigator
        }
    }

    val titleWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            viewModel.title = s.toString()
        }
    }

    val descriptionWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            viewModel.description = s.toString()
        }
    }

}