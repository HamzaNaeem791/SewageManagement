package com.example.sewagemanagement.ui.complaint

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sewagemanagement.databinding.ActivityComplaintHistoryBinding
import com.example.sewagemanagement.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.example.sewagemanagement.ui.ViewModelFactory
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch

class ComplaintHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComplaintHistoryBinding
    private val viewModel: ComplaintViewModel by viewModels {
        ViewModelFactory(
            complaintRepository = (application as com.example.sewagemanagement.SewageApplication).container.complaintRepository
        )
    }
    private val adapter = ComplaintAdapter { complaint ->
        val intent = android.content.Intent(this, ComplaintTrackingActivity::class.java)
        // Pass essential data. In a real app, pass Parcelable. Here passing fields.
        intent.putExtra("COMPLAINT_ID", complaint.timestamp.time.toString()) // Using timestamp as pseudo-ID or pass real doc ID if added to model
        intent.putExtra("ISSUE_TYPE", complaint.issueType)
        intent.putExtra("STATUS", complaint.status)
        intent.putExtra("DESCRIPTION", complaint.description)
        intent.putExtra("DATE", java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(complaint.timestamp))
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComplaintHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.getComplaints(userId)
        } else {
             Toast.makeText(this, "User session invalid", Toast.LENGTH_SHORT).show()
             finish()
        }

        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvComplaints.layoutManager = LinearLayoutManager(this)
        binding.rvComplaints.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.complaints.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            resource.data?.let { adapter.submitList(it) }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@ComplaintHistoryActivity, resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
