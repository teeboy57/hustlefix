package com.example.hustlefix.data

import com.example.hustlefix.Rating
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RatingRepository {
    private val database = FirebaseDatabase.getInstance()
    private val ratingsRef = database.getReference("ratings")
    private val usersRef = database.getReference("users")

    suspend fun submitRating(rating: Rating): Result<Unit> {
        return try {
            val ref = ratingsRef.push()
            val id = ref.key ?: throw Exception("Failed to get key")
            rating.id = id
            ref.setValue(rating).await()
            
            // Update user's average rating
            updateUserRating(rating.ratedId, rating.rating)

            // Update service's average rating if applicable
            if (!rating.jobId.isNullOrEmpty()) {
                updateServiceRating(rating.jobId, rating.rating)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateUserRating(userId: String, newRating: Float) {
        val userRef = usersRef.child(userId)
        val snapshot = userRef.get().await()
        
        val currentRating = snapshot.child("rating").getValue(Double::class.java) ?: 0.0
        val ratingCount = snapshot.child("ratingCount").getValue(Int::class.java) ?: 0
        
        val newCount = ratingCount + 1
        val newAvg = ((currentRating * ratingCount) + newRating) / newCount
        
        userRef.child("rating").setValue(newAvg)
        userRef.child("ratingCount").setValue(newCount)
    }

    private suspend fun updateServiceRating(serviceId: String, newRating: Float) {
        val serviceRef = database.getReference("services").child(serviceId)
        val snapshot = serviceRef.get().await()
        if (!snapshot.exists()) return

        val currentRating = snapshot.child("averageRating").getValue(Double::class.java) ?: 0.0
        val bookingsCount = snapshot.child("bookingsCount").getValue(Int::class.java) ?: 0
        
        // We use bookingsCount as the denominator, assuming each rating corresponds to a booking
        val newCount = bookingsCount + 1
        val newAvg = ((currentRating * bookingsCount) + newRating) / newCount
        
        serviceRef.child("averageRating").setValue(newAvg)
        serviceRef.child("bookingsCount").setValue(newCount)
    }
}
