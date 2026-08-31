package com.example.hustlefix.util

import com.google.firebase.database.FirebaseDatabase

object ActivityLogger {
    private val database = FirebaseDatabase.getInstance()
    private val logRef = database.getReference("activity_log")

    fun log(userId: String, userName: String, action: String, details: String = "") {
        val entry = mutableMapOf<String, Any>(
            "userId" to userId,
            "userName" to userName,
            "action" to action,
            "details" to details,
            "timestamp" to System.currentTimeMillis()
        )
        logRef.push().setValue(entry)
    }

    fun logLogin(userId: String, userName: String, role: String) {
        log(userId, userName, "LOGIN", "User logged in as $role")
    }

    fun logBooking(userId: String, userName: String, bookingId: String, amount: Double) {
        log(userId, userName, "NEW_BOOKING", "Created booking #$bookingId for R$amount")
    }

    fun logEmergency(userId: String, userName: String, type: String) {
        log(userId, userName, "EMERGENCY_ALERT", "Triggered $type alert")
    }
}
