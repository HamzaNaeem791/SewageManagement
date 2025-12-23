package com.example.sewagemanagement.ui.complaint

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sewagemanagement.data.model.Complaint
import com.example.sewagemanagement.databinding.ItemComplaintBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ComplaintAdapter : RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder>() {

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
        holder.bind(complaints[position])
    }

    override fun getItemCount() = complaints.size

    class ComplaintViewHolder(private val binding: ItemComplaintBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(complaint: Complaint) {
            binding.tvIssueType.text = complaint.issueType
            binding.tvStatus.text = complaint.status
            binding.tvDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(complaint.timestamp)
            
            // Set Color based on status
            // Note: In a real app we'd use ContextCompat.getColor logic here
        }
    }
}
