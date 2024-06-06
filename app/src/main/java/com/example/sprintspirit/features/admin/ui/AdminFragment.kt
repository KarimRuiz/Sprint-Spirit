package com.example.sprintspirit.features.admin.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.sprintspirit.databinding.FragmentAdminBinding
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
                reports = it,
                onGoToUser = {
                    navigator.navigateToProfileDetail(
                        activity = activity,
                        userId = it
                    )
                },
                onGoToMessage = { chatId, messageId ->

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
        }
    }

}