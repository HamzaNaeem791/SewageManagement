package com.example.sewagemanagement.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.sewagemanagement.databinding.ActivitySplashBinding
import com.example.sewagemanagement.ui.auth.LoginActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animations
        val fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, com.example.sewagemanagement.R.anim.fade_in)
        val slideUp = android.view.animation.AnimationUtils.loadAnimation(this, com.example.sewagemanagement.R.anim.slide_up)

        binding.ivLogo.startAnimation(fadeIn)
        binding.tvAppName.startAnimation(slideUp)

        // Simple delay for splash (2.5 seconds)
        // In a real app, you might check for user session here
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAndNavigate()
        }, 2000)
    }

    private fun checkUserAndNavigate() {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val role = document.getString("role") ?: "citizen"
                        navigateBasedOnRole(role)
                    } else {
                        startAuth()
                    }
                }
                .addOnFailureListener {
                    startAuth()
                }
        } else {
            startAuth()
        }
    }

    private fun navigateBasedOnRole(role: String) {
        val intent = when (role) {
            "admin" -> Intent(this, com.example.sewagemanagement.ui.admin.AdminDashboardActivity::class.java)
            "worker" -> Intent(this, com.example.sewagemanagement.ui.worker.WorkerDashboardActivity::class.java)
            else -> Intent(this, com.example.sewagemanagement.ui.dashboard.DashboardActivity::class.java)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
    
    private fun startAuth() {
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
