package com.example.sewagemanagement.ui.complaint

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sewagemanagement.databinding.ActivityComplaintHistoryBinding
import com.example.sewagemanagement.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.example.sewagemanagement.ui.ViewModelFactory
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch

class ComplaintHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComplaintHistoryBinding
    private val viewModel: ComplaintViewModel by viewModels {
        ViewModelFactory(
            complaintRepository = (application as com.example.sewagemanagement.SewageApplication).container.complaintRepository
        )
    }
    private val adapter = ComplaintAdapter(onItemClick = { complaint ->
        val intent = android.content.Intent(this, ComplaintTrackingActivity::class.java)
        // Pass essential data. In a real app, pass Parcelable. Here passing fields.
        intent.putExtra("COMPLAINT_ID", complaint.complaintId)
        intent.putExtra("ISSUE_TYPE", complaint.issueType)
        intent.putExtra("STATUS", complaint.status)
        intent.putExtra("DESCRIPTION", complaint.description)
        intent.putExtra("DATE", java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(complaint.timestamp))
        intent.putExtra("LATITUDE", complaint.location?.latitude ?: 0.0)
        intent.putExtra("LONGITUDE", complaint.location?.longitude ?: 0.0)
        startActivity(intent)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComplaintHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupBannerAd()
        
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.getComplaints(userId)
        } else {
             Toast.makeText(this, "User session invalid", Toast.LENGTH_SHORT).show()
             finish()
        }

        observeViewModel()
    }

    private fun setupBannerAd() {
        Log.d("AdMobBanner", "ComplaintHistory: loading banner")
        binding.adViewBanner.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("AdMobBanner", "ComplaintHistory: banner loaded")
                binding.adViewBanner.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("AdMobBanner", "ComplaintHistory: banner failed: ${adError.message} (${adError.code})")
                binding.adViewBanner.visibility = View.GONE
            }
        }
        binding.adViewBanner.loadAd(AdRequest.Builder().build())
    }

    override fun onResume() {
        super.onResume()
        binding.adViewBanner.resume()
    }

    override fun onPause() {
        binding.adViewBanner.pause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.adViewBanner.destroy()
        super.onDestroy()
    }

    private fun setupRecyclerView() {
        binding.rvComplaints.layoutManager = LinearLayoutManager(this)
        binding.rvComplaints.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.complaints.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            resource.data?.let { adapter.submitList(it) }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@ComplaintHistoryActivity, resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
