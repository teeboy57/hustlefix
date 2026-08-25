package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IncomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalFees: Double = 0.0,
    val netPay: Double = 0.0,
    val isLoading: Boolean = false
)

class IncomeStatementViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid

    init {
        loadIncomeData()
    }

    private fun loadIncomeData() {
        val uid = userId ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        database.getReference("transactions").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                    val income = list.sumOf { it.amount }
                    val fees = 0.0 // Free platform for now
                    
                    _uiState.value = _uiState.value.copy(
                        transactions = list.sortedByDescending { it.timestamp },
                        totalIncome = income,
                        totalFees = fees,
                        netPay = income - fees,
                        isLoading = false
                    )
                }
                override fun onCancelled(error: DatabaseError) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            })
    }
}
