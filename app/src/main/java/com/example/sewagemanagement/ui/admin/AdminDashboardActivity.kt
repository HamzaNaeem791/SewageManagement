package com.example.sewagemanagement.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sewagemanagement.databinding.ActivityAdminDashboardBinding
import com.example.sewagemanagement.ui.RoleNavigator
import com.example.sewagemanagement.ui.auth.LoginActivity
import com.example.sewagemanagement.utils.Resource
import com.example.sewagemanagement.SewageApplication
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private var isAdminConfirmed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()

        // Keep it simple: disable navigation until role is confirmed.
        binding.btnManageComplaints.isEnabled = false
        binding.btnManageWorkers.isEnabled = false

        if (!ensureSessionAndRole()) return
    }

    private fun ensureSessionAndRole(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return false
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val authRepository = (application as SewageApplication).container.authRepository
            when (val result = authRepository.getUser(userId)) {
                is Resource.Success -> {
                    val role = (result.data?.role ?: "citizen").trim().lowercase()
                    if (role != "admin") {
                        RoleNavigator.startAndClearTask(this@AdminDashboardActivity, role)
                        return@launch
                    }

                    // Role confirmed.
                    isAdminConfirmed = true
                    binding.btnManageComplaints.isEnabled = true
                    binding.btnManageWorkers.isEnabled = true
                    binding.progressBar.visibility = View.GONE
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
        binding.btnManageComplaints.setOnClickListener {
            if (!isAdminConfirmed) return@setOnClickListener
            startActivity(Intent(this, ManageComplaintsActivity::class.java))
        }

        binding.btnManageWorkers.setOnClickListener {
            if (!isAdminConfirmed) return@setOnClickListener
            startActivity(Intent(this, ManageWorkersActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
