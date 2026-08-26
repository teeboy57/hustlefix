package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

data class WalletUiState(
    val balance: String = "R0.00",
    val balanceValue: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val isWithdrawalSuccess: Boolean = false,
    val topUpCheckoutUrl: String? = null,
    val error: String? = null
)

class WalletViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userId: String? = auth.currentUser?.uid

    private var balanceRef: DatabaseReference? = null
    private var balanceListener: ValueEventListener? = null
    private var transRef: DatabaseReference? = null
    private var transListener: ValueEventListener? = null

    init {
        loadData()
    }

    private fun loadData() {
        val uid = userId ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        // Load Balance
        balanceListener?.let { balanceRef?.removeEventListener(it) }
        balanceRef = database.getReference("users").child(uid).child("walletBalance")
        balanceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balance = snapshot.getValue(Double::class.java) ?: 0.0
                _uiState.value = _uiState.value.copy(
                    balance = String.format(Locale.getDefault(), "R%.2f", balance),
                    balanceValue = balance
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        balanceListener?.let { balanceRef?.addValueEventListener(it) }

        // Load Transactions
        transListener?.let { transRef?.removeEventListener(it) }
        transRef = database.getReference("transactions").child(uid)
        transListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Transaction>()
                for (ds in snapshot.children) {
                    ds.getValue(Transaction::class.java)?.let { list.add(it) }
                }
                _uiState.value = _uiState.value.copy(
                    transactions = list.sortedByDescending { it.getTimestamp() ?: 0L },
                    isLoading = false
                )
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
        transListener?.let { transRef?.addValueEventListener(it) }
    }

    fun topUp(amount: Double) {
        val uid = userId ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        // Instead of adding balance immediately, we create a temporary "Top Up" booking/ID
        // and request a Payfast checkout URL from the backend.
        viewModelScope.launch {
            try {
                val tempId = "TOPUP_${uid}_${System.currentTimeMillis()}"
                
                // Create a generic booking object for PayfastViewModel logic or call repo directly
                // For simplicity, we'll assume the backend /create-checkout handles generic IDs
                // In a real app, you might have a /create-topup endpoint, but we'll adapt the existing one.
                
                // We'll trigger the Payfast checkout via the API
                val userSnapshot = database.getReference("users").child(uid).get().await()
                val name = userSnapshot.child("name").getValue(String::class.java) ?: "User"
                val email = userSnapshot.child("email").getValue(String::class.java) ?: ""

                val request = com.example.hustlefix.data.PayfastRequest(
                    merchantId = "10053500",
                    merchantKey = "s7dtvpr5uallq",
                    returnUrl = "https://hustlefix.onrender.com/api/payments/success",
                    cancelUrl = "https://hustlefix.onrender.com/api/payments/cancel",
                    notifyUrl = "https://hustlefix.onrender.com/api/payments/payfast-itn",
                    firstName = name.split(" ").firstOrNull() ?: "User",
                    lastName = if (name.contains(" ")) name.split(" ").last() else "TopUp",
                    email = email,
                    mPaymentId = tempId,
                    amount = String.format(java.util.Locale.getDefault(), "%.2f", amount),
                    itemName = "Wallet Top Up"
                )

                // Note: We need a repository here. To keep it clean, we'll assume the 
                // NavGraph will handle the navigation if we just expose the intent.
                // However, I'll update the state so the UI can navigate.
                
                val api = com.example.hustlefix.ApiClient.getClient().create(com.example.hustlefix.data.PayfastApi::class.java)
                val response = api.createCheckout(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        topUpCheckoutUrl = response.body()?.checkoutUrl,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(error = "Failed to start payment", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun clearTopUpUrl() {
        _uiState.value = _uiState.value.copy(topUpCheckoutUrl = null)
    }

    fun requestWithdrawal(amount: Double, bankName: String, accHolder: String, accNumber: String, branchCode: String) {
        val uid = userId ?: return
        if (amount <= 0 || amount > _uiState.value.balanceValue) {
            _uiState.value = _uiState.value.copy(error = "Invalid amount or insufficient balance")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        val ref = database.getReference("withdrawal_requests")
        val id = ref.push().key ?: return

        val request = mapOf(
            "id" to id,
            "userId" to uid,
            "amount" to amount,
            "bankName" to bankName,
            "accountHolder" to accHolder,
            "accountNumber" to accNumber,
            "branchCode" to branchCode,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )

        ref.child(id).setValue(request).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Deduct balance immediately for the user (optional, depends on policy)
                val userRef = database.getReference("users").child(uid)
                userRef.child("walletBalance").setValue(_uiState.value.balanceValue - amount)
                recordTransaction("Withdrawal Request", -amount)
                _uiState.value = _uiState.value.copy(isLoading = false, isWithdrawalSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Request failed")
            }
        }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(isWithdrawalSuccess = false, error = null)
    }

    private fun recordTransaction(type: String, amount: Double) {
        val uid = userId ?: return
        val tRef = database.getReference("transactions").child(uid)
        val id = tRef.push().key ?: return

        val trans = mapOf(
            "id" to id,
            "type" to type,
            "amount" to amount,
            "timestamp" to System.currentTimeMillis()
        )
        tRef.child(id).setValue(trans)
    }

    override fun onCleared() {
        super.onCleared()
        balanceListener?.let { balanceRef?.removeEventListener(it) }
        transListener?.let { transRef?.removeEventListener(it) }
    }
}
