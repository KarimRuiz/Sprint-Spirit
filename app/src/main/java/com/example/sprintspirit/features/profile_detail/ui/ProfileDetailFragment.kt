package com.example.sprintspirit.features.profile_detail.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.FragmentProfileBinding
import com.example.sprintspirit.databinding.FragmentProfileDetailBinding
import com.example.sprintspirit.features.dashboard.DashboardViewModel
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.dashboard.home.ui.HomeRunAdapter
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.post.ui.PostViewModel
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator

class ProfileDetailFragment : BaseFragment() {

    companion object{
        private const val USER_ID = "profile_detail_user_id"

        fun newInstance(userId:String?): ProfileDetailFragment{
            val fragment = ProfileDetailFragment()
            val args = Bundle()
            args.putString(USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var viewModel: ProfileDetailViewModel
    private lateinit var adapter: HomeRunAdapter

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = SprintSpiritNavigator(requireContext())
        viewModel = ViewModelProvider(this).get(ProfileDetailViewModel::class.java)

        arguments?.let{
            userId = it.getString(USER_ID)
            viewModel.email = userId ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        subscribeUi(binding as FragmentProfileDetailBinding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentProfileDetailBinding) {
        if(userId.isNullOrBlank()) {
            binding.tvProfileNoRuns.visibility = View.VISIBLE
            binding.tvProfileNoRuns.text = getString(R.string.User_not_found)
            return
        }

        viewModel.user.observe(viewLifecycleOwner){
            fillProfileData(it)
        }

        viewModel.posts.observe(viewLifecycleOwner){
            fillPosts(it.posts)
        }
    }

    private fun fillPosts(posts: List<Post>) {
        if(posts.isEmpty()){
            (binding as FragmentProfileDetailBinding).tvProfileNoRuns.visibility = View.VISIBLE
            return
        }

        adapter = HomeRunAdapter(
            postList = posts.sortedBy { it.startTime },
            context = requireContext(),
            onOpenPost = { openPost(it) },
            onOpenChat = { openChat(it) },
            showUser = false)
        (binding as FragmentProfileDetailBinding).profilePostsRv.let {
            it.visibility = View.VISIBLE
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = adapter
        }
    }

    private fun fillProfileData(user: UserResponse?){
        if(user != null){
            (binding as FragmentProfileDetailBinding).tvName.text = user.user?.username ?: user.user?.email ?: ""
            viewModel.profilePicture.observe(viewLifecycleOwner){
                setProfilePicture(it)
            }
        }else{
            logd("Coudln't retrieve ${userId} data")
        }
    }

    private fun setProfilePicture(it: ProfilePictureResponse?) {
        if (it != null) {
            if(it.picture?.uri != null){
                logd("${userId} picture link: ${it.picture?.uri}")
                Glide.with(requireContext())
                    .load(it.picture?.uri)
                    .into((binding as FragmentProfileDetailBinding).ivProfilePicture)
            }
        }else{
            logd("Coudln't retrieve ${userId} picture")
        }
    }

    private fun openPost(post: Post){
        navigator.navigateToPostDetail(activity, post)
    }

    private fun openChat(post: Post) {
        navigator.navigateToChat(activity, post)
    }


}