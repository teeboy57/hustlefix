package com.example.hustlefix.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VerificationUiState(
    val idImageUri: Uri? = null,
    val selfieImageUri: Uri? = null,
    val certImageUri: Uri? = null,
    val remoteIdUrl: String? = null,
    val remoteSelfieUrl: String? = null,
    val remoteCertUrl: String? = null,
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

    private var statusListener: ValueEventListener? = null

    init {
        observeVerificationStatus()
    }

    private fun observeVerificationStatus() {
        val uid = userId ?: return
        val userRef = database.getReference("users").child(uid)
        
        statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("verificationStatus").getValue(String::class.java) ?: "unverified"
                val reason = snapshot.child("rejectionReason").getValue(String::class.java)
                val idUrl = snapshot.child("idDocumentUrl").getValue(String::class.java)
                val selfieUrl = snapshot.child("selfieUrl").getValue(String::class.java)
                val certUrl = snapshot.child("certificateUrl").getValue(String::class.java)
                
                _uiState.value = _uiState.value.copy(
                    currentStatus = status, 
                    rejectionReason = reason,
                    remoteIdUrl = idUrl,
                    remoteSelfieUrl = selfieUrl,
                    remoteCertUrl = certUrl
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        
        userRef.addValueEventListener(statusListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        val uid = userId ?: return
        statusListener?.let {
            database.getReference("users").child(uid).removeEventListener(it)
        }
    }

    fun onIdImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(idImageUri = uri)
    }

    fun onSelfieImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selfieImageUri = uri)
    }

    fun onCertImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(certImageUri = uri)
    }

    fun deleteDocument(type: String) {
        val uid = userId ?: return
        val path = when (type) {
            "id" -> "idDocumentUrl"
            "selfie" -> "selfieUrl"
            else -> "certificateUrl"
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        val updates = mutableMapOf<String, Any?>(path to null)
        if (type == "id" || type == "selfie") {
            updates["verificationStatus"] = "unverified"
        }
        
        database.getReference("users").child(uid).updateChildren(updates)
            .addOnCompleteListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
    }

    fun submitVerification() {
        val uid = userId ?: return
        val idUri = _uiState.value.idImageUri
        val selfieUri = _uiState.value.selfieImageUri

        if (idUri == null && _uiState.value.remoteIdUrl == null) {
            _uiState.value = _uiState.value.copy(error = "Identification photo is required")
            return
        }
        if (selfieUri == null && _uiState.value.remoteSelfieUrl == null) {
            _uiState.value = _uiState.value.copy(error = "Selfie / Face verification photo is required")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        val context = FirebaseApp.getInstance().applicationContext

        val proceedWithUpload = {
            val uploadId: ((String?) -> Unit) -> Unit = { onDone ->
                if (idUri != null) uploadToCloudinary(uid, idUri, "id_document", onDone)
                else onDone(_uiState.value.remoteIdUrl)
            }
            val uploadSelfie: ((String?) -> Unit) -> Unit = { onDone ->
                if (selfieUri != null) uploadToCloudinary(uid, selfieUri, "selfie_document", onDone)
                else onDone(_uiState.value.remoteSelfieUrl)
            }
            val uploadCert: ((String?) -> Unit) -> Unit = { onDone ->
                val certUri = _uiState.value.certImageUri
                if (certUri != null) uploadToCloudinary(uid, certUri, "cert_document", onDone)
                else onDone(_uiState.value.remoteCertUrl)
            }

            uploadId { idUrl ->
                if (idUrl != null) {
                    uploadSelfie { selfieUrl ->
                        if (selfieUrl != null) {
                            uploadCert { certUrl ->
                                updateUserStatus(uid, idUrl, selfieUrl, certUrl)
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to upload selfie")
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to upload ID")
                }
            }
        }

        if (idUri != null && selfieUri != null) {
            try {
                val idImage = InputImage.fromFilePath(context, idUri)
                val selfieImage = InputImage.fromFilePath(context, selfieUri)
                
                val faceOptions = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .build()
                val faceDetector = FaceDetection.getClient(faceOptions)
                val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                // 1. Strictly verify ID document contains text (document validation)
                textRecognizer.process(idImage).addOnSuccessListener { visionText ->
                    if (visionText.text.length < 5) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Invalid Document: The uploaded image does not appear to be a valid Identity Document."
                        )
                        return@addOnSuccessListener
                    }

                    // 2. Verify face present in ID document
                    faceDetector.process(idImage).addOnSuccessListener { idFaces ->
                        if (idFaces.isEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Invalid Document: No face detected in the ID document. Please upload a clear photo of your ID."
                            )
                        } else {
                            // 3. Verify face present in Selfie
                            faceDetector.process(selfieImage).addOnSuccessListener { selfieFaces ->
                                if (selfieFaces.isEmpty()) {
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        error = "Selfie Error: No face detected in the selfie. Please take a clear photo of your face."
                                    )
                                } else {
                                    // 4. Valid ID text, ID face, and Selfie face detected -> Securely upload and submit for Admin Review ("pending")
                                    proceedWithUpload()
                                }
                            }.addOnFailureListener {
                                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to process selfie face.")
                            }
                        }
                    }.addOnFailureListener {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to process ID face.")
                    }
                }.addOnFailureListener {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to process ID document text. Please upload a clearer image."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Verification processing error.")
            }
        } else {
            proceedWithUpload()
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

    private fun updateUserStatus(uid: String, idUrl: String, selfieUrl: String, certUrl: String?) {
        val updates = hashMapOf<String, Any?>(
            "verificationStatus" to "pending",
            "idDocumentUrl" to idUrl,
            "selfieUrl" to selfieUrl,
            "certificateUrl" to certUrl,
            "verificationSubmittedAt" to System.currentTimeMillis(),
            "rejectionReason" to null
        )

        database.getReference("users").child(uid).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, 
                        isSuccess = true, 
                        currentStatus = "pending"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to update verification status")
                }
            }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(isSuccess = false, error = null)
    }
}
