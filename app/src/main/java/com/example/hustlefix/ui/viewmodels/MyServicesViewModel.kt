package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MyServicesUiState(
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false
)

class MyServicesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MyServicesUiState())
    val uiState: StateFlow<MyServicesUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid

    init {
        loadServices()
    }

    private fun loadServices() {
        val uid = userId ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        database.getReference("services").orderByChild("serviceProviderId").equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(Service::class.java) }
                    _uiState.value = _uiState.value.copy(services = list, isLoading = false)
                }
                override fun onCancelled(error: DatabaseError) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            })
    }

    fun deleteService(serviceId: String) {
        database.getReference("services").child(serviceId).removeValue()
    }
}
