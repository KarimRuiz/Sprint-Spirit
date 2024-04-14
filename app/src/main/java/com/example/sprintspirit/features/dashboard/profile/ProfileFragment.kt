package com.example.sprintspirit.features.dashboard.profile

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.FragmentProfileBinding
import com.example.sprintspirit.features.dashboard.DashboardViewModel
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.dashboard.profile.ui.ProfileRunAdapter
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.features.run.data.RunsResponse
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.Utils.isInternetAvailable
import com.google.firebase.firestore.GeoPoint
import java.io.FileNotFoundException
import java.util.Date

class ProfileFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var adapter: ProfileRunAdapter

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            uploadProfileImage(uri)
        }
    }

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

    private fun getProfilePicture(){
        viewModel.profilePicture.observe(this){
            setProfilePicture(it)
        }
    }

    private fun setProfilePicture(it: ProfilePictureResponse?) {
        if (it != null) {
            if(it.picture?.uri != null){
                Glide.with(requireContext())
                    .load(it.picture?.uri)
                    .into((binding as FragmentProfileBinding).ivProfilePicture)
            }
        }else{
            Log.d("GetProfilePicture", "PIC NULL!")
        }
    }

    private fun subscribeUi(binding: FragmentProfileBinding) {
        binding.runProfileRv.layoutManager = LinearLayoutManager(requireContext())

        binding.ivProfilePicture.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun uploadProfileImage(uri: Uri) {
        viewModel.uploadProfilePicture(uri, viewModel.userId!!){success ->
            if(success){
                activity?.runOnUiThread {
                    (binding as FragmentProfileBinding).ivProfilePicture.setImageURI(uri)
                }
            }else{
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), getString(R.string.Failed_to_upload_picture), Toast.LENGTH_SHORT).show()
                }
            }
        }
        if(!isInternetAvailable(requireContext())){
            Toast.makeText(requireContext(), getString(R.string.Internet_will_be_uploaded), Toast.LENGTH_SHORT).show()
        }
    }

    private fun fillProfileData(user: UserResponse?) {
        if(user != null){
            (binding as FragmentProfileBinding).tvName.text = user.user?.username ?: user.user?.email ?: ""
            viewModel.userId = user.user?.email
            getProfilePicture()
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


}