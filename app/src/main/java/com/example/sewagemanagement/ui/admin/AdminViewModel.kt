package com.example.sewagemanagement.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sewagemanagement.data.model.User
import com.example.sewagemanagement.data.repository.AuthRepository
import com.example.sewagemanagement.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _workers = MutableStateFlow<Resource<List<User>>>(Resource.Loading())
    val workers: StateFlow<Resource<List<User>>> = _workers.asStateFlow()

    private val _createWorkerStatus = MutableStateFlow<Resource<String>?>(null)
    val createWorkerStatus: StateFlow<Resource<String>?> = _createWorkerStatus.asStateFlow()

    private val _disableWorkerStatus = MutableStateFlow<Resource<String>?>(null)
    val disableWorkerStatus: StateFlow<Resource<String>?> = _disableWorkerStatus.asStateFlow()

    fun fetchWorkers() {
        _workers.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.getUsersByRole("worker")
            _workers.value = result
        }
    }

    fun createWorker(name: String, email: String, password: String, phoneNumber: String, address: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _createWorkerStatus.value = Resource.Error("Name, email and password are required")
            return
        }
        if (password.length < 6) {
            _createWorkerStatus.value = Resource.Error("Password must be at least 6 characters")
            return
        }

        _createWorkerStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.createWorkerAccount(
                name = name,
                email = email,
                password = password,
                phoneNumber = phoneNumber,
                address = address
            )
            _createWorkerStatus.value = result
        }
    }

    fun resetCreateWorkerStatus() {
        _createWorkerStatus.value = null
    }

    fun disableWorker(workerUserId: String) {
        if (workerUserId.isBlank()) {
            _disableWorkerStatus.value = Resource.Error("Invalid worker")
            return
        }

        _disableWorkerStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.disableWorker(workerUserId)
            _disableWorkerStatus.value = result
        }
    }

    fun resetDisableWorkerStatus() {
        _disableWorkerStatus.value = null
    }
}
