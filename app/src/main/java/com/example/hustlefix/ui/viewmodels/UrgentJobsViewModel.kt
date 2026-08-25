package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.EmergencyRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UrgentJobsUiState(
    val urgentJobs: List<EmergencyRequest> = emptyList(),
    val isLoading: Boolean = false
)

class UrgentJobsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UrgentJobsUiState())
    val uiState: StateFlow<UrgentJobsUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var jobsRef: DatabaseReference? = null
    private var jobsListener: ValueEventListener? = null

    init {
        loadUrgentJobs()
    }

    private fun loadUrgentJobs() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        jobsRef?.removeEventListener(jobsListener!!)
        jobsRef = database.getReference("emergency_requests")
        
        jobsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(EmergencyRequest::class.java) }
                    .filter { it.status == "pending" }
                    .sortedByDescending { it.timestamp }
                
                _uiState.value = _uiState.value.copy(urgentJobs = list, isLoading = false)
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
        jobsRef?.addValueEventListener(jobsListener!!)
    }

    fun acceptJob(job: EmergencyRequest) {
        val user = auth.currentUser ?: return
        val updates = mapOf(
            "status" to "responded",
            "responderId" to user.uid,
            "responderName" to (user.displayName ?: "Professional")
        )
        
        database.getReference("emergency_requests").child(job.id ?: "").updateChildren(updates)
    }

    override fun onCleared() {
        super.onCleared()
        jobsRef?.removeEventListener(jobsListener!!)
    }
}
