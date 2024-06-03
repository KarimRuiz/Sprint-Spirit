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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sprintspirit.R
import com.example.sprintspirit.database.filters.LocationFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.databinding.FragmentHomeBinding
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.dashboard.home.ui.HomeRunAdapter
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.example.sprintspirit.util.Utils.normalize
import com.github.ybq.android.spinkit.style.ChasingDots

class HomeFragment : BaseFragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeRunAdapter

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var locationSearch: String = ""
    private var locationType: LocationFilter = LocationFilter.EMPTY

    private var following = listOf<String>()

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

    override fun onResume() {
        super.onResume()
        viewModel.currentUser.observe(viewLifecycleOwner){
            following = it.user?.following?.keys?.toList() ?: listOf()
        }
    }

    private fun subscribeUi(binding: FragmentHomeBinding) {
        val loadingAnim = ChasingDots()
        binding.skvLoadingView.setIndeterminateDrawable(loadingAnim)
        showLoading()

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

        binding.cbFilterFollowing.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                viewModel.following.value = following
            }else{
                viewModel.following.value = null
            }
        }

        binding.runsHomeRv.layoutManager = LinearLayoutManager(requireContext())
        binding.runsHomeRv.addOnChildAttachStateChangeListener(postAttacherListener())

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
            if(posts != null){
                logd("Updating...")
                binding.tvNoPostsFound.visibility = View.GONE
                if(posts.posts.isEmpty()){
                    logd("POSTS ARE EMPTY! D:")
                    binding.tvNoPostsFound.visibility = View.VISIBLE
                    hideLoading()
                }
                adapter = HomeRunAdapter(
                    postList = posts.postsByTime(),
                    context = requireContext(),
                    onOpenPost = { openPost(it) },
                    onOpenChat = { openChat(it) })
                binding.runsHomeRv.adapter = adapter
            }else{
                Log.e(TAG, "filteredRunsByData: COULDN'T GET RUNS: ${posts?.exception.toString()}")
            }
        })

        /*viewModel.filteredRunsByData.observe(viewLifecycleOwner, Observer{posts ->
            if(posts != null){
                logd("Updating...")
                binding.tvNoPostsFound.visibility = View.GONE
                if(posts.posts.isEmpty()){
                    binding.tvNoPostsFound.visibility = View.VISIBLE
                    hideLoading()
                }
                adapter = HomeRunAdapter(posts.postsByTime(), requireContext()) {
                    onOpenRun(it)
                }
                binding.runsHomeRv.adapter = adapter
            }else{
                Log.e(TAG, "filteredRunsByData: COULDN'T GET RUNS: ${posts?.exception.toString()}")
            }
        })*/
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
            showLoading()
            handler.postDelayed(runnable!!, 750)
            showLoading()
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
            showLoading()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    private fun OnOrderSelectedListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            logd("SORTING...")
            val sortedList = when(position){
                0 -> viewModel.filteredRunsByLocation.value?.posts?.sortedByDescending { it.startTime }
                1 -> viewModel.filteredRunsByLocation.value?.posts?.sortedByDescending { it.distance }
                2 -> viewModel.filteredRunsByLocation.value?.posts?.sortedBy { it.pace() }
                else -> viewModel.filteredRunsByLocation.value?.posts?.sortedBy { it.startTime }
            }
            sortedList?.take(10)
            sortedList?.let { list ->
                if (::adapter.isInitialized) {
                    adapter = HomeRunAdapter(
                        postList = list,
                        context = requireContext(),
                        onOpenPost = { openPost(it) },
                        onOpenChat = { openChat(it) })
                    (binding as FragmentHomeBinding).runsHomeRv.adapter = adapter
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}

    }

    private fun OnFilterSelectedListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            locationType = when(position){
                0 -> LocationFilter.TOWN
                1 -> LocationFilter.CITY
                2 -> LocationFilter.STATE
                else -> LocationFilter.EMPTY
            }
            viewModel.locationName.value = locationSearch
            viewModel.locationFilter.value = locationType
            showLoading()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}

    }

    private fun postAttacherListener() = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            hideLoading()
        }
        override fun onChildViewDetachedFromWindow(view: View) {}
    }

    private fun openPost(post: Post){
        navigator.navigateToPostDetail(activity, post)
    }

    private fun openChat(post: Post) {
        navigator.navigateToChat(activity, post)
    }

    private fun showLoading(){
        (binding as FragmentHomeBinding).skvLoadingView.visibility = View.VISIBLE
    }

    private fun hideLoading(){
        (binding as FragmentHomeBinding).skvLoadingView.visibility = View.GONE
    }
}