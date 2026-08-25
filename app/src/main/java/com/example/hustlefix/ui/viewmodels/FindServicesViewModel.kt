package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Service
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FindServicesUiState(
    val services: List<Service> = emptyList(),
    val filteredServices: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

class FindServicesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FindServicesUiState())
    val uiState: StateFlow<FindServicesUiState> = _uiState.asStateFlow()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    init {
        loadServices()
    }

    private fun loadServices() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        database.getReference("services").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Service>()
                for (serviceSnapshot in snapshot.children) {
                    val service = serviceSnapshot.getValue(Service::class.java)
                    if (service != null) {
                        list.add(service)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    services = list,
                    filteredServices = list,
                    isLoading = false
                )
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        })
    }

    fun onSearchQueryChange(query: String) {
        val filtered = _uiState.value.services.filter {
            (it.title ?: "").contains(query, ignoreCase = true) ||
            (it.category ?: "").contains(query, ignoreCase = true)
        }
        _uiState.value = _uiState.value.copy(searchQuery = query, filteredServices = filtered)
    }
}
