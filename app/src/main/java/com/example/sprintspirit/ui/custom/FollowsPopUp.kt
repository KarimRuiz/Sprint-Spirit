package com.example.sprintspirit.ui.custom

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sprintspirit.MainActivity
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.CardFollowerBinding
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.features.signin.data.UserFollow
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object popUpFollows{
    fun showFollows(context: Context,
                    user: User, title:
                    String = "",
                    onClickFollow: (String) -> Unit){

        //val builder = AlertDialog.Builder(context, R.style.CustomDialog)
        val builder = MaterialAlertDialogBuilder(context, R.style.CustomDialog)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_follow_list, null)
        builder.setView(dialogView)

        val recyclerView: RecyclerView = dialogView.findViewById(R.id.rv_follow_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = FollowsAdapter(context, user, onClickFollow)
        recyclerView.adapter = adapter

        builder.setTitle(title)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()
    }

    class FollowsAdapter(
        private val context: Context,
        private val user: User,
        val onClickFollow: (String) -> Unit
    ) : RecyclerView.Adapter<FollowsAdapter.ViewHolder>() {

        private val follows = user.getFollowingList()
        private val followsList = follows.toList()

            inner class ViewHolder(val binding: CardFollowerBinding
            ) : RecyclerView.ViewHolder(binding.root){
                fun bind(pair: Pair<String, UserFollow>, onClickFollow: (String) -> Unit) {
                    binding.followUsername.text = pair.second.username
                    binding.cvFollow.setOnClickListener {
                        onClickFollow(pair.first)
                    }
                }

            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = CardFollowerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return followsList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(followsList[position], onClickFollow)
        }

    }
}