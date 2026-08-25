package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Service
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ServiceDetailUiState(
    val service: Service? = null,
    val isLoading: Boolean = false
)

class ServiceDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServiceDetailUiState())
    val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun loadService(serviceId: String) {
        if (serviceId.isEmpty()) return
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        database.getReference("services").child(serviceId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val service = snapshot.getValue(Service::class.java)
                _uiState.value = _uiState.value.copy(service = service, isLoading = false)
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        })
    }
}
