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
import com.example.sewagemanagement.R
import com.example.sewagemanagement.ui.ViewModelFactory
import com.example.sewagemanagement.ui.RoleNavigator
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
    private var createWorkerDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!ensureSessionAndRole()) return

        setupUI()
        setupObservers()
        
        viewModel.fetchAllComplaints()
        adminViewModel.fetchWorkers()
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
                    if (role != "admin") {
                        RoleNavigator.startAndClearTask(this@AdminDashboardActivity, role)
                    }
                }
                else -> {
                    // If role can't be resolved, force re-auth
                    startActivity(Intent(this@AdminDashboardActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
        return true
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

        binding.btnCreateWorker.setOnClickListener {
            showCreateWorkerDialog()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showCreateWorkerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_worker, null)

        val etName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etWorkerName)
        val etEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etWorkerEmail)
        val etPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etWorkerPassword)
        val etPhone = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etWorkerPhone)
        val etAddress = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etWorkerAddress)

        val tilName = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilWorkerName)
        val tilEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilWorkerEmail)
        val tilPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilWorkerPassword)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Create Worker Account")
            .setView(dialogView)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .setPositiveButton("Create", null)
            .create()

        dialog.setOnShowListener {
            val createButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            createButton.setOnClickListener {
                tilName.error = null
                tilEmail.error = null
                tilPassword.error = null

                val name = etName.text?.toString()?.trim().orEmpty()
                val email = etEmail.text?.toString()?.trim().orEmpty()
                val password = etPassword.text?.toString()?.trim().orEmpty()
                val phone = etPhone.text?.toString()?.trim().orEmpty()
                val address = etAddress.text?.toString()?.trim().orEmpty()

                var hasError = false
                if (name.isBlank()) {
                    tilName.error = "Required"
                    hasError = true
                }
                if (email.isBlank()) {
                    tilEmail.error = "Required"
                    hasError = true
                }
                if (password.isBlank()) {
                    tilPassword.error = "Required"
                    hasError = true
                }

                if (hasError) return@setOnClickListener

                adminViewModel.createWorker(name, email, password, phone, address)
            }
        }

        createWorkerDialog = dialog
        dialog.show()
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
                viewModel.updateStatus(complaint.complaintId, newStatus)
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
                viewModel.assignJobToWorker(complaint.complaintId, selectedWorker.userId)
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminViewModel.createWorkerStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            Toast.makeText(this@AdminDashboardActivity, "Creating worker...", Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Success -> {
                            Toast.makeText(this@AdminDashboardActivity, "Worker account created", Toast.LENGTH_SHORT).show()
                            createWorkerDialog?.dismiss()
                            createWorkerDialog = null
                            adminViewModel.resetCreateWorkerStatus()
                            adminViewModel.fetchWorkers()
                        }
                        is Resource.Error -> {
                            Toast.makeText(this@AdminDashboardActivity, resource.message ?: "Failed to create worker", Toast.LENGTH_SHORT).show()
                            adminViewModel.resetCreateWorkerStatus()
                        }
                        null -> {
                            // no-op
                        }
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
