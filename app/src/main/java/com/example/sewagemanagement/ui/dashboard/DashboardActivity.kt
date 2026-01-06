package com.example.sewagemanagement.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.sewagemanagement.databinding.ActivityDashboardBinding
import com.example.sewagemanagement.ui.auth.LoginActivity
import com.example.sewagemanagement.ui.complaint.ComplaintHistoryActivity
import com.example.sewagemanagement.ui.complaint.SubmitComplaintActivity
import com.example.sewagemanagement.ui.profile.ProfileActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkUserSession()

        setupBannerAd()

        binding.btnSubmitComplaint.setOnClickListener {
            startActivity(Intent(this, SubmitComplaintActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, ComplaintHistoryActivity::class.java))
        }

        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupBannerAd() {
        Log.d("AdMobBanner", "Dashboard: loading banner")
        binding.adViewBanner.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("AdMobBanner", "Dashboard: banner loaded")
                binding.adViewBanner.visibility = android.view.View.VISIBLE
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("AdMobBanner", "Dashboard: banner failed: ${adError.message} (${adError.code})")
                binding.adViewBanner.visibility = android.view.View.GONE
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

    private fun checkUserSession() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
