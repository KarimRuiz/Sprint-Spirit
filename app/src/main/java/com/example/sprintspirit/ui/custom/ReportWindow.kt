package com.example.sprintspirit.ui.custom

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.databinding.DialogReportBinding
import com.example.sprintspirit.features.admin.data.MessageReport
import com.example.sprintspirit.features.admin.data.PostReport
import com.example.sprintspirit.features.admin.data.Report
import com.example.sprintspirit.features.admin.data.UserReport
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReportDialog(
    private val context: Context,
    val type: String,
    val id: String,
    val messageId: String = ""
) {
    fun showDialog() {
        val builder = MaterialAlertDialogBuilder(context, R.style.CustomDialog)
        builder.setTitle(context.getString(R.string.Upload_report))

        val inflater = LayoutInflater.from(context)
        val binding: DialogReportBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_report, null, false)
        builder.setView(binding.root)

        val dialog = builder.create()

        binding.onCancel = View.OnClickListener {
            dialog.dismiss()
        }

        val reporter = Preferences(context).email ?: ""
        binding.onReport = View.OnClickListener {
            val db = DBManager.getCurrentDBManager()
            var report: Report
            when(type){
                "message" -> {
                    report = MessageReport(
                        type = type,
                        reporter = reporter,
                        reason = binding.edtContent.text.toString(),
                        itemId = id,
                        messageId = messageId)
                }
                "user" -> {
                    report = UserReport(
                        type = type,
                        reporter = reporter,
                        reason = binding.edtContent.text.toString(),
                        itemId = id)
                }
                else -> {
                    report = PostReport(
                        type = type,
                        reporter = reporter,
                        reason = binding.edtContent.text.toString(),
                        itemId = id)
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                db.submitReport(report)
                Toast.makeText(context, "Gracias por subir el reporte. Nuestros moderadores se encargarán de tomar acciones.", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }

        }

        dialog.show()
    }
}