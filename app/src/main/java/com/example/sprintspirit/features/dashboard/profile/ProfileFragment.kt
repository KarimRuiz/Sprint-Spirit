package com.example.sprintspirit.features.dashboard.profile

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sprintspirit.databinding.FragmentProfileBinding
import com.example.sprintspirit.features.dashboard.DashboardViewModel
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.dashboard.profile.ui.ProfileRunAdapter
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.features.run.data.RunsResponse
import com.example.sprintspirit.ui.BaseFragment
import com.google.firebase.firestore.GeoPoint
import java.io.FileNotFoundException
import java.util.Date

class ProfileFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var adapter: ProfileRunAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = DashboardViewModel()
        getCurrentUser()

        getRuns()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        subscribeUi(binding as FragmentProfileBinding)
        fillProfilePicture(binding as FragmentProfileBinding)

        return binding.root
    }

    private fun getCurrentUser() {
        viewModel.currentUserLiveData.observe(this) {
            fillProfileData(it)
        }
    }

    private fun getRuns() {
        viewModel.runs.observe(this){
            fillRuns(it)
        }
    }

    private fun subscribeUi(binding: FragmentProfileBinding) {
        binding.runProfileRv.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun fillProfileData(user: UserResponse?) {
        if(user != null){
            (binding as FragmentProfileBinding).tvName.text = user.user?.username ?: user.user?.email ?: ""
        }else{
            Log.e(TAG, "fillProfileData: COULDN'T GET USER'S DATA")
        }
    }

    private fun fillRuns(runs: RunsResponse?) {
        if((runs != null) and (runs!!.runs != null)){
            adapter = ProfileRunAdapter(runs.runs!!)
            (binding as FragmentProfileBinding).runProfileRv.adapter = adapter
        }else{
            Log.e(TAG, "fillRuns: COULDN'T GET RUNS")
        }
    }

    private fun fillProfilePicture(fragmentProfileBinding: FragmentProfileBinding) {
        if(viewModel.getPictureUri() != null){
            val userPicture = getUserPicture(viewModel.getPictureUri()!!)
            fragmentProfileBinding.ivProfilePicture.setImageDrawable(userPicture)
        }
    }

    fun getUserPicture(uri: Uri): Drawable? {
        try {
            return Drawable.createFromStream(context?.contentResolver?.openInputStream(uri), null)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }
    }


}