package com.example.sewagemanagement.ui.complaint

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sewagemanagement.data.model.Complaint
import com.example.sewagemanagement.databinding.ItemComplaintBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ComplaintAdapter(
    private val onItemClick: (Complaint) -> Unit
) : RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder>() {

    private var complaints: List<Complaint> = emptyList()

    fun submitList(list: List<Complaint>) {
        complaints = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val binding = ItemComplaintBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ComplaintViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        holder.bind(complaints[position], onItemClick)
    }

    override fun getItemCount() = complaints.size

    class ComplaintViewHolder(private val binding: ItemComplaintBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(complaint: Complaint, onItemClick: (Complaint) -> Unit) {
            binding.tvIssueType.text = complaint.issueType
            binding.tvStatus.text = complaint.status
            binding.tvDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(complaint.timestamp)

            // Dynamic Status Coloring
            val context = binding.root.context
            when (complaint.status) {
                "Pending" -> binding.tvStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                "In Progress" -> binding.tvStatus.setChipBackgroundColorResource(android.R.color.holo_blue_light)
                "Resolved" -> binding.tvStatus.setChipBackgroundColorResource(android.R.color.holo_green_light)
                else -> binding.tvStatus.setChipBackgroundColorResource(android.R.color.darker_gray)
            }

            binding.root.setOnClickListener { onItemClick(complaint) }
        }
    }
}
