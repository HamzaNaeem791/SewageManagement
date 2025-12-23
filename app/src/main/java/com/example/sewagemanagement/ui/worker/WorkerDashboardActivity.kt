package com.example.sewagemanagement.ui.worker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sewagemanagement.databinding.ActivityWorkerDashboardBinding
import com.example.sewagemanagement.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class WorkerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        
        binding.btnAssignedJobs.setOnClickListener {
            // TODO: Navigate to Assigned Jobs
        }
    }
}
