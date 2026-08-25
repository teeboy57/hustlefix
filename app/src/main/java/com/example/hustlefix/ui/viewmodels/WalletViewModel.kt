package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

data class WalletUiState(
    val balance: String = "R0.00",
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WalletViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userId: String? = auth.currentUser?.uid

    init {
        loadData()
    }

    private fun loadData() {
        val uid = userId ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        // Load Balance
        database.getReference("users").child(uid).child("walletBalance")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val balance = snapshot.getValue(Double::class.java) ?: 0.0
                    _uiState.value = _uiState.value.copy(
                        balance = String.format(Locale.getDefault(), "R%.2f", balance)
                    )
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // Load Transactions
        database.getReference("transactions").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Transaction>()
                    for (ds in snapshot.children) {
                        ds.getValue(Transaction::class.java)?.let { list.add(it) }
                    }
                    _uiState.value = _uiState.value.copy(
                        transactions = list.sortedByDescending { it.timestamp },
                        isLoading = false
                    )
                }
                override fun onCancelled(error: DatabaseError) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            })
    }

    fun topUp(amount: Double) {
        val uid = userId ?: return
        val userRef = database.getReference("users").child(uid)
        
        userRef.child("walletBalance").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val current = snapshot.getValue(Double::class.java) ?: 0.0
                userRef.child("walletBalance").setValue(current + amount)
                recordTransaction("Top Up", amount)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun recordTransaction(type: String, amount: Double) {
        val uid = userId ?: return
        val transRef = database.getReference("transactions").child(uid)
        val id = transRef.push().key ?: return

        val trans = mapOf(
            "id" to id,
            "type" to type,
            "amount" to amount,
            "timestamp" to System.currentTimeMillis()
        )
        transRef.child(id).setValue(trans)
    }
}
