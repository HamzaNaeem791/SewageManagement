package com.example.sewagemanagement.ui.complaint

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.sewagemanagement.R
import com.example.sewagemanagement.data.model.Complaint
import com.example.sewagemanagement.databinding.ActivitySubmitComplaintBinding
import com.example.sewagemanagement.utils.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import java.util.Date
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.example.sewagemanagement.ui.ViewModelFactory
import kotlinx.coroutines.launch

class SubmitComplaintActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmitComplaintBinding
    private val viewModel: ComplaintViewModel by viewModels {
        ViewModelFactory(
            complaintRepository = (application as com.example.sewagemanagement.SewageApplication).container.complaintRepository
        )
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fetchLocation()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitComplaintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupSpinner()
        requestLocationPermission()

        // Image Picker Launcher
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                binding.ivPreview.visibility = View.VISIBLE
                binding.ivPreview.setImageURI(uri)
                Toast.makeText(this, "Image Selected (Preview Only)", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSelectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSubmit.setOnClickListener {
            submitComplaint()
        }

        observeViewModel()
    }

    private fun setupSpinner() {
        val issues = arrayOf("Blocked Pipeline", "Overflowing Sewage", "Broken Sewer Line", "Bad Odor", "Other")
        binding.spinnerIssue.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            issues
        )
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        // Show loading indication for location
        Toast.makeText(this, "Fetching precise location...", Toast.LENGTH_SHORT).show()
        
        val priority = com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
        val cancellationTokenSource = com.google.android.gms.tasks.CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(priority, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = location
                    Toast.makeText(this, "Location fetched successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Unable to get location. Ensure GPS is on.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Location error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun submitComplaint() {
        val issueType = binding.spinnerIssue.selectedItem.toString()
        val description = binding.etDescription.text.toString().trim()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) return
        if (description.isEmpty()) {
            binding.etDescription.error = "Description required"
            return
        }
        if (userLocation == null) {
            Toast.makeText(this, "Fetching location, try again", Toast.LENGTH_SHORT).show()
            return
        }

        val complaint = Complaint(
            userId = userId,
            issueType = issueType,
            description = description,
            location = GeoPoint(userLocation!!.latitude, userLocation!!.longitude),
            status = "Pending",
            timestamp = Date()
        )

        viewModel.submitComplaint(complaint)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submissionStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@SubmitComplaintActivity, "Complaint submitted", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@SubmitComplaintActivity, resource.message, Toast.LENGTH_LONG).show()
                        }
                        null -> binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }
}
