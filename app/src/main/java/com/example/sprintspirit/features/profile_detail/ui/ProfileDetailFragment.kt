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
import com.example.sprintspirit.databinding.FragmentProfileDetailBinding
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.dashboard.home.ui.HomeRunAdapter
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.ui.custom.popUpFollows
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
    private var currentUser: User? = null
    private var isFollowed: Boolean = false

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

    override fun onResume() {
        super.onResume()
        getUser()
    }

    private fun subscribeUi(binding: FragmentProfileDetailBinding) {
        if(userId.isNullOrBlank()) {
            binding.tvProfileNoRuns.visibility = View.VISIBLE
            binding.tvProfileNoRuns.text = getString(R.string.User_not_found)
            return
        }

        viewModel.posts.observe(viewLifecycleOwner){
            fillPosts(it.posts)
        }
    }

    private fun getUser() {
        viewModel.user.observe(viewLifecycleOwner){
            logd(it.toString())
            fillProfileData(it)
        }

        viewModel.currentUser.observe(viewLifecycleOwner){
            currentUser = it.user
            if(currentUser != null){
                isFollowed = currentUser!!.follows(userId!!)
                if(isFollowed){
                    (binding as FragmentProfileDetailBinding).btnFollow.text = getString(R.string.Unfollow)
                }else{
                    (binding as FragmentProfileDetailBinding).btnFollow.text = getString(R.string.Follow)
                }
                setUpCurrentUserConfig(binding as FragmentProfileDetailBinding)
            }else{
                (binding as FragmentProfileDetailBinding).btnFollow.visibility = View.GONE
            }
        }
    }

    private fun setUpCurrentUserConfig(binding: FragmentProfileDetailBinding) {
        binding.onClickFollow = onFollow()
    }

    private fun onFollow() = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if(!isFollowed){
                logd("Following user ${userId!!}")
                viewModel.follow(sharedPreferences.email ?: "", userId!!)
                (binding as FragmentProfileDetailBinding).btnFollow.text = getString(R.string.Unfollow)
            }else{
                logd("Unfollowing user ${userId!!}")
                viewModel.unFollow(sharedPreferences.email ?: "", userId!!)
                (binding as FragmentProfileDetailBinding).btnFollow.text = getString(R.string.Follow)
            }
            isFollowed = !isFollowed
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
            (binding as FragmentProfileDetailBinding).tvName.text = user.user?.username ?: ""
            viewModel.profilePicture.observe(viewLifecycleOwner){
                setProfilePicture(it)
            }
            setFollowers(user.user)
        }else{
            logd("Coudln't retrieve ${userId} data")
        }
    }

    private fun setFollowers(user: User?) {
        if(user != null && user.following.isNotEmpty()){
            val following = user.following
            (binding as FragmentProfileDetailBinding).tvFollowing.let{
                it.text = getString(R.string.User_follow, following.size)
                it.setOnClickListener {
                    popUpFollows.showFollows(requireContext(), user) { email ->
                        navigator.navigateToProfileDetail(
                            activity = activity,
                            userId = email
                        )
                    }
                }
            }
        }else{
            (binding as FragmentProfileDetailBinding).tvFollowing.let{
                it.text = getString(R.string.User_doesnt_follow_anyone)
            }
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