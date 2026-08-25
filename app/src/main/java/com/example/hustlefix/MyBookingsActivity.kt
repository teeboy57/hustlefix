package com.example.hustlefix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.hustlefix.ui.screens.MyBookingsScreen
import com.example.hustlefix.ui.theme.HustleFixTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyBookingsActivity : ComponentActivity() {
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        currentUserId = FirebaseAuth.getInstance().uid ?: return

        setContent {
            HustleFixTheme {
                val bookings = remember { mutableStateListOf<Booking>() }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    val role = SessionHelper.getRole(this@MyBookingsActivity)
                    val queryField = if (role == "service_provider") "serviceProviderId" else "clientId"
                    
                    FirebaseDatabase.getInstance().getReference("bookings")
                        .orderByChild(queryField).equalTo(currentUserId)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                bookings.clear()
                                for (ds in snapshot.children) {
                                    val b = ds.getValue(Booking::class.java)
                                    if (b != null) bookings.add(b)
                                }
                                bookings.sortByDescending { it.timestamp }
                                isLoading = false
                            }
                            override fun onCancelled(error: DatabaseError) {
                                isLoading = false
                            }
                        })
                }

                MyBookingsScreen(
                    bookings = bookings,
                    isLoading = isLoading,
                    onBookingClick = { openBookingDetail(it) },
                    onBackClick = { finish() }
                )
            }
        }
    }

    private fun openBookingDetail(booking: Booking) {
        val role = SessionHelper.getRole(this)
        val intent = Intent(this, BookingDetailActivity::class.java)
        intent.putExtra("bookingId", booking.bookingId)
        intent.putExtra("isServiceProvider", role == "service_provider")
        startActivity(intent)
    }
}
