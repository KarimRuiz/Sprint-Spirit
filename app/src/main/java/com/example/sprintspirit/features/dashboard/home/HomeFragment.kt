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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sprintspirit.R
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.database.filters.LocationFilter
import com.example.sprintspirit.database.filters.OrderFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.databinding.FragmentHomeBinding
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.dashboard.home.data.PostsResponse
import com.example.sprintspirit.features.dashboard.home.data.StatsResponse
import com.example.sprintspirit.features.dashboard.home.ui.HomeRunAdapter
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.example.sprintspirit.util.Utils.normalize
import com.github.ybq.android.spinkit.style.ChasingDots

class HomeFragment : BaseFragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeRunAdapter

    private var following = listOf<String>()
    private var filterByFollowing = true
    private var locationType: LocationFilter = LocationFilter.EMPTY
    private var order: OrderFilter = OrderFilter.NEW
    private var locationName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = SprintSpiritNavigator(requireContext())
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
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
        requireInternet()

        //Listen for posts
        viewModel.posts.observe(viewLifecycleOwner, fillPosts(binding as FragmentHomeBinding))
        fetchPosts()

        viewModel.fetchCurrentUser()
    }

    private fun subscribeUi(binding: FragmentHomeBinding) {
        val loadingAnim = ChasingDots()
        binding.skvLoadingView.setIndeterminateDrawable(loadingAnim)

        viewModel.user = sharedPreferences.email ?: ""
        viewModel.currentUser.observe(viewLifecycleOwner){
            sharedPreferences.isAdmin = it?.isAdmin ?: false
            logd("Is user ${it} banned? ${it?.isBanned}")
            if(it?.isBanned ?: false){
                sharedPreferences.email =null
                sharedPreferences.username = null
                DBManager.getCurrentDBManager().signOut()
                navigator.navigateToLogIn(activity, false)
            }

            following = if(it != null){
                sharedPreferences.email = it.email
                sharedPreferences.username = it.username
                it.following.keys.toList()
            }else{
                listOf()
            }
        }

        //Set up spinners
        setupSpinners(binding)

        //Filter by following
        binding.cbFilterFollowing.setOnCheckedChangeListener { _, isChecked ->
            filterByFollowing = isChecked
        }

        binding.runsHomeRv.layoutManager = LinearLayoutManager(requireContext())
        binding.runsHomeRv.addOnChildAttachStateChangeListener(postAttacherListener())

        //Stats
        viewModel.stats.observe(viewLifecycleOwner, observeStats(binding))

        //Buttons
        binding.openDrawerButton.setOnClickListener{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.onApplyFilter = View.OnClickListener {
            binding.drawerLayout.close()
            fetchPosts()
        }
    }

    private fun fetchPosts(){
        logd("Fetching posts")
        viewModel.following = if(filterByFollowing){
            following
        }else{
            null
        }
        viewModel.orderBy = order
        viewModel.locationName = locationName
        viewModel.locationFilter = locationType

        viewModel.fetchPosts()
        showLoading()
    }

    private fun setupSpinners(binding: FragmentHomeBinding) {
        //Location filters
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

        //Location name
        binding.etFilterBy.addTextChangedListener(OnLocationSearchChanged())

        //Order filter
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

        //Stats
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
    }

    //OBSERVERS

    private fun fillPosts(binding: FragmentHomeBinding) = Observer<PostsResponse> { response ->
        if(response.exception != null){
            loge("Couldnt get posts: ${response.exception.toString()}")
        }
        val posts = response.posts
        binding.tvNoPostsFound.visibility = if(posts.isEmpty()){
            View.VISIBLE
        }else{
            View.INVISIBLE
        }
        adapter = HomeRunAdapter(
            postList = posts,
            context = requireContext(),
            onOpenPost = { openPost(it) },
            onOpenChat = { openChat(it) })
        binding.runsHomeRv.adapter = adapter
        hideLoading()
    }

    private fun observeStats(binding: FragmentHomeBinding) = Observer<StatsResponse> {
        binding.homeFragmentStats.distance = String.format("%.2f", it.stats?.distance)

        val totalHours = it.stats?.time
        val hours = totalHours?.toInt() ?: 0
        val minutes = ((totalHours?.rem(1) ?: 0.0) * 60).toInt()

        binding.homeFragmentStats.hours = hours.toString()
        binding.homeFragmentStats.minutes = minutes.toString()

        binding.homeFragmentStats.pace = String.format("%.2f", it.stats?.pace)
    }

    private fun postAttacherListener() = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            hideLoading()
        }
        override fun onChildViewDetachedFromWindow(view: View) {}
    }

    private fun OnFilterSelectedListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            locationType = when(position){
                0 -> LocationFilter.TOWN
                1 -> LocationFilter.CITY
                2 -> LocationFilter.STATE
                else -> LocationFilter.EMPTY
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    private fun OnLocationSearchChanged() = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            locationName = s.toString()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun OnOrderSelectedListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            order = when(position){
                0 -> OrderFilter.NEW
                1 -> OrderFilter.DISTANCE
                2 -> OrderFilter.TIME
                else -> OrderFilter.NEW
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {}
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
/*
class HomeFragmentOld : BaseFragment() {

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

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.user = email
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
        requireInternet()
        viewModel.currentUser.observe(viewLifecycleOwner){
            logd("USER: ${it}")
            following = if(it != null){
                it.following.keys.toList()
            }else{
                listOf()
            }
            logd("following: ${following.toString()}")
            if((binding as FragmentHomeBinding).cbFilterFollowing.isChecked){
                viewModel.following.value = following
            }else{
                viewModel.following.value = null
            }

            (binding as FragmentHomeBinding).cbFilterFollowing.setOnCheckedChangeListener { buttonView, isChecked ->
                showLoading()
                if(isChecked){
                    viewModel.following.value = following
                }else{
                    viewModel.following.value = null
                }
            }

            viewModel.filteredRunsByLocation.observe(viewLifecycleOwner, Observer { posts ->
                if(posts != null){
                    logd("Updating...")
                    (binding as FragmentHomeBinding).tvNoPostsFound.visibility = View.GONE
                    if(posts.posts.isEmpty()){
                        logd("POSTS ARE EMPTY! D:")
                        (binding as FragmentHomeBinding).tvNoPostsFound.visibility = View.VISIBLE
                        hideLoading()
                    }
                    adapter = HomeRunAdapter(
                        postList = posts.posts,
                        context = requireContext(),
                        onOpenPost = { openPost(it) },
                        onOpenChat = { openChat(it) })
                    (binding as FragmentHomeBinding).runsHomeRv.adapter = adapter
                }else{
                    Log.e(TAG, "filteredRunsByData: COULDN'T GET RUNS: ${posts?.exception.toString()}")
                }
            })

        }
        viewModel.fetchCurrentUser()

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
            showLoading()
            val order = when(position){
                0 -> OrderFilter.NEW
                1 -> OrderFilter.DISTANCE
                2 -> OrderFilter.TIME
                else -> OrderFilter.NEW
            }

            viewModel.orderFilter.value = order
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
        logd("loading...")
        (binding as FragmentHomeBinding).skvLoadingView.visibility = View.VISIBLE
    }

    private fun hideLoading(){
        (binding as FragmentHomeBinding).skvLoadingView.visibility = View.GONE
    }
}

 */