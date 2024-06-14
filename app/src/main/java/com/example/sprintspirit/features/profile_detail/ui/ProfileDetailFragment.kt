package com.example.sprintspirit.features.profile_detail.ui

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.DialogImageViewerBinding
import com.example.sprintspirit.databinding.FragmentPostDetailBinding
import com.example.sprintspirit.databinding.FragmentProfileBinding
import com.example.sprintspirit.databinding.FragmentProfileDetailBinding
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.dashboard.home.ui.HomeRunAdapter
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.ui.custom.ReportDialog
import com.example.sprintspirit.ui.custom.popUpFollows
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.github.ybq.android.spinkit.style.ChasingDots

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
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireInternet(compulsory = true)
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
        val loadingAnim = ChasingDots()
        binding.skvLoadingView.setIndeterminateDrawable(loadingAnim)
        showLoading()

        if(userId.isNullOrBlank()) {
            binding.tvProfileNoRuns.visibility = View.VISIBLE
            binding.tvProfileNoRuns.text = getString(R.string.User_not_found)
            return
        }

        viewModel.posts.observe(viewLifecycleOwner){
            fillPosts(it.posts)
            hideLoading()
        }

        if(sharedPreferences.isAdmin){
            binding.btnReport.visibility = View.GONE
            binding.btnBan.visibility = View.VISIBLE
        }

        binding.onReport = onReportUser()
        binding.onBan = onBanUser()
    }

    private fun onBanUser() = object : View.OnClickListener {
        override fun onClick(v: View?) {
            logd("Banning/unbanning user...")
            val isBanned = user?.isBanned ?: false
            if(isBanned){
                viewModel.unBanUser(userId)
            }else{
                viewModel.banUser(userId)
            }
            if(user != null){
                user!!.isBanned = !isBanned
                if(isBanned){
                    (binding as FragmentProfileDetailBinding).btnBan.text = "Banear"
                }else{
                    (binding as FragmentProfileDetailBinding).btnBan.text = "Desbanear"
                }
            }
        }
    }

    private fun getUser() {
        viewModel.user.observe(viewLifecycleOwner){
            logd("USER: ${it.user}")
            if(it.user?.isBanned ?: false){
                if(!sharedPreferences.isAdmin) showBanned()
                (binding as FragmentProfileDetailBinding).btnBan.text = "Desbanear"
            }
            logd(it.toString())
            user = it.user
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

    private fun showBanned() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage("Este usuario está baneado. No es posible ver su perfil")
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            navigator.navigateToHome(activity = activity)
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
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
        if(user != null && user.followers.isNotEmpty()){
            val followers = user.followers
            (binding as FragmentProfileDetailBinding).tvFollowers.let{
                it.text = getString(R.string.Followers, followers.size)
                it.setOnClickListener {
                    popUpFollows.showFollowers(requireContext(), user) { email ->
                        navigator.navigateToProfileDetail(
                            activity = activity,
                            userId = email
                        )
                    }
                }
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

                (binding as FragmentProfileDetailBinding).onImageClick = View.OnClickListener {view ->
                    showImageDialog(it.picture?.uri!!)
                }
            }
        }else{
            logd("Coudln't retrieve ${userId} picture")
        }
    }

    private fun showImageDialog(image: Uri) {
        val dialogBinding = DialogImageViewerBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)

        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        Glide.with(requireContext())
            .load(image)
            .into(dialogBinding.ivDialogImageViewer)

        dialogBinding.root.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun onReportUser() = object : View.OnClickListener {
        override fun onClick(v: View?) {
            ReportDialog(
                context = requireContext(),
                type = "user",
                id = userId ?: ""
            ).showDialog()
        }
    }

    private fun openPost(post: Post){
        navigator.navigateToPostDetail(activity, post)
    }

    private fun openChat(post: Post) {
        navigator.navigateToChat(activity, post)
    }

    fun showLoading(){
        (binding as FragmentProfileDetailBinding).skvLoadingView.visibility = View.VISIBLE
    }

    fun hideLoading(){
        (binding as FragmentProfileDetailBinding).skvLoadingView.visibility = View.GONE
    }

}