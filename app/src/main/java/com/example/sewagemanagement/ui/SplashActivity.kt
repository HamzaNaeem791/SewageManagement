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
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // User is signed in, redirect to Dashboard
                startActivity(Intent(this, com.example.sewagemanagement.ui.dashboard.DashboardActivity::class.java))
            } else {
                // No user is signed in, redirect to Login
                startActivity(Intent(this, LoginActivity::class.java))
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2500)
    }
}
