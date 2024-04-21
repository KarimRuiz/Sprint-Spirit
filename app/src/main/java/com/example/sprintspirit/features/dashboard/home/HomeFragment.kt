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

        viewModel.filteredRuns.observe(viewLifecycleOwner, Observer{posts ->
            logd("Filtered runs observer triggered.")
            if(posts != null){
                logd("Updating...")
                adapter = HomeRunAdapter(posts.postsByTime(), requireContext())
                binding.runsHomeRv.adapter = adapter
            }else{
                Log.e(TAG, "filteredRuns: COULDN'T GET RUNS: ${posts?.exception.toString()}")
            }
        })
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val sortedList = when(position){
            0 -> viewModel.filteredRuns.value?.posts?.filter{it.run.isPublic}?.sortedByDescending { it.run.startTime }
            1 -> viewModel.filteredRuns.value?.posts?.filter{it.run.isPublic}?.sortedByDescending { it.run.distance }
            2 -> viewModel.filteredRuns.value?.posts?.filter{it.run.isPublic}?.sortedByDescending { (it.run.distance)/(it.run.startTime.time/(1000*60)) }
            else -> viewModel.filteredRuns.value?.posts?.filter{it.run.isPublic}?.sortedBy { it.run.startTime }
        }
        sortedList?.take(10)
        sortedList?.let { list ->
            if (::adapter.isInitialized) {
                adapter = HomeRunAdapter(list, requireContext())
                (binding as FragmentHomeBinding).runsHomeRv.adapter = adapter
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}