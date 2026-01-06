package com.example.sewagemanagement.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sewagemanagement.databinding.ActivitySplashBinding
import com.example.sewagemanagement.SewageApplication
import com.example.sewagemanagement.ui.auth.LoginActivity
import com.example.sewagemanagement.utils.Resource
import kotlinx.coroutines.launch

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
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startAuth()
            return
        }

        val authRepository = (application as SewageApplication).container.authRepository
        lifecycleScope.launch {
            when (val result = authRepository.getUser(currentUser.uid)) {
                is Resource.Success -> {
                    val role = result.data?.role ?: "citizen"
                    navigateBasedOnRole(role)
                }
                is Resource.Error -> {
                    startAuth()
                }
                is Resource.Loading -> {
                    // no-op (not used here)
                }
            }
        }
    }

    private fun navigateBasedOnRole(role: String) {
        val intent = RoleNavigator.intentForRole(this, role)
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
