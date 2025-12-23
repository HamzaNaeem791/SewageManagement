package com.example.sewagemanagement.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sewagemanagement.databinding.ActivityDashboardBinding
import com.example.sewagemanagement.ui.auth.LoginActivity
import com.example.sewagemanagement.ui.complaint.ComplaintHistoryActivity
import com.example.sewagemanagement.ui.complaint.SubmitComplaintActivity
import com.example.sewagemanagement.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkUserSession()

        binding.btnSubmitComplaint.setOnClickListener {
            startActivity(Intent(this, SubmitComplaintActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, ComplaintHistoryActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun checkUserSession() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
