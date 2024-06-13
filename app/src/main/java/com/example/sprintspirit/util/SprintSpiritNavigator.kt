package com.example.sprintspirit.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.features.admin.ui.AdminActivity
import com.example.sprintspirit.features.admin.ui.AdminFragment
import com.example.sprintspirit.features.chat.ChatActivity
import com.example.sprintspirit.features.dashboard.DashboardActivity
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.post.ui.PostActivity
import com.example.sprintspirit.features.post_detail.PostDetailActivity
import com.example.sprintspirit.features.profile_detail.ProfileDetailActivity
import com.example.sprintspirit.features.route_detail.RouteDetailActivity
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.ui.RunActivity
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

    fun navigateToRecordRoute(
        activity: FragmentActivity?,
        preserveStack: Boolean? = true
    ){
        navigateTo(activity, Intent(activity, RunActivity::class.java), preserveStack)
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

    fun navigateToPostDetail(
        activity: FragmentActivity?,
        post: Post,
        preserveStack: Boolean? = true
    ){
        PostDetailActivity.post = post
        navigateTo(activity,
            Intent(activity, PostDetailActivity::class.java),
            preserveStack)
    }

    fun navigateToRunDetail(
        activity: FragmentActivity?,
        run: RunData,
        preserveStack: Boolean? = true
    ){
        RouteDetailActivity.route = run
        navigateTo(activity,
            Intent(activity, RouteDetailActivity::class.java),
            preserveStack)
    }

    fun navigateToProfileDetail(
        activity: FragmentActivity?,
        userId: String,
        preserveStack: Boolean? = true
    ){
        if(Preferences(context).email == userId){
            val intent = Intent(activity, DashboardActivity::class.java)
            intent.putExtra(DashboardActivity.NAVIGATE_TO, DashboardActivity.PROFILE_SECTION)
            navigateTo(activity, intent, preserveStack)
        }else{
            val intent = Intent(activity, ProfileDetailActivity::class.java)
            intent.putExtra(ProfileDetailActivity.PROFILE_DETAIL_USER_ID, userId)
            navigateTo(activity,
                intent,
                preserveStack)
        }
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

    fun navigateToChat(
        activity: FragmentActivity?,
        postId: String,
        title: String,
        highlightMessageId: String,
        preserveStack: Boolean? = true
    ){
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra(ChatActivity.CHAT_POST_ID, postId)
        intent.putExtra(ChatActivity.CHAT_POST_TITLE, title)
        intent.putExtra(ChatActivity.CHAT_HIGHLIGHT_MESSAGE, highlightMessageId)
        navigateTo(activity,
            intent,
            preserveStack
        )
    }

    fun navigateToAdminPanel(
        activity: FragmentActivity?,
        preserveStack: Boolean? = true
    ){
        navigateTo(activity,
            Intent(activity, AdminActivity::class.java),
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