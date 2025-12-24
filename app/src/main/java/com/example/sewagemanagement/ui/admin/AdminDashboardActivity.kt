package com.example.sewagemanagement.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sewagemanagement.databinding.ActivityAdminDashboardBinding
import com.example.sewagemanagement.ui.ViewModelFactory
import com.example.sewagemanagement.ui.auth.LoginActivity
import com.example.sewagemanagement.ui.complaint.ComplaintAdapter
import com.example.sewagemanagement.ui.complaint.ComplaintViewModel
import com.example.sewagemanagement.utils.Resource
import com.example.sewagemanagement.SewageApplication
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val viewModel: ComplaintViewModel by viewModels {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        
        viewModel.fetchAllComplaints()
        adminViewModel.fetchWorkers()
    }

    private fun setupUI() {
        val adapter = ComplaintAdapter(
            onItemClick = { complaint ->
                showActionDialog(complaint)
            },
            onMapClick = { complaint ->
                val gmmIntentUri = android.net.Uri.parse("geo:${complaint.location?.latitude},${complaint.location?.longitude}?q=${complaint.location?.latitude},${complaint.location?.longitude}(Issue Location)")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    // Fallback to any app that can handle geo intents
                    startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                }
            }
        )
        binding.rvAllComplaints.layoutManager = LinearLayoutManager(this)
        binding.rvAllComplaints.adapter = adapter

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showActionDialog(complaint: com.example.sewagemanagement.data.model.Complaint) {
        val options = arrayOf("Update Status", "Assign to Worker")
        AlertDialog.Builder(this)
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
        AlertDialog.Builder(this)
            .setTitle("Update Status")
            .setItems(statuses) { _, which ->
                val newStatus = statuses[which]
                val id = complaint.timestamp.time.toString() 
                viewModel.updateStatus(id, newStatus)
                Toast.makeText(this, "Updating to $newStatus...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showAssignWorkerDialog(complaint: com.example.sewagemanagement.data.model.Complaint) {
        if (availableWorkers.isEmpty()) {
            Toast.makeText(this, "No workers found. Register a worker with @worker.com first.", Toast.LENGTH_LONG).show()
            return
        }

        val workerNames = availableWorkers.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Assign Worker")
            .setItems(workerNames) { _, which ->
                val selectedWorker = availableWorkers[which]
                val id = complaint.timestamp.time.toString()
                viewModel.assignJobToWorker(id, selectedWorker.userId)
                Toast.makeText(this, "Assigned to ${selectedWorker.name}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.complaints.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            val complaints = resource.data ?: emptyList()
                            (binding.rvAllComplaints.adapter as ComplaintAdapter).submitList(complaints)
                            updateStatistics(complaints)
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@AdminDashboardActivity, resource.message ?: "Error loading complaints", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminViewModel.workers.collect { resource ->
                    if (resource is Resource.Success) {
                        availableWorkers = resource.data ?: emptyList()
                    } else if (resource is Resource.Error) {
                        Toast.makeText(this@AdminDashboardActivity, resource.message ?: "Error loading workers", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateStatistics(complaints: List<com.example.sewagemanagement.data.model.Complaint>) {
        val total = complaints.size
        val pending = complaints.count { it.status.equals("Pending", ignoreCase = true) }
        val inProgress = complaints.count { it.status.equals("In Progress", ignoreCase = true) }
        val resolved = complaints.count { it.status.equals("Resolved", ignoreCase = true) }

        binding.tvStatTotal.text = total.toString()
        binding.tvStatPending.text = pending.toString()
        binding.tvStatInProgress.text = inProgress.toString()
        binding.tvStatResolved.text = resolved.toString()
    }
}
