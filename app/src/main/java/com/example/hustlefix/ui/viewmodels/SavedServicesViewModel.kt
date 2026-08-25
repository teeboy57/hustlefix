package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SavedServicesUiState(
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

class SavedServicesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SavedServicesUiState())
    val uiState: StateFlow<SavedServicesUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userId: String? = auth.currentUser?.uid
    
    private var savedRef: DatabaseReference? = null
    private var savedListener: ValueEventListener? = null

    init {
        loadSavedServices()
    }

    private fun loadSavedServices() {
        val uid = userId ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        savedListener?.let { savedRef?.removeEventListener(it) }

        savedRef = database.getReference("saved_services").child(uid)
        savedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val serviceIds = mutableListOf<String>()
                for (ds in snapshot.children) {
                    ds.getValue(String::class.java)?.let { serviceIds.add(it) }
                }

                if (serviceIds.isEmpty()) {
                    _uiState.value = _uiState.value.copy(services = emptyList(), isLoading = false, isRefreshing = false)
                    return
                }

                fetchServiceDetails(serviceIds)
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false)
            }
        }
        savedListener?.let { savedRef?.addValueEventListener(it) }
    }

    private fun fetchServiceDetails(ids: List<String>) {
        val servicesList = mutableListOf<Service>()
        var count = 0
        
        for (id in ids) {
            database.getReference("services").child(id).get().addOnSuccessListener { snapshot ->
                snapshot.getValue(Service::class.java)?.let { servicesList.add(it) }
                count++
                if (count == ids.size) {
                    _uiState.value = _uiState.value.copy(services = servicesList, isLoading = false, isRefreshing = false)
                }
            }.addOnFailureListener {
                count++
                if (count == ids.size) {
                    _uiState.value = _uiState.value.copy(services = servicesList, isLoading = false, isRefreshing = false)
                }
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadSavedServices()
        viewModelScope.launch {
            delay(3000)
            if (_uiState.value.isRefreshing) {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        savedListener?.let { savedRef?.removeEventListener(it) }
    }
}
