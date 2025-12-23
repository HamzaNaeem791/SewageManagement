package com.example.sewagemanagement.ui.complaint

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sewagemanagement.R
import com.example.sewagemanagement.databinding.ActivityComplaintTrackingBinding

class ComplaintTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComplaintTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComplaintTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val issueType = intent.getStringExtra("ISSUE_TYPE") ?: "Complaint"
        val status = intent.getStringExtra("STATUS") ?: "Pending"
        val description = intent.getStringExtra("DESCRIPTION") ?: ""
        val date = intent.getStringExtra("DATE") ?: ""
        val id = intent.getStringExtra("COMPLAINT_ID") ?: ""

        binding.tvIssueType.text = issueType
        binding.tvDescription.text = description
        binding.tvDateStep1.text = date
        binding.tvId.text = "ID: #$id"

        updateTimeline(status)
    }

    private fun updateTimeline(status: String) {
        val activeColor = getColor(R.color.secondary)
        val inactiveColor = getColor(R.color.gray_light)

        // Step 1 is always done if we are here
        // Checkboxes: android.R.drawable.checkbox_on_background

        when (status) {
            "Pending" -> {
                // Only step 1 active
            }
            "In Progress" -> {
                binding.ivStep2.setImageResource(android.R.drawable.checkbox_on_background)
                binding.ivStep2.setColorFilter(activeColor)
            }
            "Resolved" -> {
                binding.ivStep2.setImageResource(android.R.drawable.checkbox_on_background)
                binding.ivStep2.setColorFilter(activeColor)
                binding.ivStep3.setImageResource(android.R.drawable.checkbox_on_background)
                binding.ivStep3.setColorFilter(activeColor)
            }
        }
    }
}
