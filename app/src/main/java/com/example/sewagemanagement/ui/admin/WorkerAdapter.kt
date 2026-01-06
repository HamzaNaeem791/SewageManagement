package com.example.sewagemanagement.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sewagemanagement.data.model.User
import com.example.sewagemanagement.databinding.ItemWorkerBinding

class WorkerAdapter(
    private val onDeleteClick: (User) -> Unit
) : ListAdapter<User, WorkerAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.userId == newItem.userId
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }

    inner class VH(private val binding: ItemWorkerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvName.text = user.name
            binding.tvEmail.text = user.email
            binding.btnDelete.setOnClickListener { onDeleteClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemWorkerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
