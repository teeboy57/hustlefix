package com.example.hustlefix.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Worker
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MapUiState(
    val workers: List<Worker> = emptyList(),
    val isLoading: Boolean = false,
    val userLatitude: Double = 0.0,
    val userLongitude: Double = 0.0,
    val trackedWorker: Worker? = null
)

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    init {
        loadNearbyWorkers()
        updateCurrentUserLocation()
    }

    private fun loadNearbyWorkers() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        database.getReference("users").orderByChild("role").equalTo("worker")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(Worker::class.java) }
                    _uiState.value = _uiState.value.copy(workers = list, isLoading = false)
                }
                override fun onCancelled(error: DatabaseError) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            })
    }

    private fun updateCurrentUserLocation() {
        viewModelScope.launch {
            try {
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
                if (location != null) {
                    _uiState.value = _uiState.value.copy(
                        userLatitude = location.latitude,
                        userLongitude = location.longitude
                    )
                    
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val updates = mapOf(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude,
                            "lastLocationUpdate" to System.currentTimeMillis()
                        )
                        database.getReference("users").child(uid).updateChildren(updates)
                    }
                }
            } catch (e: SecurityException) {
                // Handle no permission
            }
        }
    }

    fun startTrackingWorker(workerId: String) {
        database.getReference("users").child(workerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val worker = snapshot.getValue(Worker::class.java)
                    _uiState.value = _uiState.value.copy(trackedWorker = worker)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
