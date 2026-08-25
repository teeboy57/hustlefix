package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Job
import com.example.hustlefix.Quote
import com.example.hustlefix.data.JobRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    fun postJob(title: String, category: String, description: String, amount: Double, location: String) {
        val uid = currentUserId ?: return
        val name = auth.currentUser?.displayName ?: "Client"
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val job = Job(title, category, uid, name, location, description, amount)
            val result = repository.createJob(job)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Job posted successfully") }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun submitQuote(jobId: String, amount: Double, message: String) {
        val uid = currentUserId ?: return
        val name = auth.currentUser?.displayName ?: "Worker"
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val quote = Quote(jobId, "", uid, name, "", "", message, amount, "")
            val result = repository.submitQuote(quote)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Quote submitted successfully") }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
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

    fun updateJobStatus(jobId: String, status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.updateJobStatus(jobId, status)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Job status updated to $status") }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
