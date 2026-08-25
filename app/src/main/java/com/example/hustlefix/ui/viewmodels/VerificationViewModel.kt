package com.example.hustlefix.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VerificationUiState(
    val idImageUri: Uri? = null,
    val certImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val currentStatus: String = "unverified", // unverified, pending, verified, rejected
    val rejectionReason: String? = null
)

class VerificationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid

    init {
        loadVerificationStatus()
    }

    private fun loadVerificationStatus() {
        val uid = userId ?: return
        database.getReference("users").child(uid).get()
            .addOnSuccessListener { snapshot ->
                val status = snapshot.child("verificationStatus").getValue(String::class.java) ?: "unverified"
                val reason = snapshot.child("rejectionReason").getValue(String::class.java)
                _uiState.value = _uiState.value.copy(currentStatus = status, rejectionReason = reason)
            }
    }

    fun onIdImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(idImageUri = uri)
    }

    fun onCertImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(certImageUri = uri)
    }

    fun submitVerification() {
        val uid = userId ?: return
        val idUri = _uiState.value.idImageUri
        if (idUri == null) {
            _uiState.value = _uiState.value.copy(error = "Identification photo is required")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        uploadToCloudinary(uid, idUri, "id_document") { idUrl ->
            if (idUrl != null) {
                val certUri = _uiState.value.certImageUri
                if (certUri != null) {
                    uploadToCloudinary(uid, certUri, "cert_document") { certUrl ->
                        updateUserStatus(uid, idUrl, certUrl)
                    }
                } else {
                    updateUserStatus(uid, idUrl, null)
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to upload ID")
            }
        }
    }

    private fun uploadToCloudinary(uid: String, uri: Uri, type: String, onComplete: (String?) -> Unit) {
        MediaManager.get().upload(uri)
            .unsigned("hustle_fix")
            .option("folder", "verifications/$uid")
            .option("public_id", type)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    onComplete(resultData?.get("secure_url") as? String)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onComplete(null)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun updateUserStatus(uid: String, idUrl: String, certUrl: String?) {
        val updates = hashMapOf<String, Any?>(
            "verificationStatus" to "pending",
            "idDocumentUrl" to idUrl,
            "certificateUrl" to certUrl,
            "verificationSubmittedAt" to System.currentTimeMillis(),
            "rejectionReason" to null
        )

        database.getReference("users").child(uid).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, currentStatus = "pending")
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to update status")
                }
            }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(isSuccess = false, error = null)
    }
}
