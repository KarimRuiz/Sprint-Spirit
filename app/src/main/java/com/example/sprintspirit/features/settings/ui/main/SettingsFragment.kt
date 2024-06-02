package com.example.sprintspirit.features.settings.ui.main

import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.databinding.FragmentSettingsBinding
import com.example.sprintspirit.features.run.location.LocationRefreshRate
import com.example.sprintspirit.ui.BaseFragment


class SettingsFragment : BaseFragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        sharedPreferences = Preferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        
        subscribeUi(binding as FragmentSettingsBinding)
        
        return binding.root
    }

    private fun subscribeUi(binding: FragmentSettingsBinding) {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.location_rates_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spLocationRefreshRate.adapter = adapter

            logd("?Current rate: ${sharedPreferences.locationRefreshRate}")
            val refreshRate = LocationRefreshRate.fromMilliseconds(sharedPreferences.locationRefreshRate)
            logd("?Current rate: ${refreshRate.toString()}")
            logd("?Current rate ordinal: ${refreshRate.ordinal}")
            binding.spLocationRefreshRate.setSelection(refreshRate.ordinal)
        }
        binding.spLocationRefreshRate.onItemSelectedListener = OnRefreshRateListener()

    }

    private fun setWarning(tv: TextView, message: String?){
        if(message != null){
            tv.visibility = View.VISIBLE
            tv.setText(message)
        }else{
            tv.visibility = View.GONE
        }
    }

    private fun OnRefreshRateListener() = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val refreshRate = LocationRefreshRate.entries.get(position)
            if(refreshRate == LocationRefreshRate.LOWEST){
                setWarning(
                    (binding as FragmentSettingsBinding).tvLocationRefreshRateWarning,
                    requireContext().getString(R.string.Lowest_refresh_warning)
                )
            }else if(refreshRate == LocationRefreshRate.HIGHEST){
                setWarning(
                    (binding as FragmentSettingsBinding).tvLocationRefreshRateWarning,
                    requireContext().getString(R.string.Highest_refresh_warning)
                )
            }else{
                setWarning(
                    (binding as FragmentSettingsBinding).tvLocationRefreshRateWarning,
                    null
                )
            }
            sharedPreferences.locationRefreshRate = refreshRate.getMilli()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

}