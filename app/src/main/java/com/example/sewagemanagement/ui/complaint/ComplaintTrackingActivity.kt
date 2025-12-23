package com.example.sewagemanagement.ui.complaint

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sewagemanagement.R
import com.example.sewagemanagement.databinding.ActivityComplaintTrackingBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ComplaintTrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityComplaintTrackingBinding
    private var complaintLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComplaintTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val issueType = intent.getStringExtra("ISSUE_TYPE") ?: "Complaint"
        val status = intent.getStringExtra("STATUS") ?: "Pending"
        val description = intent.getStringExtra("DESCRIPTION") ?: ""
        val date = intent.getStringExtra("DATE") ?: ""
        val id = intent.getStringExtra("COMPLAINT_ID") ?: ""
        val lat = intent.getDoubleExtra("LATITUDE", 0.0)
        val lng = intent.getDoubleExtra("LONGITUDE", 0.0)
        complaintLatLng = LatLng(lat, lng)

        binding.tvIssueType.text = issueType
        binding.tvDescription.text = description
        binding.tvDateStep1.text = date
        binding.tvId.text = "ID: #$id"

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        updateTimeline(status)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        complaintLatLng?.let { latLng ->
            googleMap.addMarker(MarkerOptions().position(latLng).title("Reported Issue"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    private fun updateTimeline(status: String) {
        val activeColor = getColor(R.color.secondary)
        val inactiveColor = getColor(R.color.gray_light)

        // Step 1 is always done if we are here
        // Checkboxes: android.R.drawable.checkbox_on_background

        when (status) {
            "Pending" -> {
                // Only step 1 active
            }
            "In Progress" -> {
                binding.ivStep2.setImageResource(android.R.drawable.checkbox_on_background)
                binding.ivStep2.setColorFilter(activeColor)
            }
            "Resolved" -> {
                binding.ivStep2.setImageResource(android.R.drawable.checkbox_on_background)
                binding.ivStep2.setColorFilter(activeColor)
                binding.ivStep3.setImageResource(android.R.drawable.checkbox_on_background)
                binding.ivStep3.setColorFilter(activeColor)
            }
        }
    }
}
