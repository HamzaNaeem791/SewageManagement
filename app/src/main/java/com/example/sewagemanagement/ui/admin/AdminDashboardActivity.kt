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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        
        viewModel.fetchAllComplaints()
    }

    private fun setupUI() {
        val adapter = ComplaintAdapter { complaint ->
            showStatusUpdateDialog(complaint)
        }
        binding.rvAllComplaints.layoutManager = LinearLayoutManager(this)
        binding.rvAllComplaints.adapter = adapter

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showStatusUpdateDialog(complaint: com.example.sewagemanagement.data.model.Complaint) {
        val statuses = arrayOf("Pending", "In Progress", "Resolved")
        AlertDialog.Builder(this)
            .setTitle("Update Status: ${complaint.issueType}")
            .setItems(statuses) { _, which ->
                val newStatus = statuses[which]
                // We use timestamp string as pseudo-ID for now based on Repo implementation
                val id = complaint.timestamp.time.toString() 
                viewModel.updateStatus(id, newStatus)
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
                            (binding.rvAllComplaints.adapter as ComplaintAdapter).submitList(resource.data ?: emptyList())
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@AdminDashboardActivity, resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
