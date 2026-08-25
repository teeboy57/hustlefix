package com.example.hustlefix

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JobStatusTest {

    @Test
    fun testValidTransitions() {
        assertTrue(Job.isValidTransition("open", "quoted"))
        assertTrue(Job.isValidTransition("open", "cancelled"))
        assertTrue(Job.isValidTransition("quoted", "in-progress"))
        assertTrue(Job.isValidTransition("in-progress", "completed"))
    }

    @Test
    fun testInvalidTransitions() {
        assertFalse(Job.isValidTransition("completed", "open"))
        assertFalse(Job.isValidTransition("completed", "cancelled"))
        assertFalse(Job.isValidTransition("cancelled", "open"))
        assertFalse(Job.isValidTransition("open", "completed"))
    }
}
