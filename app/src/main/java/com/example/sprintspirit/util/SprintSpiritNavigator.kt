package com.example.sprintspirit.util

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.example.sprintspirit.features.chat.ChatActivity
import com.example.sprintspirit.features.dashboard.DashboardActivity
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.post.ui.PostActivity
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.settings.SettingsActivity
import com.example.sprintspirit.features.signin.ui.signin.SignInActivity

class SprintSpiritNavigator(val context: Context) {

    fun navigateToHome(
        activity: FragmentActivity?,
        preserveStack: Boolean? = true
    ){
        navigateTo(activity, Intent(activity, DashboardActivity::class.java), preserveStack)
    }

    fun navigateToLogIn(
        activity: FragmentActivity?,
        preserveStack: Boolean? = true
    ){
        navigateTo(activity, Intent(activity, SignInActivity::class.java), preserveStack)
    }

    fun navigateToSettings(
        activity: FragmentActivity?,
        preserveStack: Boolean? = true
    ){
        navigateTo(activity, Intent(activity, SettingsActivity::class.java), preserveStack)
    }

    fun navigateToPostRun(
        activity: FragmentActivity?,
        run: RunData,
        preserveStack: Boolean? = true
    ){
        PostActivity.runData = run
        navigateTo(activity,
            Intent(activity, PostActivity::class.java),
            preserveStack)
    }

    fun navigateToChat(
        activity: FragmentActivity?,
        post: Post,
        preserveStack: Boolean? = true
    ){
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra(ChatActivity.CHAT_POST_ID, post.id)
        intent.putExtra(ChatActivity.CHAT_POST_TITLE, post.title)
        navigateTo(activity,
            intent,
            preserveStack
        )
    }

    fun navigateToChat(
        activity: FragmentActivity?,
        postId: String,
        title: String,
        preserveStack: Boolean? = true
    ){
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra(ChatActivity.CHAT_POST_ID, postId)
        intent.putExtra(ChatActivity.CHAT_POST_TITLE, title)
        navigateTo(activity,
            intent,
            preserveStack
        )
    }

    fun goBack(activty: FragmentActivity?){
        activty?.onBackPressedDispatcher?.onBackPressed()
    }

    private fun navigateTo(
        activity: FragmentActivity?,
        intent: Intent,
        preserveStack: Boolean? = true
    ){
        if(preserveStack == true){
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }else{
            activity?.finish()
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

}