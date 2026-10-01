package com.example.hustlefix.ui.viewmodels

import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.EmergencyRequest
import com.example.hustlefix.util.ActivityLogger
import com.example.hustlefix.util.AnalyticsHelper
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
    val locationReceived: Boolean = false,
    val activeRequest: EmergencyRequest? = null
)

class EmergencyRequestViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    
    private var requestRef: com.google.firebase.database.DatabaseReference? = null
    private var requestListener: com.google.firebase.database.ValueEventListener? = null

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
                // Log Activity & Analytics
                ActivityLogger.logEmergency(user.uid, user.displayName ?: "User", type)
                AnalyticsHelper.logEmergencyTriggered(type, _uiState.value.currentAddress)
                
                // NEARBY BLAST AUTOMATION
                blastToNearbyPros(id, type, description, _uiState.value.latitude, _uiState.value.longitude)
                
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, activeRequest = request)
                listenToRequest(id)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Database error")
            }
        }
    }

    private fun blastToNearbyPros(requestId: String, type: String, description: String, userLat: Double, userLng: Double) {
        database.getReference("users").orderByChild("role").equalTo("worker").get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                val proLat = child.child("latitude").getValue(Double::class.java) ?: 0.0
                val proLng = child.child("longitude").getValue(Double::class.java) ?: 0.0
                val isVerified = child.child("verified").getValue(Boolean::class.java) ?: false
                
                if (isVerified) {
                    val distance = calculateDistance(userLat, userLng, proLat, proLng)
                    if (distance <= 10.0) { // 10km radius
                        val proId = child.key ?: continue
                        val notifRef = database.getReference("notifications").child(proId).push()
                        val notif = mapOf(
                            "id" to notifRef.key,
                            "userId" to proId,
                            "title" to "🚨 URGENT: $type nearby!",
                            "message" to "A client needs immediate help within 10km. View details in the Emergency Hub.",
                            "type" to "emergency",
                            "relatedId" to requestId,
                            "timestamp" to System.currentTimeMillis(),
                            "read" to false
                        )
                        notifRef.setValue(notif)
                    }
                }
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    private fun listenToRequest(requestId: String) {
        requestListener?.let { requestRef?.removeEventListener(it) }
        
        requestRef = database.getReference("emergency_requests").child(requestId)
        requestListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val updated = snapshot.getValue(EmergencyRequest::class.java)
                if (updated != null) {
                    _uiState.value = _uiState.value.copy(activeRequest = updated)
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        requestListener?.let { requestRef?.addValueEventListener(it) }
    }

    override fun onCleared() {
        super.onCleared()
        requestListener?.let { requestRef?.removeEventListener(it) }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
