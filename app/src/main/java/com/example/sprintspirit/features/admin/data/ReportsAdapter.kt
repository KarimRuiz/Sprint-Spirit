package com.example.sprintspirit.features.admin.data

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sprintspirit.databinding.CardReportBinding
import com.example.sprintspirit.util.SprintSpiritNavigator

class ReportsAdapter(
    private val reports: MutableList<Report>,
    private val onGoToUser: (String) -> Unit,
    private val onGoToMessage: (String, String) -> Unit,
    private val onGoToPost: (String) -> Unit
) : RecyclerView.Adapter<ReportsAdapter.ReportHolder>() {
    class ReportHolder(
        private val binding: CardReportBinding,
        var onGoToUser: (String) -> Unit,
        var onGoToMessage: (String, String) -> Unit,
        var onGoToPost: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(rep: Report) {
            when(rep){
                is UserReport -> {
                    binding.title.text = "Email: ${rep.itemId}"
                    binding.reason.text = if(rep.reason.isNotBlank()){
                        "Motivo: ${rep.reason}"
                    }else{
                        "No se ha especificado motivo."
                    }
                    binding.reporter.text = "Reporte por: ${rep.reporter}"
                    binding.btnGoToReference.text = "Ir al usuario"
                    binding.goToReference = View.OnClickListener {
                        onGoToUser(rep.itemId)
                    }
                }
                is MessageReport -> {
                    binding.title.text = "Chat id: ${rep.itemId}"
                    binding.reason.text = if(rep.reason.isNotBlank()){
                        "Motivo: ${rep.reason}"
                    }else{
                        "No se ha especificado motivo."
                    }
                    binding.reporter.text = "Reporte por: ${rep.reporter}"
                    binding.btnGoToReference.text = "Ir al mensaje"
                    binding.goToReference = View.OnClickListener {
                        onGoToMessage(rep.itemId, rep.messageId)
                    }
                }
                is PostReport -> {
                    binding.title.text = "Post id: ${rep.itemId}"
                    binding.reason.text = if(rep.reason.isNotBlank()){
                        "Motivo: ${rep.reason}"
                    }else{
                        "No se ha especificado motivo."
                    }
                    binding.reporter.text = "Reporte por: ${rep.reporter}"
                    binding.btnGoToReference.text = "Ir al post"
                    binding.goToReference = View.OnClickListener {
                        onGoToPost(rep.itemId)
                    }
                }
                else -> {}
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CardReportBinding.inflate(inflater, parent, false)
        return ReportHolder(binding, onGoToUser, onGoToMessage, onGoToPost)
    }

    override fun getItemCount() = reports.size

    override fun onBindViewHolder(holder: ReportHolder, position: Int) {
        val item = reports[position]
        holder.bind(item)
    }

    fun getReportAt(position: Int): Report = reports[position]
    fun removeReport(report: Report) {
        val position = reports.indexOf(report)
        Log.d("ReportsAdapter", "$position")
        if(position != -1){
            reports.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun removeReport(position: Int) {
        if(position != -1){
            reports.removeAt(position)
            notifyItemChanged(position)
        }
    }
}