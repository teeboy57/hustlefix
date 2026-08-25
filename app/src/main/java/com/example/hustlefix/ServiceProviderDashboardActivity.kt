package com.example.hustlefix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.hustlefix.ui.screens.ServiceProviderDashboardScreen
import com.example.hustlefix.ui.theme.HustleFixTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class ServiceProviderDashboardActivity : ComponentActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageManager.applyLanguage(this)
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser?.uid ?: return

        setContent {
            HustleFixTheme {
                var businessName by remember { mutableStateOf("Hustler") }
                var totalEarnings by remember { mutableStateOf("R0.00") }
                var totalSkills by remember { mutableIntStateOf(0) }
                var totalJobs by remember { mutableIntStateOf(0) }
                var averageRating by remember { mutableStateOf("5.0") }
                val recentOrders = remember { mutableStateListOf<Booking>() }

                LaunchedEffect(Unit) {
                    val db = FirebaseDatabase.getInstance()
                    
                    // Get Business Name
                    db.getReference("users").child(currentUserId).child("name").get()
                        .addOnSuccessListener { businessName = "${it.value.toString()}'s Business" }

                    // Get Skills Count
                    db.getReference("services").orderByChild("serviceProviderId").equalTo(currentUserId)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                totalSkills = snapshot.childrenCount.toInt()
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })

                    // Get Jobs & Revenue
                    db.getReference("bookings").orderByChild("serviceProviderId").equalTo(currentUserId)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var jobs = 0
                                var revenue = 0.0
                                var ratingsSum = 0.0
                                var ratingsCount = 0
                                recentOrders.clear()
                                
                                for (ds in snapshot.children) {
                                    val b = ds.getValue(Booking::class.java)
                                    if (b != null) {
                                        jobs++
                                        if (b.status == "completed") revenue += b.price
                                        if (b.rating > 0) {
                                            ratingsSum += b.rating
                                            ratingsCount++
                                        }
                                        recentOrders.add(b)
                                    }
                                }
                                totalJobs = jobs
                                totalEarnings = "R${String.format(Locale.getDefault(), "%.2f", revenue)}"
                                if (ratingsCount > 0) {
                                    averageRating = String.format(Locale.getDefault(), "%.1f", ratingsSum / ratingsCount)
                                }
                                recentOrders.sortByDescending { it.timestamp }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                ServiceProviderDashboardScreen(
                    businessName = businessName,
                    totalEarnings = totalEarnings,
                    totalSkills = totalSkills,
                    totalJobs = totalJobs,
                    averageRating = averageRating,
                    recentOrders = recentOrders,
                    onQuickActionClick = { handleAction(it) },
                    onBookingClick = { openBookingDetail(it) },
                    onMenuClick = { /* Drawer implementation */ }
                )
            }
        }
    }

    private fun handleAction(action: String) {
        when(action) {
            "new" -> startActivity(Intent(this, PostServiceActivity::class.java))
            "work" -> startActivity(Intent(this, MyServicesActivity::class.java))
        }
    }

    private fun openBookingDetail(booking: Booking) {
        val intent = Intent(this, BookingDetailActivity::class.java)
        intent.putExtra("bookingId", booking.bookingId)
        intent.putExtra("isServiceProvider", true)
        startActivity(intent)
    }
}
