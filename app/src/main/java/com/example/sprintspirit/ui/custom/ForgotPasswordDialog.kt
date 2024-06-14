package com.example.sprintspirit.ui.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.TextViewBindingAdapter
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.DialogForgotPasswordBinding
import com.example.sprintspirit.util.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordDialog(
    private val context: Context
) {

    private lateinit var auth: FirebaseAuth

    init {
        auth = FirebaseAuth.getInstance()
    }

    fun show(){

        val builder = MaterialAlertDialogBuilder(context, R.style.CustomDialog)

        val inflater = LayoutInflater.from(context)
        val binding: DialogForgotPasswordBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_forgot_password, null, false)
        builder.setView(binding.root)

        val dialog = builder.create()

        binding.onCancel = View.OnClickListener {
            dialog.dismiss()
        }

        binding.writeText = TextViewBindingAdapter.AfterTextChanged{
            binding.btnSend.isEnabled =
                (binding.edtEmail.text.length >= 4) and Utils.isValidEmail(binding.edtEmail.text)
        }

        binding.onSendEmail = View.OnClickListener {
            val editableEmail = binding.edtEmail.text
            val email = editableEmail.toString()
            if (Utils.isValidEmail(editableEmail)) {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener { task ->
                        Toast.makeText(context, context.getString(R.string.Email_sent), Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener{
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialog.show()
    }

}