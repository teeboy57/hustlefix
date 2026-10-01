package com.example.hustlefix.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Service
import com.example.hustlefix.util.AnalyticsHelper
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FindServicesUiState(
    val services: List<Service> = emptyList(),
    val filteredServices: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val activeCategory: String = "All",
    val sortMode: String = "Latest", // "Latest", "Price Low", "Price High"
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val onlyVerified: Boolean = false
)

class FindServicesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FindServicesUiState())
    val uiState: StateFlow<FindServicesUiState> = _uiState.asStateFlow()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var servicesRef: DatabaseReference? = null
    private var servicesListener: ValueEventListener? = null

    init {
        loadServices()
    }

    private fun loadServices() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        servicesRef?.let { ref -> servicesListener?.let { ref.removeEventListener(it) } }
        
        servicesRef = database.getReference("services")
        servicesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Service>()
                Log.d("HustleFix", "Loaded ${snapshot.childrenCount} services from DB")
                
                for (serviceSnapshot in snapshot.children) {
                    try {
                        val service = serviceSnapshot.getValue(Service::class.java)
                        if (service != null) {
                            service.serviceId = serviceSnapshot.key ?: ""
                            list.add(service)
                        }
                    } catch (e: Exception) {
                        Log.e("HustleFix", "Error parsing service ${serviceSnapshot.key}: ${e.message}")
                    }
                }
                _uiState.value = _uiState.value.copy(
                    services = list,
                    isLoading = false,
                    isRefreshing = false
                )
                applyFilters()
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false)
            }
        }
        servicesRef?.addValueEventListener(servicesListener!!)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length >= 3) {
            AnalyticsHelper.logSearchQuery(query, _uiState.value.activeCategory)
        }
        applyFilters()
    }

    fun onCategoryChange(category: String) {
        _uiState.value = _uiState.value.copy(activeCategory = category)
        applyFilters()
    }

    fun onSortToggle() {
        val nextMode = when (_uiState.value.sortMode) {
            "Latest" -> "Price Low"
            "Price Low" -> "Price High"
            else -> "Latest"
        }
        _uiState.value = _uiState.value.copy(sortMode = nextMode)
        applyFilters()
    }

    fun onFilterChange(min: Double?, max: Double?, verified: Boolean) {
        _uiState.value = _uiState.value.copy(minPrice = min, maxPrice = max, onlyVerified = verified)
        applyFilters()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadServices()
        viewModelScope.launch {
            delay(3000)
            if (_uiState.value.isRefreshing) {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    private fun applyFilters() {
        val query = _uiState.value.searchQuery.trim()
        val category = _uiState.value.activeCategory
        val sortMode = _uiState.value.sortMode
        val min = _uiState.value.minPrice
        val max = _uiState.value.maxPrice
        val verified = _uiState.value.onlyVerified
        
        var filtered = _uiState.value.services.filter { service ->
            val titleText = service.title ?: ""
            val categoryText = service.category ?: ""
            val priceValue = service.price ?: 0.0
            
            val matchesQuery = if (query.isEmpty()) true else (
                titleText.contains(query, ignoreCase = true) ||
                categoryText.contains(query, ignoreCase = true)
            )
            val matchesCategory = if (category == "All") true else categoryText == category
            val matchesMin = min == null || priceValue >= min
            val matchesMax = max == null || priceValue <= max
            val matchesVerified = !verified || (service.verified ?: false)
            
            matchesQuery && matchesCategory && matchesMin && matchesMax && matchesVerified
        }

        filtered = when (sortMode) {
            "Price Low" -> filtered.sortedBy { it.price ?: 0.0 }
            "Price High" -> filtered.sortedByDescending { it.price ?: 0.0 }
            else -> filtered.sortedByDescending { it.createdAt ?: 0L }
        }

        _uiState.value = _uiState.value.copy(filteredServices = filtered)
    }

    override fun onCleared() {
        super.onCleared()
        servicesListener?.let { servicesRef?.removeEventListener(it) }
    }
}
