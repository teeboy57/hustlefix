package com.example.hustlefix.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.hustlefix.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PostServiceUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class PostServiceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PostServiceUiState())
    val uiState: StateFlow<PostServiceUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    fun postService(title: String, desc: String, category: String, price: Double, imageUri: Uri?) {
        val user = auth.currentUser ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        if (imageUri != null) {
            uploadImageAndSave(user.uid, user.displayName ?: "Pro", title, desc, category, price, imageUri)
        } else {
            saveToDatabase(user.uid, user.displayName ?: "Pro", title, desc, category, price, null)
        }
    }

    private fun uploadImageAndSave(uid: String, userName: String, title: String, desc: String, category: String, price: Double, uri: Uri) {
        MediaManager.get().upload(uri)
            .unsigned("hustle_fix")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url") as? String
                    saveToDatabase(uid, userName, title, desc, category, price, url)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error?.description)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun saveToDatabase(uid: String, userName: String, title: String, desc: String, category: String, price: Double, imageUrl: String?) {
        val ref = database.getReference("services")
        val serviceId = ref.push().key ?: return
        
        val service = Service().apply {
            this.serviceId = serviceId
            this.serviceProviderId = uid
            this.serviceProviderName = userName
            this.title = title
            this.description = desc
            this.category = category
            this.price = price
            this.serviceImageUrl = imageUrl
            this.timestamp = System.currentTimeMillis()
        }

        ref.child(serviceId).setValue(service).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Database error")
            }
        }
    }
}
