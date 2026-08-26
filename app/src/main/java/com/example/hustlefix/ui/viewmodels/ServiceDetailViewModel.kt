package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Booking
import com.example.hustlefix.Rating
import com.example.hustlefix.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class ServiceDetailUiState(
    val service: Service? = null,
    val reviews: List<Rating> = emptyList(),
    val isLoading: Boolean = false,
    val bookingSuccess: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class ServiceDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServiceDetailUiState())
    val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun loadService(serviceId: String) {
        if (serviceId.isEmpty()) return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        database.getReference("services").child(serviceId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val service = snapshot.getValue(Service::class.java)
                _uiState.value = _uiState.value.copy(service = service, isLoading = false)
                if (service != null) {
                    loadReviews(service.getserviceProviderId())
                }
                checkIfSaved(serviceId)
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
            }
        })
    }

    private fun loadReviews(providerId: String) {
        database.getReference("ratings").orderByChild("ratedId").equalTo(providerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(Rating::class.java) }
                    _uiState.value = _uiState.value.copy(reviews = list.sortedByDescending { it.timestamp })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun checkIfSaved(serviceId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("saved_services").child(uid).orderByValue().equalTo(serviceId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _uiState.value = _uiState.value.copy(isSaved = snapshot.exists())
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun toggleSaveService() {
        val serviceId = _uiState.value.service?.serviceId ?: return
        val uid = auth.currentUser?.uid ?: return
        val ref = database.getReference("saved_services").child(uid)

        if (_uiState.value.isSaved) {
            ref.orderByValue().equalTo(serviceId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                    _uiState.value = _uiState.value.copy(isSaved = false)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } else {
            ref.push().setValue(serviceId).addOnSuccessListener {
                _uiState.value = _uiState.value.copy(isSaved = true)
            }
        }
    }

    fun bookService(date: String, notes: String, onSuccess: () -> Unit) {
        val service = _uiState.value.service ?: return
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid
        
        _uiState.value = _uiState.value.copy(isLoading = true)

        // Get client name first
        database.getReference("users").child(userId).child("name").get().addOnSuccessListener { snapshot ->
            val clientName = snapshot.getValue(String::class.java) ?: currentUser.displayName ?: "Client"
            
            val bookingId = database.getReference("bookings").push().key ?: UUID.randomUUID().toString()
            
            val booking = Booking(
                service.serviceId,
                userId,
                clientName,
                service.getserviceProviderId(),
                service.getserviceProviderName(),
                service.price
            ).apply {
                setBookingId(bookingId)
                setServiceImageUrl(service.serviceImageUrl ?: service.serviceImageUrls?.firstOrNull())
                setPreferredDate(date)
                setInstructions(notes)
            }

            database.getReference("bookings").child(bookingId).setValue(booking)
                .addOnSuccessListener {
                    _uiState.value = _uiState.value.copy(isLoading = false, bookingSuccess = true)
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }.addOnFailureListener { e ->
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }
    
    fun clearStatus() {
        _uiState.value = _uiState.value.copy(bookingSuccess = false, error = null)
    }
}
