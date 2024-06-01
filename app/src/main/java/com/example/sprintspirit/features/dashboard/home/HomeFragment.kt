package com.example.sprintspirit.features.dashboard.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sprintspirit.R
import com.example.sprintspirit.database.filters.LocationFilter
import com.example.sprintspirit.database.filters.OrderFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.databinding.FragmentHomeBinding
import com.example.sprintspirit.databinding.FragmentProfileBinding
import com.example.sprintspirit.features.dashboard.DashboardViewModel
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.dashboard.home.ui.HomeRunAdapter
import com.example.sprintspirit.features.dashboard.profile.ui.ProfileRunAdapter
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.example.sprintspirit.util.Utils.normalize

class HomeFragment : BaseFragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeRunAdapter

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var locationSearch: String = ""
    private var locationType: LocationFilter = LocationFilter.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = sharedPreferences.email ?: ""
        navigator = SprintSpiritNavigator(requireContext())
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
        binding.openDrawerButton.setOnClickListener{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.place_filter_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also{adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spFilterBy.adapter = adapter
            binding.spFilterBy.setSelection(1)
        }
        binding.spFilterBy.onItemSelectedListener = OnFilterSelectedListener()

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.order_filter_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also{adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spOrderBy.adapter = adapter
            binding.spOrderBy.setSelection(0)
        }
        binding.spOrderBy.onItemSelectedListener = OnOrderSelectedListener()

        binding.etFilterBy.addTextChangedListener(OnLocationSearchChanged())

        binding.runsHomeRv.layoutManager = LinearLayoutManager(requireContext())

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.stats_filter_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.homeFragmentStats.spStats.adapter = adapter
            binding.homeFragmentStats.spStats.setSelection(1)
        }
        binding.homeFragmentStats.spStats.onItemSelectedListener = OnStatsFilterChangedListener()

        viewModel.stats.observe(viewLifecycleOwner, Observer {
            binding.homeFragmentStats.distance = String.format("%.2f", it.stats?.distance)

            val totalHours = it.stats?.time
            val hours = totalHours?.toInt() ?: 0
            val minutes = ((totalHours?.rem(1) ?: 0.0) * 60).toInt()
            logd("hours: ${hours}")
            logd("minutes: ${minutes}")
            binding.homeFragmentStats.hours = hours.toString()
            binding.homeFragmentStats.minutes = minutes.toString()

            binding.homeFragmentStats.pace = String.format("%.2f", it.stats?.pace)
        })

        viewModel.filteredRunsByLocation.observe(viewLifecycleOwner, Observer { posts ->
            logd("Filtered runs by location observer triggered: ${posts.posts.size}, ${posts.exception.toString()}")
            if(posts != null){
                logd("Updating...")
                adapter = HomeRunAdapter(posts.postsByTime(), requireContext()) {
                    onOpenRun(it)
                }
                binding.runsHomeRv.adapter = adapter
            }else{
                Log.e(TAG, "filteredRunsByData: COULDN'T GET RUNS: ${posts?.exception.toString()}")
            }
        })

        viewModel.filteredRunsByData.observe(viewLifecycleOwner, Observer{posts ->
            logd("Filtered runs observer triggered: ${posts.posts.size}, ${posts.exception.toString()}")
            if(posts != null){
                logd("Updating...")
                adapter = HomeRunAdapter(posts.postsByTime(), requireContext()) {
                    onOpenRun(it)
                }
                binding.runsHomeRv.adapter = adapter
            }else{
                Log.e(TAG, "filteredRunsByData: COULDN'T GET RUNS: ${posts?.exception.toString()}")
            }
        })
    }

    private fun OnLocationSearchChanged() = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            runnable?.let { handler.removeCallbacks(it) }

            runnable = Runnable {
                locationSearch = s.toString().normalize()
                logd("Searching posts...")
                if(locationSearch.length > 3){
                    viewModel.locationName.value = locationSearch
                }else{
                    locationType = LocationFilter.EMPTY
                    viewModel.locationFilter.value = locationType
                }
            }
            handler.postDelayed(runnable!!, 750)
        }

        override fun afterTextChanged(s: Editable?) {}

    }

    private fun OnStatsFilterChangedListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val filter = when(position){
                0 -> TimeFilter.DAILY
                1 -> TimeFilter.WEEKLY
                2 -> TimeFilter.YEARLY
                else -> TimeFilter.WEEKLY
            }
            viewModel.statsFilter.value = filter
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    private fun OnOrderSelectedListener() = object : AdapterView.OnItemSelectedListener {
        val spinner = (binding as FragmentHomeBinding).spOrderBy

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            logd("SORTING...")
            val sortedList = when(position){
                //0 -> viewModel.filteredRuns.value?.posts?.filter{it.run.isPublic}?.sortedByDescending { it.run.startTime }
                0 -> viewModel.filteredRunsByLocation.value?.posts?.sortedByDescending { it.startTime }
                1 -> viewModel.filteredRunsByLocation.value?.posts?.sortedByDescending { it.distance }
                2 -> viewModel.filteredRunsByLocation.value?.posts?.sortedBy { it.pace() }
                else -> viewModel.filteredRunsByLocation.value?.posts?.sortedBy { it.startTime }
            }
            sortedList?.take(10)
            sortedList?.let { list ->
                if (::adapter.isInitialized) {
                    adapter = HomeRunAdapter(list, requireContext(), { onOpenRun(it) })
                    (binding as FragmentHomeBinding).runsHomeRv.adapter = adapter
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    private fun OnFilterSelectedListener() = object : AdapterView.OnItemSelectedListener {
        val spinner = (binding as FragmentHomeBinding).spFilterBy

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            logd("selected: ${spinner.getItemAtPosition(position)}")
            locationType = when(position){
                0 -> LocationFilter.TOWN
                1 -> LocationFilter.CITY
                2 -> LocationFilter.STATE
                else -> LocationFilter.EMPTY
            }
            viewModel.locationName.value = locationSearch
            viewModel.locationFilter.value = locationType
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    private fun onOpenRun(post: Post) {
        navigator.navigateToChat(activity, post)
    }
}