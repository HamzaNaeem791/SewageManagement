package com.example.sewagemanagement.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sewagemanagement.SewageApplication
import com.example.sewagemanagement.databinding.ActivityManageComplaintsBinding
import com.example.sewagemanagement.ui.ViewModelFactory
import com.example.sewagemanagement.ui.complaint.ComplaintAdapter
import com.example.sewagemanagement.ui.complaint.ComplaintViewModel
import com.example.sewagemanagement.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ManageComplaintsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageComplaintsBinding

    private val complaintViewModel: ComplaintViewModel by viewModels {
        ViewModelFactory(
            complaintRepository = (application as SewageApplication).container.complaintRepository
        )
    }

    private val adminViewModel: AdminViewModel by viewModels {
        ViewModelFactory(
            authRepository = (application as SewageApplication).container.authRepository
        )
    }

    private var availableWorkers: List<com.example.sewagemanagement.data.model.User> = emptyList()

    private val adapter = ComplaintAdapter(
        onItemClick = { complaint ->
            showActionDialog(complaint)
        },
        onMapClick = { complaint ->
            val uri = android.net.Uri.parse(
                "geo:${complaint.location?.latitude},${complaint.location?.longitude}?q=${complaint.location?.latitude},${complaint.location?.longitude}(Issue Location)"
            )
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageComplaintsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvComplaints.layoutManager = LinearLayoutManager(this)
        binding.rvComplaints.adapter = adapter

        complaintViewModel.fetchAllComplaints()
        adminViewModel.fetchWorkers()

        observeComplaints()
        observeWorkers()
    }

    private fun observeComplaints() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                complaintViewModel.complaints.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.progressBar.visibility = android.view.View.VISIBLE
                        is Resource.Success -> {
                            binding.progressBar.visibility = android.view.View.GONE
                            adapter.submitList(resource.data ?: emptyList())
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = android.view.View.GONE
                            Toast.makeText(this@ManageComplaintsActivity, resource.message ?: "Error loading complaints", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun observeWorkers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminViewModel.workers.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            availableWorkers = (resource.data ?: emptyList()).filter { !it.disabled }
                        }
                        is Resource.Error -> {
                            Toast.makeText(this@ManageComplaintsActivity, resource.message ?: "Error loading workers", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            // no-op
                        }
                    }
                }
            }
        }
    }

    private fun showActionDialog(complaint: com.example.sewagemanagement.data.model.Complaint) {
        val options = arrayOf("Update Status", "Assign to Worker")
        MaterialAlertDialogBuilder(this)
            .setTitle("Manage Complaint")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showStatusUpdateDialog(complaint)
                    1 -> showAssignWorkerDialog(complaint)
                }
            }
            .show()
    }

    private fun showStatusUpdateDialog(complaint: com.example.sewagemanagement.data.model.Complaint) {
        val statuses = arrayOf("Pending", "In Progress", "Resolved")
        MaterialAlertDialogBuilder(this)
            .setTitle("Update Status")
            .setItems(statuses) { _, which ->
                val newStatus = statuses[which]
                complaintViewModel.updateStatus(complaint.complaintId, newStatus)
                Toast.makeText(this, "Updating to $newStatus...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showAssignWorkerDialog(complaint: com.example.sewagemanagement.data.model.Complaint) {
        if (availableWorkers.isEmpty()) {
            Toast.makeText(this, "No workers found.", Toast.LENGTH_SHORT).show()
            return
        }

        val workerNames = availableWorkers.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Assign Worker")
            .setItems(workerNames) { _, which ->
                val selectedWorker = availableWorkers[which]
                complaintViewModel.assignJobToWorker(complaint.complaintId, selectedWorker.userId)
                Toast.makeText(this, "Assigned to ${selectedWorker.name}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
