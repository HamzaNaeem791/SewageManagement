package com.example.sewagemanagement.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.sewagemanagement.ui.admin.AdminDashboardActivity
import com.example.sewagemanagement.ui.dashboard.DashboardActivity
import com.example.sewagemanagement.ui.worker.WorkerDashboardActivity

object RoleNavigator {

    fun intentForRole(context: Context, role: String): Intent {
        return when (role) {
            "admin" -> Intent(context, AdminDashboardActivity::class.java)
            "worker" -> Intent(context, WorkerDashboardActivity::class.java)
            else -> Intent(context, DashboardActivity::class.java)
        }
    }

    fun startAndClearTask(activity: Activity, role: String) {
        val intent = intentForRole(activity, role).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }
}
