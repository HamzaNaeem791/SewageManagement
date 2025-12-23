package com.example.sewagemanagement.ui.complaint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sewagemanagement.data.model.Complaint
import com.example.sewagemanagement.data.repository.ComplaintRepository
import com.example.sewagemanagement.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ComplaintViewModel(
    private val repository: ComplaintRepository
) : ViewModel() {

    private val _submissionStatus = MutableStateFlow<Resource<String>?>(null)
    val submissionStatus: StateFlow<Resource<String>?> = _submissionStatus.asStateFlow()

    private val _complaints = MutableStateFlow<Resource<List<Complaint>>>(Resource.Loading())
    val complaints: StateFlow<Resource<List<Complaint>>> = _complaints.asStateFlow()

    fun submitComplaint(complaint: Complaint) {
        _submissionStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.submitComplaint(complaint)
            _submissionStatus.value = result
        }
    }

    fun getComplaints(userId: String) {
        _complaints.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getComplaintsForUser(userId)
            _complaints.value = result
        }
    }
}
