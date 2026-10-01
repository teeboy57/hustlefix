package com.example.hustlefix.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsHelper {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun logEvent(name: String, params: Bundle? = null) {
        firebaseAnalytics?.logEvent(name, params)
    }

    fun logJobBooked(serviceId: String, serviceTitle: String, amount: Double) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, serviceId)
            putString(FirebaseAnalytics.Param.ITEM_NAME, serviceTitle)
            putDouble(FirebaseAnalytics.Param.VALUE, amount)
            putString(FirebaseAnalytics.Param.CURRENCY, "ZAR")
        }
        logEvent("job_booked", bundle)
    }

    fun logEmergencyTriggered(urgency: String, location: String) {
        val bundle = Bundle().apply {
            putString("urgency_level", urgency)
            putString("location_captured", location)
        }
        logEvent("emergency_triggered", bundle)
    }

    fun logSearchQuery(query: String, category: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
            putString("selected_category", category)
        }
        logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
    }
}
