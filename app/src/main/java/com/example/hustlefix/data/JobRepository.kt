package com.example.hustlefix.data

import com.example.hustlefix.Job
import com.example.hustlefix.Quote
import com.example.hustlefix.Booking
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JobRepository {
    private val database = FirebaseDatabase.getInstance()
    private val jobsRef = database.getReference("jobs")
    private val quotesRef = database.getReference("quotes")
    private val bookingsRef = database.getReference("bookings")

    fun getAvailableJobs(limit: Int = 50): Flow<List<Job>> = callbackFlow {
        val query = jobsRef.orderByChild("status").equalTo("open").limitToLast(limit)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobs = snapshot.children.mapNotNull { child ->
                    child.getValue(Job::class.java)?.apply { jobId = child.key ?: "" }
                }.sortedByDescending { it.getTimestamp() }
                trySend(jobs)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    fun getClientJobs(clientId: String): Flow<List<Job>> = callbackFlow {
        val query = jobsRef.orderByChild("clientId").equalTo(clientId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobs = snapshot.children.mapNotNull { child ->
                    child.getValue(Job::class.java)?.apply { jobId = child.key ?: "" }
                }
                trySend(jobs)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    fun getWorkerJobs(workerId: String): Flow<List<Job>> = callbackFlow {
        val query = jobsRef.orderByChild("workerId").equalTo(workerId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobs = snapshot.children.mapNotNull { child ->
                    child.getValue(Job::class.java)?.apply { jobId = child.key ?: "" }
                }
                trySend(jobs)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun createJob(job: Job): Result<String> {
        return try {
            val ref = jobsRef.push()
            val id = ref.key ?: throw Exception("Failed to get key")
            job.jobId = id
            ref.setValue(job).await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJobStatus(jobId: String, status: String): Result<Unit> {
        return try {
            jobsRef.child(jobId).child("status").setValue(status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(booking: Booking, newStatus: String): Result<Unit> {
        return try {
            val bookingUpdates = mutableMapOf<String, Any?>("status" to newStatus)
            
            // Sync with Job if jobId exists
            if (!booking.jobId.isNullOrEmpty()) {
                val jobStatus = when (newStatus) {
                    "confirmed" -> "in-progress"
                    "completed" -> "completed"
                    "cancelled" -> "cancelled"
                    "failed" -> "cancelled"
                    else -> null
                }
                
                if (jobStatus != null) {
                    jobsRef.child(booking.jobId).child("status").setValue(jobStatus).await()
                }
            }
            
            bookingsRef.child(booking.bookingId).updateChildren(bookingUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitQuote(quote: Quote): Result<Unit> {
        return try {
            val ref = quotesRef.child(quote.jobId).child(quote.workerId)
            ref.setValue(quote).await()
            // Increment applications count on job
            jobsRef.child(quote.jobId).child("applicationsCount").runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val count = currentData.getValue(Int::class.java) ?: 0
                    currentData.value = count + 1
                    return Transaction.success(currentData)
                }
                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
            })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getQuotesForJob(jobId: String): Flow<List<Quote>> = callbackFlow {
        val ref = quotesRef.child(jobId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val quotes = snapshot.children.mapNotNull { it.getValue(Quote::class.java) }
                trySend(quotes)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun acceptQuote(job: Job, quote: Quote): Result<Unit> {
        return try {
            // 1. Update Job
            val jobUpdates = mapOf(
                "status" to "quoted",
                "workerId" to quote.workerId,
                "workerName" to quote.workerName,
                "quotedAmount" to quote.amount
            )
            jobsRef.child(job.jobId).updateChildren(jobUpdates).await()

            // 2. Create Booking
            val bookingRef = bookingsRef.push()
            val bookingId = bookingRef.key ?: throw Exception("Failed to get booking key")
            val booking = Booking(job.jobId, job.clientId, job.clientName, quote.workerId, quote.workerName, quote.amount).apply {
                this.bookingId = bookingId
            }
            bookingRef.setValue(booking).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
