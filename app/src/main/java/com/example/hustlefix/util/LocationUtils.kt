package com.example.hustlefix.util

import kotlin.math.*

object LocationUtils {
    /**
     * Calculates the distance between two points in kilometers using the Haversine formula.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun formatDistance(distance: Double): String {
        return if (distance < 1) {
            "${(distance * 1000).toInt()}m away"
        } else {
            "${String.format("%.1f", distance)}km away"
        }
    }
}
