package com.example.hustlefix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.hustlefix.ui.screens.ClientDashboardScreen
import com.example.hustlefix.ui.theme.HustleFixTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ClientDashboardActivity : ComponentActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageManager.applyLanguage(this)
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser?.uid ?: return

        setContent {
            HustleFixTheme {
                var clientName by remember { mutableStateOf("Client") }
                var totalBookings by remember { mutableIntStateOf(0) }
                var activeBookings by remember { mutableIntStateOf(0) }
                var completedBookings by remember { mutableIntStateOf(0) }
                val recentBookings = remember { mutableStateListOf<Booking>() }

                LaunchedEffect(Unit) {
                    val db = FirebaseDatabase.getInstance()
                    
                    // Get name
                    db.getReference("users").child(currentUserId).child("name").get()
                        .addOnSuccessListener { clientName = it.value.toString() }

                    // Get stats and recent
                    db.getReference("bookings").orderByChild("clientId").equalTo(currentUserId)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var total = 0
                                var active = 0
                                var done = 0
                                recentBookings.clear()
                                for (ds in snapshot.children) {
                                    val b = ds.getValue(Booking::class.java)
                                    if (b != null) {
                                        total++
                                        if (b.status == "completed") done++
                                        else if (b.status != "cancelled") active++
                                        recentBookings.add(b)
                                    }
                                }
                                totalBookings = total
                                activeBookings = active
                                completedBookings = done
                                recentBookings.sortByDescending { it.timestamp }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                ClientDashboardScreen(
                    clientName = clientName,
                    totalBookings = totalBookings,
                    activeBookings = activeBookings,
                    completedBookings = completedBookings,
                    recentBookings = recentBookings,
                    onCategoryClick = { openFindWithCategory(it) },
                    onQuickActionClick = { handleAction(it) },
                    onBookingClick = { openBookingDetail(it) },
                    onMenuClick = { /* Drawer would go here, for now use standard activity drawer if needed or just skip as requested for stunning UI */ }
                )
            }
        }
    }

    private fun openFindWithCategory(cat: String) {
        val intent = Intent(this, FindServicesActivity::class.java)
        if (cat != "All") intent.putExtra("category", cat)
        startActivity(intent)
    }

    private fun handleAction(action: String) {
        when(action) {
            "find" -> startActivity(Intent(this, FindServicesActivity::class.java))
            "bookings" -> startActivity(Intent(this, MyBookingsActivity::class.java))
            "saved" -> startActivity(Intent(this, SavedServicesActivity::class.java))
            "messages" -> startActivity(Intent(this, ChatListActivity::class.java))
        }
    }

    private fun openBookingDetail(booking: Booking) {
        val intent = Intent(this, BookingDetailActivity::class.java)
        intent.putExtra("bookingId", booking.bookingId)
        startActivity(intent)
    }
}
