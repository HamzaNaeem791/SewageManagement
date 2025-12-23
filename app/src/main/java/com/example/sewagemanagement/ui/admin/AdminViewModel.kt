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

    fun fetchWorkers() {
        _workers.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.getUsersByRole("worker")
            _workers.value = result
        }
    }
}
