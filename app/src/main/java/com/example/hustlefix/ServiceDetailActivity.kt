package com.example.hustlefix

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.hustlefix.ui.screens.ServiceDetailScreen
import com.example.hustlefix.ui.theme.HustleFixTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ServiceDetailActivity : ComponentActivity() {
    private var serviceId: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        serviceId = intent.getStringExtra("serviceId")
        currentUserId = FirebaseAuth.getInstance().uid

        setContent {
            HustleFixTheme {
                var service by remember { mutableStateOf<Service?>(null) }
                var isLoading by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    if (serviceId != null) {
                        FirebaseDatabase.getInstance().getReference("services").child(serviceId!!)
                            .get().addOnSuccessListener { snapshot ->
                                service = snapshot.getValue(Service::class.java)
                            }
                    }
                }

                ServiceDetailScreen(
                    service = service,
                    onBackClick = { finish() },
                    onBookNowClick = { 
                        if (service != null) {
                            isLoading = true
                            bookNow(service!!) {
                                isLoading = false
                            }
                        }
                    },
                    onSaveClick = { saveService(service) },
                    onProviderClick = { 
                        val intent = Intent(this, WorkerProfileActivity::class.java)
                        intent.putExtra("worker_id", service?.getserviceProviderId())
                        startActivity(intent)
                    },
                    isLoading = isLoading
                )
            }
        }
    }

    private fun saveService(service: Service?) {
        if (currentUserId == null || service == null) return
        FirebaseDatabase.getInstance().getReference("saved_services").child(currentUserId!!)
            .child(service.serviceId!!).setValue(service.serviceId)
            .addOnSuccessListener { Toast.makeText(this, "Service Saved!", Toast.LENGTH_SHORT).show() }
    }

    private fun bookNow(service: Service, onComplete: () -> Unit) {
        val uid = currentUserId ?: return
        val db = FirebaseDatabase.getInstance()
        val bookingId = db.getReference("bookings").push().key ?: return

        val booking = mutableMapOf<String, Any?>(
            "bookingId" to bookingId,
            "serviceId" to service.serviceId,
            "serviceTitle" to service.title,
            "price" to service.price,
            "clientId" to uid,
            "serviceProviderId" to service.getserviceProviderId(),
            "serviceProviderName" to service.getserviceProviderName(),
            "status" to "pending",
            "timestamp" to System.currentTimeMillis(),
            "paymentStatus" to "UNPAID"
        )

        db.getReference("bookings").child(bookingId).setValue(booking)
            .addOnSuccessListener {
                onComplete()
                Toast.makeText(this, "Booking request sent!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                onComplete()
                Toast.makeText(this, "Booking failed", Toast.LENGTH_SHORT).show()
            }
    }
}
