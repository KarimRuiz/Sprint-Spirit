package com.example.sprintspirit.features.dashboard.profile

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
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.databinding.FragmentProfileBinding
import com.example.sprintspirit.features.dashboard.DashboardViewModel
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.dashboard.profile.ui.ProfileRunAdapter
import com.example.sprintspirit.features.run.data.RunsResponse
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.Utils.isInternetAvailable

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
        viewModel.email = Preferences(requireContext()).email
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
            logd("Picture response is not null")
            if(it.picture?.uri != null){
                logd("picture is not null: ${it.picture?.uri}")
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
        viewModel.uploadProfilePicture(uri, viewModel.email!!){ success ->
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
            getProfilePicture()
        }else{
            Log.e(TAG, "fillProfileData: COULDN'T GET USER'S DATA")
        }
    }

    private fun fillRuns(runs: RunsResponse?) {
        if((runs != null) and (runs!!.runs != null)){
            adapter = ProfileRunAdapter(runs.runs!!,
                deleteCallback = {
                viewModel.deleteRun(it)
                },
                postCallback = {
                    navigator.navigateToPostRun(activity, true)
                }
            )
            (binding as FragmentProfileBinding).runProfileRv.adapter = adapter
            if(runs.runs!!.isEmpty())
                    (binding as FragmentProfileBinding).tvProfileNoRuns.visibility = View.VISIBLE
        }else{
            Log.e(TAG, "fillRuns: COULDN'T GET RUNS")
        }
    }


}