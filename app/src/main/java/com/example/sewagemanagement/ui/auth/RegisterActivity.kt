package com.example.sewagemanagement.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sewagemanagement.SewageApplication
import com.example.sewagemanagement.databinding.ActivityRegisterBinding
import com.example.sewagemanagement.ui.ViewModelFactory
import com.example.sewagemanagement.utils.Resource
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    
    // Inject ViewModel
    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(
            authRepository = (application as SewageApplication).container.authRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.register(name, email, password)
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar.isVisible = true
                            binding.btnRegister.isEnabled = false
                        }
                        is Resource.Success -> {
                            binding.progressBar.isVisible = false
                            binding.btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterActivity, resource.data, Toast.LENGTH_SHORT).show()
                            // Close register screen on success, going back to login
                            finish()
                        }
                        is Resource.Error -> {
                            binding.progressBar.isVisible = false
                            binding.btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterActivity, resource.message, Toast.LENGTH_SHORT).show()
                        }
                        null -> {
                            binding.progressBar.isVisible = false
                        }
                    }
                }
            }
        }
    }
}
