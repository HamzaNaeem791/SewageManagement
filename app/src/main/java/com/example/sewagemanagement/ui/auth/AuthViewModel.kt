package com.example.sewagemanagement.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sewagemanagement.data.repository.AuthRepository
import com.example.sewagemanagement.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authStatus = MutableStateFlow<Resource<String>?>(null)
    val authStatus: StateFlow<Resource<String>?> = _authStatus.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authStatus.value = Resource.Error("Fields cannot be empty")
            return
        }
        _authStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.login(email, pass)
            _authStatus.value = result
        }
    }

    fun register(name: String, email: String, pass: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _authStatus.value = Resource.Error("All fields are required")
            return
        }
        if (pass.length < 6) {
             _authStatus.value = Resource.Error("Password must be at least 6 characters")
             return
        }
        _authStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.register(name, email, pass)
            _authStatus.value = result
        }
    }
}
