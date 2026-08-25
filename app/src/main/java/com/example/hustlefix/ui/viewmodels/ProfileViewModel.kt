package com.example.hustlefix.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val skill: String = "",
    val category: String = "",
    val about: String = "",
    val experience: Int = 0,
    val photoUrl: String? = null,
    val selectedImageUri: Uri? = null,
    val walletBalance: String = "R0.00",
    val role: String = "client",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userId: String? = auth.currentUser?.uid

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val user = auth.currentUser ?: return
        _uiState.value = _uiState.value.copy(
            name = user.displayName ?: "",
            email = user.email ?: "",
            photoUrl = user.photoUrl?.toString(),
            isLoading = true
        )

        userId?.let { uid ->
            database.getReference("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                    val loc = snapshot.child("location").getValue(String::class.java) ?: ""
                    val balance = snapshot.child("walletBalance").getValue(Double::class.java) ?: 0.0
                    val dbPhotoUrl = snapshot.child("profileImage").getValue(String::class.java)
                    val role = snapshot.child("role").getValue(String::class.java) ?: "client"
                    
                    val skill = snapshot.child("skill").getValue(String::class.java) ?: ""
                    val category = snapshot.child("category").getValue(String::class.java) ?: ""
                    val about = snapshot.child("about").getValue(String::class.java) ?: ""
                    val exp = snapshot.child("experience").getValue(Int::class.java) ?: 0

                    _uiState.value = _uiState.value.copy(
                        phone = phone,
                        location = loc,
                        skill = skill,
                        category = category,
                        about = about,
                        experience = exp,
                        role = role,
                        walletBalance = String.format(Locale.getDefault(), "R%.2f", balance),
                        photoUrl = dbPhotoUrl ?: _uiState.value.photoUrl,
                        isLoading = false
                    )
                }
                override fun onCancelled(error: DatabaseError) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            })
        }
    }

    fun onNameChange(name: String) { _uiState.value = _uiState.value.copy(name = name) }
    fun onPhoneChange(phone: String) { _uiState.value = _uiState.value.copy(phone = phone) }
    fun onLocationChange(loc: String) { _uiState.value = _uiState.value.copy(location = loc) }
    fun onSkillChange(skill: String) { _uiState.value = _uiState.value.copy(skill = skill) }
    fun onCategoryChange(cat: String) { _uiState.value = _uiState.value.copy(category = cat) }
    fun onAboutChange(about: String) { _uiState.value = _uiState.value.copy(about = about) }
    fun onExperienceChange(exp: Int) { _uiState.value = _uiState.value.copy(experience = exp) }
    fun onImageSelected(uri: Uri?) { _uiState.value = _uiState.value.copy(selectedImageUri = uri) }

    fun saveProfile() {
        val uid = userId ?: return
        val currentState = _uiState.value
        if (currentState.name.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Name is required")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Check suspension
                val snapshot = database.getReference("users").child(uid).child("isSuspended").get().await()
                if (snapshot.getValue(Boolean::class.java) == true) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Action denied. Your account is suspended.")
                    return@launch
                }

                // Update Auth Profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(currentState.name)
                    .build()
                auth.currentUser?.updateProfile(profileUpdates)?.await()

                if (currentState.selectedImageUri != null) {
                    uploadImageAndSave(uid, currentState.selectedImageUri)
                } else {
                    updateDatabase(uid, currentState.photoUrl)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun uploadImageAndSave(uid: String, uri: Uri) {
        MediaManager.get().upload(uri)
            .unsigned("hustle_fix")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val secureUrl = resultData?.get("secure_url") as? String
                    updateDatabase(uid, secureUrl)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error?.description)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun updateDatabase(uid: String, photoUrl: String?) {
        val currentState = _uiState.value
        val updates = hashMapOf<String, Any?>(
            "name" to currentState.name,
            "phone" to currentState.phone,
            "location" to currentState.location,
            "profileImage" to photoUrl
        )

        if (currentState.role == "worker") {
            updates["skill"] = currentState.skill
            updates["category"] = currentState.category
            updates["about"] = currentState.about
            updates["experience"] = currentState.experience
        }

        database.getReference("users").child(uid).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, photoUrl = photoUrl)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Database update failed")
                }
            }
    }
    
    fun clearStatus() {
        _uiState.value = _uiState.value.copy(isSuccess = false, error = null)
    }
}
