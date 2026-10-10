package com.example.hustlefix.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Booking
import com.example.hustlefix.Quote
import com.example.hustlefix.Rating
import com.example.hustlefix.Service
import com.example.hustlefix.data.JobRepository
import com.example.hustlefix.data.RatingRepository
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class BookingDetailUiState(
    val booking: Booking? = null,
    val service: Service? = null,
    val quote: Quote? = null,
    val walletBalance: Double = 0.0,
    val isServiceProvider: Boolean = false,
    val isLoading: Boolean = false,
    val isVerifyingPayment: Boolean = false,
    val error: String? = null,
    val isUpdateSuccess: Boolean = false
)

class BookingDetailViewModel(
    private val repository: JobRepository = JobRepository(),
    private val ratingRepository: RatingRepository = RatingRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookingDetailUiState())
    val uiState: StateFlow<BookingDetailUiState> = _uiState.asStateFlow()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: com.google.firebase.auth.FirebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private var bookingRef: DatabaseReference? = null
    private var bookingListener: ValueEventListener? = null

    fun loadBooking(bookingId: String) {
        if (bookingId.isEmpty()) return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        loadQuote(bookingId)

        // Load Wallet Balance
        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.getReference("users").child(uid).child("walletBalance").get().addOnSuccessListener {
                _uiState.value = _uiState.value.copy(walletBalance = it.getValue(Double::class.java) ?: 0.0)
            }
        }

        // Clear existing listener
        bookingRef?.let { ref -> bookingListener?.let { ref.removeEventListener(it) } }

        bookingRef = database.getReference("bookings").child(bookingId)
        bookingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val booking = snapshot.getValue(Booking::class.java)?.apply {
                        setBookingId(snapshot.key ?: "")
                    }
                    if (booking != null) {
                        val isNowPaid = booking.paymentStatus == "PAID"
                        val currentUserId = auth.currentUser?.uid
                        val isProvider = currentUserId != null && currentUserId == booking.workerId
                        
                        _uiState.value = _uiState.value.copy(
                            booking = booking,
                            isServiceProvider = isProvider,
                            isVerifyingPayment = _uiState.value.isVerifyingPayment && !isNowPaid
                        )
                        
                        val sid = booking.jobId
                        if (!sid.isNullOrEmpty() && (booking.jobId.isNullOrEmpty() || _uiState.value.service == null)) {
                            fetchServiceDetails(sid)
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Booking not found")
                    }
                } catch (e: Exception) {
                    Log.e("HustleFix", "Error parsing booking: ${e.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Data loading error")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
            }
        }
        bookingListener?.let { bookingRef?.addValueEventListener(it) }
    }

    fun fetchServiceDetails(serviceId: String) {
        database.getReference("services").child(serviceId).get().addOnSuccessListener { snapshot ->
            val service = snapshot.getValue(Service::class.java)
            if (service != null) {
                _uiState.value = _uiState.value.copy(service = service, isLoading = false)
            } else {
                // Fallback: Create a mock service using booking data if service node is gone
                val booking = _uiState.value.booking
                val fallback = Service().apply {
                    setServiceId(serviceId)
                    setTitle(booking?.serviceTitle ?: "Professional Service")
                    setPrice(booking?.amount ?: 0.0)
                }
                _uiState.value = _uiState.value.copy(service = fallback, isLoading = false)
            }
        }.addOnFailureListener {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun loadQuote(bookingId: String) {
        database.getReference("bookings").child(bookingId).child("quote").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val quote = snapshot.getValue(Quote::class.java)
                _uiState.value = _uiState.value.copy(quote = quote)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun submitQuoteForBooking(amount: Double, message: String) {
        val booking = _uiState.value.booking ?: return
        val uid = auth.currentUser?.uid ?: return
        val quote = Quote(
            booking.bookingId,
            booking.getServiceTitleCompatibility(),
            uid,
            booking.workerName ?: "Provider",
            booking.clientId ?: "",
            booking.clientName ?: "Client",
            message,
            amount,
            "Standard On-Site Quote"
        )
        quote.id = booking.bookingId

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "quote" to quote.toMap(),
                    "status" to "quoted"
                )
                database.getReference("bookings").child(booking.bookingId).updateChildren(updates).await()
                _uiState.value = _uiState.value.copy(isLoading = false, isUpdateSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun acceptQuoteForBooking() {
        val booking = _uiState.value.booking ?: return
        val quote = _uiState.value.quote ?: return

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                database.getReference("bookings").child(booking.bookingId).updateChildren(mapOf(
                    "status" to "in_progress",
                    "quote/status" to "accepted"
                )).await()

                _uiState.value = _uiState.value.copy(isLoading = false, isUpdateSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun payQuoteWithWallet() {
        val booking = _uiState.value.booking ?: return
        val quote = _uiState.value.quote ?: return
        val uid = auth.currentUser?.uid ?: return
        val quoteAmount = quote.amount

        if (_uiState.value.walletBalance < quoteAmount) {
            _uiState.value = _uiState.value.copy(error = "Insufficient wallet balance to settle quote")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val platformFeeRate = 0.10
                val platformFee = quoteAmount * platformFeeRate
                val workerEarnings = quoteAmount - platformFee

                // 1. Deduct quote amount from client wallet
                val userRef = database.getReference("users").child(uid)
                val newBalance = _uiState.value.walletBalance - quoteAmount
                userRef.child("walletBalance").setValue(newBalance).await()

                // 2. Log Transaction
                val tRef = database.getReference("transactions").child(uid).push()
                tRef.setValue(mapOf(
                    "id" to tRef.key,
                    "type" to "Quote Settlement Payment",
                    "amount" to -quoteAmount,
                    "timestamp" to System.currentTimeMillis()
                )).await()

                // 3. Move Commission to Admin Revenue
                val revenueRef = database.getReference("admin_revenue").push()
                revenueRef.setValue(mapOf(
                    "bookingId" to booking.bookingId,
                    "quoteAmount" to quoteAmount,
                    "commission" to platformFee,
                    "timestamp" to System.currentTimeMillis()
                )).await()

                // 4. Update Admin Wallet
                database.getReference("admin_wallet").child("balance").runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentBalance = currentData.getValue(Double::class.java) ?: 0.0
                        currentData.value = currentBalance + platformFee
                        return Transaction.success(currentData)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
                })

                // 5. Update Quote Payment Status and generate new completion code inside booking
                val newCode = String.format(Locale.getDefault(), "%04d", (Math.random() * 9000).toInt() + 1000)
                database.getReference("bookings").child(booking.bookingId).updateChildren(mapOf(
                    "quote/paymentStatus" to "PAID",
                    "quote/status" to "settled",
                    "completionCode" to newCode
                )).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    walletBalance = newBalance,
                    isUpdateSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun startPaymentVerification() {
        _uiState.value = _uiState.value.copy(isVerifyingPayment = true)
    }

    fun payWithWallet() {
        val booking = _uiState.value.booking ?: return
        val uid = auth.currentUser?.uid ?: return
        val amount = booking.amount ?: 0.0
        
        if (_uiState.value.walletBalance < amount) {
            _uiState.value = _uiState.value.copy(error = "Insufficient wallet balance")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val platformFeeRate = 0.10 // 10% Commission
                val platformFee = amount * platformFeeRate
                val workerEarnings = amount - platformFee

                // 1. Deduct full amount from client wallet
                val userRef = database.getReference("users").child(uid)
                val newBalance = _uiState.value.walletBalance - amount
                userRef.child("walletBalance").setValue(newBalance).await()

                // 2. Log Transaction for client
                val tRef = database.getReference("transactions").child(uid).push()
                tRef.setValue(mapOf(
                    "id" to tRef.key,
                    "type" to "Booking Payment",
                    "amount" to -amount,
                    "timestamp" to System.currentTimeMillis()
                )).await()

                // 3. Move Commission to Admin Revenue Node
                val revenueRef = database.getReference("admin_revenue").push()
                revenueRef.setValue(mapOf(
                    "bookingId" to booking.bookingId,
                    "totalAmount" to amount,
                    "commission" to platformFee,
                    "timestamp" to System.currentTimeMillis()
                )).await()

                // 4. Update Admin Global Wallet Balance
                database.getReference("admin_wallet").child("balance").runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentBalance = currentData.getValue(Double::class.java) ?: 0.0
                        currentData.value = currentBalance + platformFee
                        return Transaction.success(currentData)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
                })

                // 5. Update Booking with payout details
                database.getReference("bookings").child(booking.bookingId).updateChildren(mapOf(
                    "paymentStatus" to "PAID",
                    "paidAt" to System.currentTimeMillis(),
                    "paymentMethod" to "wallet",
                    "platformFee" to platformFee,
                    "workerEarnings" to workerEarnings
                )).await()

                // Log Activity
                com.example.hustlefix.util.ActivityLogger.log(uid, "System", "PAYMENT_PROCESSED", "R$amount paid for booking ${booking.bookingId}")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    walletBalance = newBalance,
                    isUpdateSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateStatus(status: String, inputCode: String? = null) {
        val booking = _uiState.value.booking ?: return
        val currentUserId = auth.currentUser?.uid ?: return
        
        // Security Check: Only verify code if completing the job
        if (status == "completed" && inputCode != null) {
            if (booking.completionCode != inputCode) {
                _uiState.value = _uiState.value.copy(error = "Invalid Completion Code. Please ask the Client for the 4-digit code.")
                return
            }
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                // 1. AUTO-REFUND AUTOMATION
                // If a Pro cancels a PAID booking, refund the client instantly
                if (status == "cancelled" && booking.paymentStatus == "PAID") {
                    val clientId = booking.clientId
                    val amount = booking.amount ?: 0.0
                    
                    if (clientId != null && amount > 0) {
                        // Refund to Client Wallet
                        database.getReference("users").child(clientId).child("walletBalance")
                            .runTransaction(object : Transaction.Handler {
                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                    val current = currentData.getValue(Double::class.java) ?: 0.0
                                    currentData.value = current + amount
                                    return Transaction.success(currentData)
                                }
                                override fun onComplete(e: DatabaseError?, b: Boolean, s: DataSnapshot?) {}
                            })
                        
                        // Log Refund Transaction
                        val refundRef = database.getReference("transactions").child(clientId).push()
                        refundRef.setValue(mapOf(
                            "id" to refundRef.key,
                            "type" to "Auto-Refund",
                            "amount" to amount,
                            "details" to "Pro cancelled job: ${booking.serviceTitle}",
                            "timestamp" to System.currentTimeMillis()
                        ))
                        
                        // Update Payment Status
                        database.getReference("bookings").child(booking.bookingId).child("paymentStatus").setValue("REFUNDED")
                        
                        // Deduct from Admin Wallet (if 10% was already moved, we take the whole amount back)
                        database.getReference("admin_wallet").child("balance").runTransaction(object : Transaction.Handler {
                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                val current = currentData.getValue(Double::class.java) ?: 0.0
                                val fee = amount * 0.10
                                currentData.value = current - fee
                                return Transaction.success(currentData)
                            }
                            override fun onComplete(e: DatabaseError?, b: Boolean, s: DataSnapshot?) {}
                        })
                    }
                }

                // 1.5. PAYOUT TO WORKER ON JOB COMPLETION
                if (status == "completed") {
                    val workerId = booking.workerId
                    val bookingAmount = booking.amount ?: 0.0
                    val quoteAmount = _uiState.value.quote?.amount ?: 0.0
                    val totalAmount = bookingAmount + quoteAmount
                    
                    if (workerId != null && totalAmount > 0.0) {
                        val platformFee = totalAmount * 0.10
                        val workerEarnings = totalAmount - platformFee

                        // Credit worker wallet
                        database.getReference("users").child(workerId).child("walletBalance")
                            .runTransaction(object : Transaction.Handler {
                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                    val current = currentData.getValue(Double::class.java) ?: 0.0
                                    currentData.value = current + workerEarnings
                                    return Transaction.success(currentData)
                                }
                                override fun onComplete(e: DatabaseError?, b: Boolean, s: DataSnapshot?) {}
                            })

                        // Log Worker Payout Transaction
                        val payoutRef = database.getReference("transactions").child(workerId).push()
                        payoutRef.setValue(mapOf(
                            "id" to payoutRef.key,
                            "type" to "Job Payout",
                            "amount" to workerEarnings,
                            "details" to "Completed job: ${booking.serviceTitle ?: "Service"}",
                            "timestamp" to System.currentTimeMillis()
                        ))
                    }
                }

                // 2. Perform regular status update
                val result = repository.updateBookingStatus(booking, status)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(isLoading = false, isUpdateSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun submitRating(score: Float, comment: String, isAnonymous: Boolean) {
        val booking = _uiState.value.booking ?: return
        val currentUserId = auth.currentUser?.uid ?: return
        val currentUserName = auth.currentUser?.displayName ?: "User"
        
        val rating = Rating(
            booking.jobId,
            booking.getServiceTitle(),
            currentUserId,
            currentUserName,
            booking.workerId,
            booking.workerName,
            score,
            comment,
            isAnonymous
        )
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = ratingRepository.submitRating(rating)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isUpdateSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun submitDispute(reason: String) {
        val booking = _uiState.value.booking ?: return
        val uid = auth.currentUser?.uid ?: return
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val disputeRef = database.getReference("disputes").push()
                val id = disputeRef.key ?: return@launch
                
                val dispute = mapOf(
                    "id" to id,
                    "bookingId" to booking.bookingId,
                    "reporterId" to uid,
                    "reason" to reason,
                    "status" to "pending",
                    "timestamp" to System.currentTimeMillis()
                )
                disputeRef.setValue(dispute).await()
                
                // Mark booking as disputed to freeze actions
                database.getReference("bookings").child(booking.bookingId)
                    .child("status").setValue("disputed").await()
                
                // Log for Admin Website
                com.example.hustlefix.util.ActivityLogger.log(uid, "User", "DISPUTE_OPENED", "Dispute raised for booking ${booking.bookingId}")

                _uiState.value = _uiState.value.copy(isLoading = false, isUpdateSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(isUpdateSuccess = false, error = null)
    }

    override fun onCleared() {
        super.onCleared()
        bookingRef?.let { ref -> bookingListener?.let { ref.removeEventListener(it) } }
    }
}
