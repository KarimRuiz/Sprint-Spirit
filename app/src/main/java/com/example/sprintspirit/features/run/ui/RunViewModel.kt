package com.example.sprintspirit.features.run.ui

import android.content.Context
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.ui.BaseViewModel

class RunViewModel() : BaseViewModel() {

    private val dbManager: DBManager = DBManager.getCurrentDBManager()

    val user = dbManager.getAuthUser()

}