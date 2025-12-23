package com.example.sewagemanagement.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sewagemanagement.data.repository.AuthRepository
import com.example.sewagemanagement.data.repository.ComplaintRepository
import com.example.sewagemanagement.ui.auth.AuthViewModel
import com.example.sewagemanagement.ui.complaint.ComplaintViewModel

class ViewModelFactory(
    private val authRepository: AuthRepository? = null,
    private val complaintRepository: ComplaintRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            if (authRepository == null) {
                throw IllegalArgumentException("AuthRepository required for AuthViewModel")
            }
            return AuthViewModel(authRepository) as T
        }
        if (modelClass.isAssignableFrom(ComplaintViewModel::class.java)) {
            if (complaintRepository == null) {
                throw IllegalArgumentException("ComplaintRepository required for ComplaintViewModel")
            }
            return ComplaintViewModel(complaintRepository) as T
        }
        if (modelClass.isAssignableFrom(com.example.sewagemanagement.ui.profile.ProfileViewModel::class.java)) {
             if (authRepository == null) {
                throw IllegalArgumentException("AuthRepository required for ProfileViewModel")
            }
            return com.example.sewagemanagement.ui.profile.ProfileViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
