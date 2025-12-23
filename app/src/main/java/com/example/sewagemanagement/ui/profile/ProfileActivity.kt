package com.example.sewagemanagement.ui.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sewagemanagement.R
import com.example.sewagemanagement.SewageApplication
import com.example.sewagemanagement.data.model.User
import com.example.sewagemanagement.databinding.ActivityProfileBinding
import com.example.sewagemanagement.ui.ViewModelFactory
import com.example.sewagemanagement.utils.Resource
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory(
            authRepository = (application as SewageApplication).container.authRepository
        )
    }

    private var isEditing = false
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        setupObservers()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.loadUserProfile(userId)
        } else {
            Toast.makeText(this, "Session Expired", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.fabEdit.setOnClickListener {
            toggleEditMode()
        }
        
        binding.etDob.setOnClickListener {
            if (isEditing) showDatePicker()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userProfile.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                // optional: show loading
                            }
                            is Resource.Success -> {
                                currentUser = resource.data
                                populateFields(resource.data)
                            }
                            is Resource.Error -> {
                                Toast.makeText(this@ProfileActivity, resource.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.updateStatus.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                binding.fabEdit.isEnabled = false
                                binding.fabEdit.text = "Saving..."
                            }
                            is Resource.Success -> {
                                binding.fabEdit.isEnabled = true
                                Toast.makeText(this@ProfileActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                                isEditing = true // toggleEditMode will flip it to false
                                toggleEditMode()
                                viewModel.resetUpdateStatus()
                            }
                            is Resource.Error -> {
                                binding.fabEdit.isEnabled = true
                                binding.fabEdit.text = "Save"
                                Snackbar.make(binding.root, resource.message ?: "Error", Snackbar.LENGTH_LONG).show()
                                viewModel.resetUpdateStatus()
                            }
                            null -> {}
                        }
                    }
                }
            }
        }
    }

    private fun populateFields(user: User?) {
        user?.let {
            binding.tvHeaderName.text = it.name
            binding.etName.setText(it.name)
            binding.etEmail.setText(it.email)
            binding.etPhone.setText(it.phoneNumber)
            binding.etDob.setText(it.dob)
            binding.etAddress.setText(it.address)
        }
    }

    private fun toggleEditMode() {
        isEditing = !isEditing

        if (isEditing) {
            // Enter Edit Mode
            enableFields(true)
            binding.fabEdit.text = "Save Changes"
            binding.fabEdit.setIconResource(android.R.drawable.ic_menu_save)
            binding.fabEdit.backgroundTintList = getColorStateList(R.color.secondary)
            binding.etName.requestFocus()
        } else {
            // Save Changes
            val updatedUser = currentUser?.copy(
                name = binding.etName.text.toString(),
                // Email usually not editable here or requires re-auth
                phoneNumber = binding.etPhone.text.toString(),
                dob = binding.etDob.text.toString(),
                address = binding.etAddress.text.toString()
            )
            
            if (updatedUser != null) {
                viewModel.updateUserProfile(updatedUser)
            }
            
            enableFields(false)
            binding.fabEdit.text = "Edit Profile"
            binding.fabEdit.setIconResource(android.R.drawable.ic_menu_edit)
        }
    }

    private fun enableFields(enable: Boolean) {
        binding.etName.isEnabled = enable
        // binding.etEmail.isEnabled = enable // Email stays disabled
        binding.etPhone.isEnabled = enable
        binding.etDob.isEnabled = enable // Handled by click listener but needs enabling
        binding.etAddress.isEnabled = enable
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.etDob.setText(date)
        }, year, month, day).show()
    }
}
