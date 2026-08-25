package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Rating
import com.example.hustlefix.Worker
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WorkerProfileUiState(
    val worker: Worker? = null,
    val reviews: List<Rating> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WorkerProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WorkerProfileUiState())
    val uiState: StateFlow<WorkerProfileUiState> = _uiState.asStateFlow()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var reviewsQuery: Query? = null
    private var reviewsListener: ValueEventListener? = null

    fun loadWorker(workerId: String) {
        if (workerId.isEmpty()) return
        _uiState.value = _uiState.value.copy(isLoading = true)

        // Load Worker Info
        database.getReference("users").child(workerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val worker = snapshot.getValue(Worker::class.java)
                if (worker != null) {
                    _uiState.value = _uiState.value.copy(worker = worker)
                    loadReviews(workerId)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Worker not found")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
            }
        })
    }

    private fun loadReviews(workerId: String) {
        reviewsListener?.let { reviewsQuery?.removeEventListener(it) }
        
        reviewsQuery = database.getReference("ratings").orderByChild("ratedId").equalTo(workerId)
        reviewsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Rating::class.java) }
                _uiState.value = _uiState.value.copy(
                    reviews = list.sortedByDescending { it.timestamp },
                    isLoading = false
                )
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
        reviewsListener?.let { reviewsQuery?.addValueEventListener(it) }
    }

    override fun onCleared() {
        super.onCleared()
        reviewsListener?.let { reviewsQuery?.removeEventListener(it) }
    }
}
