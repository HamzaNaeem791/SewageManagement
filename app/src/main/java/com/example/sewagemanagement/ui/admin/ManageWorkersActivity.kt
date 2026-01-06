package com.example.sewagemanagement.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sewagemanagement.R
import com.example.sewagemanagement.SewageApplication
import com.example.sewagemanagement.databinding.ActivityManageWorkersBinding
import com.example.sewagemanagement.ui.ViewModelFactory
import com.example.sewagemanagement.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ManageWorkersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageWorkersBinding

    private val adminViewModel: AdminViewModel by viewModels {
        ViewModelFactory(
            authRepository = (application as SewageApplication).container.authRepository
        )
    }

    private val adapter = WorkerAdapter(
        onDeleteClick = { worker ->
            confirmDeleteWorker(worker.userId, worker.name)
        }
    )

    private var createWorkerDialog: androidx.appcompat.app.AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageWorkersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvWorkers.layoutManager = LinearLayoutManager(this)
        binding.rvWorkers.adapter = adapter

        binding.btnCreateWorker.setOnClickListener { showCreateWorkerDialog() }

        adminViewModel.fetchWorkers()

        observeWorkers()
        observeCreateWorker()
        observeDisableWorker()
    }

    private fun observeWorkers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminViewModel.workers.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            val activeWorkers = (resource.data ?: emptyList()).filter { !it.disabled }
                            adapter.submitList(activeWorkers)
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@ManageWorkersActivity, resource.message ?: "Error loading workers", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun observeCreateWorker() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminViewModel.createWorkerStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Toast.makeText(this@ManageWorkersActivity, "Creating worker...", Toast.LENGTH_SHORT).show()
                        is Resource.Success -> {
                            Toast.makeText(this@ManageWorkersActivity, "Worker account created", Toast.LENGTH_SHORT).show()
                            createWorkerDialog?.dismiss()
                            createWorkerDialog = null
                            adminViewModel.resetCreateWorkerStatus()
                            adminViewModel.fetchWorkers()
                        }
                        is Resource.Error -> {
                            val message = resource.message ?: "Failed to create worker"
                            Log.e("WorkerCreate", message)
                            MaterialAlertDialogBuilder(this@ManageWorkersActivity)
                                .setTitle("Create Worker Failed")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show()
                            adminViewModel.resetCreateWorkerStatus()
                        }
                        null -> {
                            // no-op
                        }
                    }
                }
            }
        }
    }

    private fun observeDisableWorker() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminViewModel.disableWorkerStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Toast.makeText(this@ManageWorkersActivity, "Deleting worker...", Toast.LENGTH_SHORT).show()
                        is Resource.Success -> {
                            Toast.makeText(this@ManageWorkersActivity, "Worker deleted", Toast.LENGTH_SHORT).show()
                            adminViewModel.resetDisableWorkerStatus()
                            adminViewModel.fetchWorkers()
                        }
                        is Resource.Error -> {
                            MaterialAlertDialogBuilder(this@ManageWorkersActivity)
                                .setTitle("Delete Worker Failed")
                                .setMessage(resource.message ?: "Failed to delete worker")
                                .setPositiveButton("OK", null)
                                .show()
                            adminViewModel.resetDisableWorkerStatus()
                        }
                        null -> {
                            // no-op
                        }
                    }
                }
            }
        }
    }

    private fun confirmDeleteWorker(userId: String, name: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Worker")
            .setMessage("Delete worker '$name'? This will disable the worker account in the app.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                adminViewModel.disableWorker(userId)
            }
            .show()
    }

    private fun showCreateWorkerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_worker, null)

        val etName = dialogView.findViewById<TextInputEditText>(R.id.etWorkerName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etWorkerEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etWorkerPassword)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.etWorkerPhone)
        val etAddress = dialogView.findViewById<TextInputEditText>(R.id.etWorkerAddress)

        val tilName = dialogView.findViewById<TextInputLayout>(R.id.tilWorkerName)
        val tilEmail = dialogView.findViewById<TextInputLayout>(R.id.tilWorkerEmail)
        val tilPassword = dialogView.findViewById<TextInputLayout>(R.id.tilWorkerPassword)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Create Worker Account")
            .setView(dialogView)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .setPositiveButton("Create", null)
            .create()

        dialog.setOnShowListener {
            val createButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            createButton.setOnClickListener {
                tilName.error = null
                tilEmail.error = null
                tilPassword.error = null

                val name = etName.text?.toString()?.trim().orEmpty()
                val email = etEmail.text?.toString()?.trim().orEmpty()
                val password = etPassword.text?.toString()?.trim().orEmpty()
                val phone = etPhone.text?.toString()?.trim().orEmpty()
                val address = etAddress.text?.toString()?.trim().orEmpty()

                var hasError = false
                if (name.isBlank()) {
                    tilName.error = "Required"
                    hasError = true
                }
                if (email.isBlank()) {
                    tilEmail.error = "Required"
                    hasError = true
                }
                if (password.isBlank()) {
                    tilPassword.error = "Required"
                    hasError = true
                }

                if (hasError) return@setOnClickListener

                adminViewModel.createWorker(name, email, password, phone, address)
            }
        }

        createWorkerDialog = dialog
        dialog.show()
    }
}
