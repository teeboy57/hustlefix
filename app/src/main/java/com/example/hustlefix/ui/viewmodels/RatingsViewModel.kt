package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Rating
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RatingsUiState(
    val ratings: List<Rating> = emptyList(),
    val isLoading: Boolean = false
)

class RatingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RatingsUiState())
    val uiState: StateFlow<RatingsUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadRatings()
    }

    private fun loadRatings() {
        val uid = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        database.getReference("ratings").orderByChild("ratedId").equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(Rating::class.java) }
                    _uiState.value = _uiState.value.copy(ratings = list.sortedByDescending { it.timestamp }, isLoading = false)
                }
                override fun onCancelled(error: DatabaseError) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            })
    }
}
