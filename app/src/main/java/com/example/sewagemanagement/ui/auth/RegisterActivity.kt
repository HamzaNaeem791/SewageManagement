package com.example.sewagemanagement.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
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
import android.view.animation.DecelerateInterpolator

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
        setupTermsText()
        observeViewModel()
        playEntryAnimations()
    }

    private fun setupTermsText() {
        val fullText = "I agree to the Terms and Conditions and Privacy Policy"
        val spannableString = SpannableString(fullText)

        val termsClickable = object : ClickableSpan() {
            override fun onClick(view: View) {
                startActivity(Intent(this@RegisterActivity, TermsActivity::class.java))
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = Color.parseColor("#0D47A1") // primary color
            }
        }

        val privacyClickable = object : ClickableSpan() {
            override fun onClick(view: View) {
                startActivity(Intent(this@RegisterActivity, TermsActivity::class.java))
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = Color.parseColor("#0D47A1") // primary color
            }
        }

        spannableString.setSpan(termsClickable, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(privacyClickable, 40, 54, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvAgreement.text = spannableString
        binding.tvAgreement.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun playEntryAnimations() {
        val views = listOf(
            binding.ivLogo,
            binding.tvTitle,
            binding.tvSubtitle,
            binding.tilName,
            binding.tilEmail,
            binding.tilPassword,
            binding.llAgreement,
            binding.btnRegister,
            binding.tvLogin
        )

        views.forEach { view ->
            view.alpha = 0f
            view.translationY = 50f
        }

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
        binding.btnRegister.setOnClickListener {
            if (!binding.cbTerms.isChecked) {
                Toast.makeText(this, "Please agree to Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
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
