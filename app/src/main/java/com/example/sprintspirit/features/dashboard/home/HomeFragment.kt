package com.example.sprintspirit.features.dashboard.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sprintspirit.R
import com.example.sprintspirit.database.filters.OrderFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.databinding.FragmentHomeBinding
import com.example.sprintspirit.databinding.FragmentProfileBinding
import com.example.sprintspirit.features.dashboard.DashboardViewModel
import com.example.sprintspirit.features.dashboard.home.ui.HomeRunAdapter
import com.example.sprintspirit.features.dashboard.profile.ui.ProfileRunAdapter
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.ui.BaseFragment

class HomeFragment : BaseFragment(), AdapterView.OnItemSelectedListener {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeRunAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = sharedPreferences.email ?: ""
        viewModel = HomeViewModel(
            user = email
        )
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
        binding.runsHomeRv.layoutManager = LinearLayoutManager(requireContext())

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.order_filter_array,
            android.R.layout.simple_spinner_item
        ).also{adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spHomeOrder.adapter = adapter
        }
        binding.spHomeOrder.onItemSelectedListener = this

        viewModel.weeklyStats.observe(viewLifecycleOwner, Observer {
            binding.homeFragmentStats.distance = String.format("%.2f", it.stats?.distance)

            val totalMinutes = it.stats?.time
            val hours = totalMinutes?.div(60)?.toInt()
            val minutes = totalMinutes?.rem(60)?.toInt()
            binding.homeFragmentStats.hours = hours.toString()
            binding.homeFragmentStats.minutes = minutes.toString()

            binding.homeFragmentStats.pace = String.format("%.2f", it.stats?.pace)
        })

        viewModel.filteredRuns.observe(viewLifecycleOwner, Observer{runs ->
            if((runs != null) and (runs!!.runs != null)){
                logd("Updating...")
                adapter = HomeRunAdapter(runs.runs!!)
                binding.runsHomeRv.adapter = adapter
            }else{
                Log.e(TAG, "filteredRuns: COULDN'T GET RUNS: ${runs.exception.toString()}")
            }
        })
    }

    private fun fillRuns(runs: List<RunData>){
        adapter = HomeRunAdapter(runs)
        (binding as FragmentProfileBinding).runProfileRv.adapter = adapter
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        logd("Selected position: ${position}")
        when(position){
            0 -> viewModel.setOrderFilter(OrderFilter.NEW)
            1 -> viewModel.setOrderFilter(OrderFilter.DISTANCE)
            else -> viewModel.setOrderFilter(OrderFilter.NEW)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}