package com.example.sprintspirit.features.admin.ui

import android.os.Bundle
import android.view.View
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.ActivityChatBinding
import com.example.sprintspirit.features.chat.ChatActivity
import com.example.sprintspirit.features.chat.ui.ChatFragment
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.example.sprintspirit.databinding.ActivityAdminBinding

class AdminActivity: BaseActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigator = SprintSpiritNavigator(this)
        binding = ActivityAdminBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (savedInstanceState == null) {
            val fragment = AdminFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }


        subscribeUi()
    }

    private fun subscribeUi() {
        binding.toolbar.goBackListener = View.OnClickListener {
            navigator.goBack(this)
        }
    }

}