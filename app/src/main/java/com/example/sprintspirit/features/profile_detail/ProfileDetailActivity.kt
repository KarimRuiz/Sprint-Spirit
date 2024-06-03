package com.example.sprintspirit.features.profile_detail

import android.os.Bundle
import android.view.View
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.ActivityProfileDetailBinding
import com.example.sprintspirit.features.profile_detail.ui.ProfileDetailFragment
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator

class ProfileDetailActivity : BaseActivity() {

    companion object{
        const val PROFILE_DETAIL_USER_ID = "profile_detail_user_id"
    }

    private lateinit var binding: ActivityProfileDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigator = SprintSpiritNavigator(this)
        binding = ActivityProfileDetailBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val userId = intent.extras?.getString(PROFILE_DETAIL_USER_ID)

        if(savedInstanceState == null){
            val fragment = ProfileDetailFragment.newInstance(userId)
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