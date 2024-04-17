package com.example.sprintspirit.features.dashboard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.FragmentHomeBinding
import com.example.sprintspirit.databinding.FragmentProfileBinding
import com.example.sprintspirit.features.dashboard.DashboardViewModel
import com.example.sprintspirit.ui.BaseFragment

class HomeFragment : BaseFragment() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = sharedPreferences.email ?: ""
        viewModel = HomeViewModel(user = email)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        subscribeUi(binding as FragmentHomeBinding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentHomeBinding) {
        viewModel.weeklyStats.observe(viewLifecycleOwner, Observer {
            binding.homeFragmentStats.distance = String.format("%.2f", it.stats?.distance)

            val totalMinutes = it.stats?.time
            val hours = totalMinutes?.div(60)?.toInt()
            val minutes = totalMinutes?.rem(60)?.toInt()
            binding.homeFragmentStats.hours = hours.toString()
            binding.homeFragmentStats.minutes = minutes.toString()

            binding.homeFragmentStats.pace = String.format("%.2f", it.stats?.pace)
        })
    }
}