package com.example.hustlefix.data

import com.example.hustlefix.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    suspend fun getUserProfile(uid: String): Result<User?> {
        return try {
            val snapshot = usersRef.child(uid).get().await()
            val user = snapshot.getValue(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isUserSuspended(uid: String): Boolean {
        return try {
            val snapshot = usersRef.child(uid).child("isSuspended").get().await()
            snapshot.getValue(Boolean::class.java) ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveUserProfile(uid: String, userMap: Map<String, Any>): Result<Unit> {
        return try {
            usersRef.child(uid).setValue(userMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        try {
            usersRef.child(uid).child("fcmToken").setValue(token).await()
        } catch (e: Exception) {
            // Log or handle error
        }
    }
}
