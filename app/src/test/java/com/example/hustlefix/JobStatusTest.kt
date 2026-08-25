package com.example.hustlefix

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JobStatusTest {

    private fun isValidTransition(currentStatus: String, newStatus: String): Boolean {
        return when (currentStatus) {
            "open" -> newStatus == "cancelled" || newStatus == "quoted"
            "quoted" -> newStatus == "cancelled" || newStatus == "in-progress"
            "in-progress" -> newStatus == "cancelled" || newStatus == "completed"
            else -> false
        }
    }

    @Test
    fun testValidTransitions() {
        assertTrue(isValidTransition("open", "quoted"))
        assertTrue(isValidTransition("open", "cancelled"))
        assertTrue(isValidTransition("quoted", "in-progress"))
        assertTrue(isValidTransition("in-progress", "completed"))
    }

    @Test
    fun testInvalidTransitions() {
        assertFalse(isValidTransition("completed", "open"))
        assertFalse(isValidTransition("completed", "cancelled"))
        assertFalse(isValidTransition("cancelled", "open"))
        assertFalse(isValidTransition("open", "completed"))
    }
}
