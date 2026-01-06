package com.example.sewagemanagement.ui.worker

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
import com.example.sewagemanagement.databinding.ActivityWorkerDashboardBinding
import com.example.sewagemanagement.ui.ViewModelFactory
import com.example.sewagemanagement.ui.RoleNavigator
import com.example.sewagemanagement.ui.auth.LoginActivity
import com.example.sewagemanagement.ui.complaint.ComplaintAdapter
import com.example.sewagemanagement.ui.complaint.ComplaintViewModel
import com.example.sewagemanagement.utils.Resource
import com.example.sewagemanagement.SewageApplication
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class WorkerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerDashboardBinding
    private val viewModel: ComplaintViewModel by viewModels {
        ViewModelFactory(
            complaintRepository = (application as SewageApplication).container.complaintRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!ensureSessionAndRole()) return

        setupUI()
        setupObservers()
        
        loadJobs()
    }

    private fun ensureSessionAndRole(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return false
        }

        lifecycleScope.launch {
            val authRepository = (application as SewageApplication).container.authRepository
            when (val result = authRepository.getUser(userId)) {
                is Resource.Success -> {
                    val role = (result.data?.role ?: "citizen").trim().lowercase()
                    if (role != "worker") {
                        RoleNavigator.startAndClearTask(this@WorkerDashboardActivity, role)
                    }
                }
                else -> {
                    startActivity(Intent(this@WorkerDashboardActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
        return true
    }

    private fun loadJobs() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.getWorkerJobs(userId)
        }
    }

    private fun setupUI() {
        val adapter = ComplaintAdapter(
            onItemClick = { complaint ->
                showStatusUpdateDialog(complaint)
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
        binding.rvAssignedJobs.layoutManager = LinearLayoutManager(this)
        binding.rvAssignedJobs.adapter = adapter

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showStatusUpdateDialog(complaint: com.example.sewagemanagement.data.model.Complaint) {
        val statuses = arrayOf("In Progress", "Resolved")
        AlertDialog.Builder(this)
            .setTitle("Update Job Status")
            .setItems(statuses) { _, which ->
                val newStatus = statuses[which]
                viewModel.updateStatus(complaint.complaintId, newStatus)
                Toast.makeText(this, "Updating to $newStatus...", Toast.LENGTH_SHORT).show()
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
                            (binding.rvAssignedJobs.adapter as ComplaintAdapter).submitList(complaints)
                            
                            updateStatistics(complaints)
                            
                            if (complaints.isEmpty()) {
                                binding.llEmptyState.visibility = View.VISIBLE
                                binding.rvAssignedJobs.visibility = View.GONE
                            } else {
                                binding.llEmptyState.visibility = View.GONE
                                binding.rvAssignedJobs.visibility = View.VISIBLE
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@WorkerDashboardActivity, resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateStatistics(complaints: List<com.example.sewagemanagement.data.model.Complaint>) {
        val assigned = complaints.size
        val resolved = complaints.count { it.status.equals("Resolved", ignoreCase = true) }

        binding.tvStatAssigned.text = assigned.toString()
        binding.tvStatResolved.text = resolved.toString()
    }
}
