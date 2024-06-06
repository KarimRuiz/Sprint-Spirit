package com.example.sprintspirit.features.admin.ui

import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.FragmentAdminBinding
import com.example.sprintspirit.databinding.FragmentProfileBinding
import com.example.sprintspirit.features.admin.data.Report
import com.example.sprintspirit.features.admin.data.ReportsAdapter
import com.example.sprintspirit.features.chat.ui.ChatFragment
import com.example.sprintspirit.features.chat.ui.ChatViewModel
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator

class AdminFragment : BaseFragment() {

    companion object {
        fun newInstance(): AdminFragment {
            val fragment = AdminFragment()
            return fragment
        }
    }

    private lateinit var adapter: ReportsAdapter
    private lateinit var viewModel: AdminViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = SprintSpiritNavigator(requireContext())
        viewModel = ViewModelProvider(this).get(AdminViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        subscribeUi(binding as FragmentAdminBinding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentAdminBinding) {
        viewModel.reports.observe(viewLifecycleOwner, getReports())

        binding.rvReports.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getReports() = Observer<List<Report>> {
        logd("Reports: ${it}")
        if(it.isNotEmpty()){
            adapter = ReportsAdapter(
                reports = it.toMutableList(),
                onGoToUser = {
                    navigator.navigateToProfileDetail(
                        activity = activity,
                        userId = it
                    )
                },
                onGoToMessage = { chatId, messageId ->
                    navigator.navigateToChat(
                        activity = activity,
                        postId = chatId,
                        title = "",//TODO: REMOVE THIS?
                        highlightMessageId = messageId
                    )
                },
                onGoToPost = {
                    viewModel.postId = it
                    viewModel.post.observe(viewLifecycleOwner){
                        if(it != null){
                            navigator.navigateToPostDetail(
                                activity = activity,
                                post = it
                            )
                        }else{
                            Toast.makeText(
                                activity,
                                "La publicación ya ha sido eliminada.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )
            (binding as FragmentAdminBinding).rvReports.adapter = adapter
            val itemTouchHelper = ItemTouchHelper(createItemTouchHelperCallback())
            itemTouchHelper.attachToRecyclerView((binding as FragmentAdminBinding).rvReports)
        }
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
                val report = adapter.getReportAt(position)
                deleteReport(report)
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

    private fun deleteReport(report: Report) {
        showDeleteConfirmationDialog(){
            viewModel.deleteReport(report)
            adapter.removeReport(report)
        }
    }

    private fun showDeleteConfirmationDialog(onConfirm: () -> Unit){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar reporte")
        builder.setMessage("Estás seguro de que quieres eliminar este reporte?")

        builder.setPositiveButton(ContextCompat.getString(requireContext(), R.string.Confirm)) { dialog, which ->
            onConfirm()
        }
        builder.setNegativeButton(ContextCompat.getString(requireContext(), R.string.Cancel)) { _, _ ->}

        builder.show()
    }

}