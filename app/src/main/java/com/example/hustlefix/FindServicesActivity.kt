package com.example.hustlefix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.hustlefix.ui.screens.FindServicesScreen
import com.example.hustlefix.ui.theme.HustleFixTheme
import com.google.firebase.database.*

class FindServicesActivity : ComponentActivity() {
    private lateinit var servicesRef: DatabaseReference
    private val serviceList = mutableStateListOf<Service>()
    private val filteredList = mutableStateListOf<Service>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        servicesRef = FirebaseDatabase.getInstance().getReference("services")
        val category = intent.getStringExtra("category")

        setContent {
            HustleFixTheme {
                var searchQuery by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    servicesRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            serviceList.clear()
                            for (ds in snapshot.children) {
                                val s = ds.getValue(Service::class.java)
                                if (s != null && s.status == "active") {
                                    serviceList.add(s)
                                }
                            }
                            isLoading = false
                            
                            if (!category.isNullOrEmpty()) {
                                searchQuery = category
                                applyFilter(category)
                            } else {
                                applyFilter("")
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            isLoading = false
                        }
                    })
                }

                FindServicesScreen(
                    services = filteredList,
                    isLoading = isLoading,
                    onServiceClick = { openServiceDetail(it) },
                    onSortClick = { showSortDialog() },
                    onBackClick = { finish() },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { 
                        searchQuery = it
                        applyFilter(it)
                    }
                )
            }
        }
    }

    private fun applyFilter(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(serviceList)
        } else {
            val q = query.lowercase().trim()
            filteredList.addAll(serviceList.filter { 
                it.title?.lowercase()?.contains(q) == true ||
                it.category?.lowercase()?.contains(q) == true ||
                it.getserviceProviderName()?.lowercase()?.contains(q) == true ||
                it.description?.lowercase()?.contains(q) == true
            })
        }
    }

    private fun showSortDialog() {
        val options = arrayOf("Price: Low to High", "Price: High to Low", "Newest First")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Sort By")
            .setItems(options) { _, which ->
                when(which) {
                    0 -> filteredList.sortBy { it.price }
                    1 -> filteredList.sortByDescending { it.price }
                    2 -> filteredList.sortByDescending { it.createdAt }
                }
            }
            .show()
    }

    private fun openServiceDetail(service: Service) {
        val intent = Intent(this, ServiceDetailActivity::class.java)
        intent.putExtra("serviceId", service.serviceId)
        startActivity(intent)
    }
}
