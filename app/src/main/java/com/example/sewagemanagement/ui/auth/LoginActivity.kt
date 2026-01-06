package com.example.sewagemanagement.ui.auth

import android.view.View
import android.view.animation.DecelerateInterpolator
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sewagemanagement.SewageApplication
import com.example.sewagemanagement.databinding.ActivityLoginBinding
import com.example.sewagemanagement.ui.ViewModelFactory
import com.example.sewagemanagement.ui.RoleNavigator
import com.example.sewagemanagement.ui.dashboard.DashboardActivity
import com.example.sewagemanagement.utils.Resource
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Use the factory to create the ViewModel with dependencies
    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(
            authRepository = (application as SewageApplication).container.authRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
        playEntryAnimations()
    }

    private fun playEntryAnimations() {
        // Initial state: transparent and slightly shifted down
        val views = listOf(
            binding.ivLogo,
            binding.tvTitle,
            binding.tvSubtitle,
            binding.tilEmail,
            binding.tilPassword,
            binding.btnLogin,
            binding.tvRegister
        )

        views.forEach { view ->
            view.alpha = 0f
            view.translationY = 50f
        }

        // Sequential animation
        views.forEachIndexed { index, view ->
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(100L * index)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar.isVisible = true
                            binding.btnLogin.isEnabled = false
                        }
                        is Resource.Success -> {
                            binding.progressBar.isVisible = false
                            binding.btnLogin.isEnabled = true
                            checkUserRoleAndNavigate()
                        }
                        is Resource.Error -> {
                            binding.progressBar.isVisible = false
                            binding.btnLogin.isEnabled = true
                            Toast.makeText(this@LoginActivity, resource.message, Toast.LENGTH_SHORT).show()
                        }
                        null -> {
                            // Initial state, do nothing
                            binding.progressBar.isVisible = false
                        }
                    }
                }
            }
        }
    }

    private fun checkUserRoleAndNavigate() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val authRepository = (application as SewageApplication).container.authRepository

        lifecycleScope.launch {
            when (val result = authRepository.getUser(userId)) {
                is Resource.Success -> {
                    val role = result.data?.role ?: "citizen"
                    RoleNavigator.startAndClearTask(this@LoginActivity, role)
                }
                is Resource.Error -> {
                    // Fallback to citizen dashboard if profile doc is missing
                    RoleNavigator.startAndClearTask(this@LoginActivity, "citizen")
                }
                is Resource.Loading -> {
                    // no-op
                }
            }
        }
    }
}
