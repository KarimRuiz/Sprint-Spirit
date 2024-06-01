package com.example.sprintspirit.features.dashboard.profile

import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.databinding.FragmentProfileBinding
import com.example.sprintspirit.features.dashboard.DashboardViewModel
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.dashboard.profile.ui.ProfileRunAdapter
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunsResponse
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator
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

        navigator = SprintSpiritNavigator(requireContext())
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
            adapter = ProfileRunAdapter(runs.runs!!.toMutableList(),
                deleteCallback = {
                    deleteRun(it, adapter.getPosOfRun(it))
                },
                postCallback = {
                    navigator.navigateToPostRun(activity, it, true)
                }
            )
            (binding as FragmentProfileBinding).runProfileRv.adapter = adapter
            if(runs.runs!!.isEmpty()){
                (binding as FragmentProfileBinding).tvProfileNoRuns.visibility = View.VISIBLE
            }else{
                val itemTouchHelper = ItemTouchHelper(createItemTouchHelperCallback())
                itemTouchHelper.attachToRecyclerView((binding as FragmentProfileBinding).runProfileRv)
            }
        }else{
            Log.e(TAG, "fillRuns: COULDN'T GET RUNS")
        }
    }

    private fun deleteRun(run: RunData, position: Int){
        showDeleteConfirmationDialog(onConfirm = {
            viewModel.deleteRun(run)
            adapter.removeRun(run)
        }, onCancel = {
            adapter.notifyItemChanged(position)
        })
    }

    private fun showDeleteConfirmationDialog(onConfirm: () -> Unit, onCancel: () -> Unit){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(ContextCompat.getString(requireContext(), R.string.Confirmation))
        builder.setMessage(ContextCompat.getString(requireContext(), R.string.Are_you_sure_delete_run))

        builder.setPositiveButton(ContextCompat.getString(requireContext(), R.string.Confirm)) { dialog, which ->
            onConfirm()
        }
        builder.setNegativeButton(ContextCompat.getString(requireContext(), R.string.Cancel)) { dialog, which ->
            onCancel()
        }

        builder.show()
    }

    private fun createItemTouchHelperCallback(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            private val deleteIcon: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash)?.apply {
                setTint(Color.RED)
            }
            private val intrinsicWidth: Int = deleteIcon?.intrinsicWidth ?: 0
            private val intrinsicHeight: Int = deleteIcon?.intrinsicHeight ?: 0
            private val scaleFactor: Float = 2.5f

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val run = adapter.getRunAt(position)
                deleteRun(run, position)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top

                val scaledWidth = (intrinsicWidth * scaleFactor).toInt()
                val scaledHeight = (intrinsicHeight * scaleFactor).toInt()

                val iconTop = itemView.top + (itemHeight - scaledHeight) / 2
                val iconMargin = (itemHeight - scaledHeight) / 2
                val iconLeft: Int
                val iconRight: Int

                if (dX > 0) {
                    iconLeft = itemView.left + iconMargin
                    iconRight = itemView.left + iconMargin + scaledWidth
                } else if (dX < 0) {
                    iconLeft = itemView.right - iconMargin - scaledWidth
                    iconRight = itemView.right - iconMargin
                } else {
                    iconLeft = itemView.right - iconMargin - scaledWidth
                    iconRight = itemView.right - iconMargin
                }

                val iconBottom = iconTop + scaledHeight

                deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon?.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
    }



}