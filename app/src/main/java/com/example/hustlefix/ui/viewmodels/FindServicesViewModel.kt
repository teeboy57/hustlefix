package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Service
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
        
        servicesListener?.let { servicesRef?.removeEventListener(it) }
        
        servicesRef = database.getReference("services")
        servicesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Service>()
                for (serviceSnapshot in snapshot.children) {
                    val service = serviceSnapshot.getValue(Service::class.java)
                    if (service != null) {
                        list.add(service)
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
        servicesListener?.let { servicesRef?.addValueEventListener(it) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
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
        val query = _uiState.value.searchQuery
        val category = _uiState.value.activeCategory
        val sortMode = _uiState.value.sortMode
        val min = _uiState.value.minPrice
        val max = _uiState.value.maxPrice
        val verified = _uiState.value.onlyVerified
        
        var filtered = _uiState.value.services.filter { service ->
            val matchesQuery = (service.title ?: "").contains(query, ignoreCase = true) ||
                             (service.category ?: "").contains(query, ignoreCase = true)
            val matchesCategory = if (category == "All") true else (service.category ?: "") == category
            val matchesMin = min == null || service.price >= min
            val matchesMax = max == null || service.price <= max
            val matchesVerified = !verified || service.isProviderVerified
            
            matchesQuery && matchesCategory && matchesMin && matchesMax && matchesVerified
        }

        filtered = when (sortMode) {
            "Price Low" -> filtered.sortedBy { it.price }
            "Price High" -> filtered.sortedByDescending { it.price }
            else -> filtered.sortedByDescending { it.createdAt }
        }

        _uiState.value = _uiState.value.copy(filteredServices = filtered)
    }

    override fun onCleared() {
        super.onCleared()
        servicesListener?.let { servicesRef?.removeEventListener(it) }
    }
}
