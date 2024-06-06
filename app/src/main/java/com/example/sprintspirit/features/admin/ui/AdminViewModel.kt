package com.example.sprintspirit.features.admin.ui

import androidx.lifecycle.liveData
import com.example.sprintspirit.features.admin.data.AdminRepository
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers

class AdminViewModel(
    private val adminRepository: AdminRepository = AdminRepository(),
) : BaseViewModel() {

    var postId: String = ""

    val reports = liveData(Dispatchers.IO){
        emit(adminRepository.getReports())
    }

    val post = liveData(Dispatchers.IO){
        emit(adminRepository.getPost(postId))
    }

}