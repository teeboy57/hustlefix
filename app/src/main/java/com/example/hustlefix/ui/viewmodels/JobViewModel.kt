package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Job
import com.example.hustlefix.Quote
import com.example.hustlefix.data.JobRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class JobUiState(
    val availableJobs: List<Job> = emptyList(),
    val myJobs: List<Job> = emptyList(),
    val quotes: List<Quote> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class JobViewModel(private val repository: JobRepository = JobRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(JobUiState())
    val uiState: StateFlow<JobUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    init {
        loadAvailableJobs()
        loadMyJobs()
    }

    private fun loadAvailableJobs() {
        viewModelScope.launch {
            repository.getAvailableJobs()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { jobs ->
                    _uiState.update { it.copy(availableJobs = jobs, isLoading = false) }
                }
        }
    }

    private fun loadMyJobs() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            repository.getClientJobs(uid)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { jobs ->
                    _uiState.update { it.copy(myJobs = jobs) }
                }
        }
    }

    fun loadQuotesForJob(jobId: String) {
        viewModelScope.launch {
            repository.getQuotesForJob(jobId)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { quotes ->
                    _uiState.update { it.copy(quotes = quotes, isLoading = false) }
                }
        }
    }

    fun postJob(title: String, category: String, description: String, amount: Double, location: String, deadline: String? = null) {
        val uid = currentUserId ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Fetch latest name from DB to avoid "Client" default
                val userSnapshot = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("users").child(uid).get().await()
                val dbName = userSnapshot.child("name").getValue(String::class.java) ?: auth.currentUser?.displayName ?: "Client"

                // Check suspension
                val isSuspended = userSnapshot.child("isSuspended").getValue(Boolean::class.java) ?: false
                
                if (isSuspended) {
                    _uiState.update { it.copy(isLoading = false, error = "Action denied. Your account is suspended.") }
                    return@launch
                }

                val job = Job(title, category, uid, dbName, location, description, amount).apply {
                    this.deadline = deadline
                }
                val result = repository.createJob(job)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Job posted successfully") }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error checking status: ${e.message}") }
            }
        }
    }

    fun submitQuote(jobId: String, amount: Double, message: String) {
        val uid = currentUserId ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Fetch latest name from DB to avoid "Worker" default
                val userSnapshot = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("users").child(uid).get().await()
                val dbName = userSnapshot.child("name").getValue(String::class.java) ?: auth.currentUser?.displayName ?: "Worker"

                // Check suspension
                val isSuspended = userSnapshot.child("isSuspended").getValue(Boolean::class.java) ?: false
                
                if (isSuspended) {
                    _uiState.update { it.copy(isLoading = false, error = "Action denied. Your account is suspended.") }
                    return@launch
                }

                val job = _uiState.value.availableJobs.find { it.jobId == jobId } 
                    ?: _uiState.value.myJobs.find { it.jobId == jobId }
                
                if (job == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Could not find job details. Please try again.") }
                    return@launch
                }

                val quote = Quote(jobId, job.title, uid, dbName, job.clientId, job.clientName, message, amount, "")
                val result = repository.submitQuote(quote)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Quote submitted successfully") }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error checking status: ${e.message}") }
            }
        }
    }

    fun acceptQuote(job: Job, quote: Quote) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.acceptQuote(job, quote)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Quote accepted. Job started.") }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun updateJobStatus(jobId: String, currentStatus: String, newStatus: String) {
        // Validation logic
        if (!Job.isValidTransition(currentStatus, newStatus)) {
            _uiState.update { it.copy(error = "Invalid status transition from $currentStatus to $newStatus") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.updateJobStatus(jobId, newStatus)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Job status updated to $newStatus") }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
