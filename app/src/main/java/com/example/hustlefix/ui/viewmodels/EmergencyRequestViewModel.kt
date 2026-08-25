package com.example.hustlefix.ui.viewmodels

import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.EmergencyRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.coroutines.resume

data class EmergencyUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val currentAddress: String = "Locating...",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationReceived: Boolean = false
)

class EmergencyRequestViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            try {
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                if (location != null) {
                    val address = getAddress(location.latitude, location.longitude)
                    _uiState.value = _uiState.value.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        currentAddress = address,
                        locationReceived = true
                    )
                }
            } catch (e: SecurityException) {
                _uiState.value = _uiState.value.copy(currentAddress = "Permission denied")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(currentAddress = "Location error")
            }
        }
    }

    private suspend fun getAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(getApplication(), Locale.getDefault())
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        continuation.resume(addresses.firstOrNull()?.getAddressLine(0) ?: "Unknown Location")
                    }
                    override fun onError(errorMessage: String?) {
                        continuation.resume("Lat: $lat, Lng: $lng")
                    }
                })
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                addresses?.get(0)?.getAddressLine(0) ?: "Unknown Location"
            } catch (e: Exception) {
                "Lat: $lat, Lng: $lng"
            }
        }
    }

    fun sendEmergencyRequest(type: String, description: String) {
        val user = auth.currentUser ?: return
        if (!_uiState.value.locationReceived) {
            _uiState.value = _uiState.value.copy(error = "Waiting for location...")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        val ref = database.getReference("emergency_requests")
        val id = ref.push().key ?: return
        
        val request = EmergencyRequest(
            user.uid,
            user.displayName ?: "User",
            user.phoneNumber ?: "",
            type,
            description,
            _uiState.value.latitude,
            _uiState.value.longitude,
            _uiState.value.currentAddress
        )
        request.id = id

        ref.child(id).setValue(request).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Database error")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
