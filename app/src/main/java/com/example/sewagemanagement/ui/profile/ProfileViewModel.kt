package com.example.sewagemanagement.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sewagemanagement.data.model.User
import com.example.sewagemanagement.data.repository.AuthRepository
import com.example.sewagemanagement.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Loading())
    val userProfile: StateFlow<Resource<User>> = _userProfile.asStateFlow()

    private val _updateStatus = MutableStateFlow<Resource<String>?>(null)
    val updateStatus: StateFlow<Resource<String>?> = _updateStatus.asStateFlow()

    fun loadUserProfile(userId: String) {
        _userProfile.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.getUser(userId)
            _userProfile.value = result
        }
    }

    fun updateUserProfile(user: User) {
        _updateStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.updateUser(user)
            _updateStatus.value = result
            if (result is Resource.Success) {
                // Refresh local data
                 _userProfile.value = Resource.Success(user)
            }
        }
    }

    fun resetUpdateStatus() {
        _updateStatus.value = null
    }
}
