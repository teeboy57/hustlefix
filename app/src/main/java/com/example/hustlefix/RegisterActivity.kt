package com.example.hustlefix

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.hustlefix.ui.screens.RegisterScreen
import com.example.hustlefix.ui.theme.HustleFixTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : ComponentActivity() {
    private lateinit var mAuth: FirebaseAuth
    private var userRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageManager.applyLanguage(this)
        super.onCreate(savedInstanceState)
        
        mAuth = FirebaseAuth.getInstance()
        userRole = intent.getStringExtra("ROLE") ?: SessionHelper.getRole(this)

        setContent {
            HustleFixTheme {
                var isLoading by remember { mutableStateOf(false) }

                RegisterScreen(
                    onRegisterClick = { name, email, phone, password ->
                        isLoading = true
                        registerUser(name, email, phone, password) { success ->
                            isLoading = false
                        }
                    },
                    onLoginClick = {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra("ROLE", userRole)
                        startActivity(intent)
                        finish()
                    },
                    isLoading = isLoading
                )
            }
        }
    }

    private fun registerUser(name: String, email: String, phone: String, pass: String, onComplete: (Boolean) -> Unit) {
        mAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    
                    user?.updateProfile(profileUpdate)?.addOnCompleteListener {
                        saveUserProfile(name, email, phone)
                        onComplete(true)
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    onComplete(false)
                }
            }
    }

    private fun saveUserProfile(name: String, email: String, phone: String) {
        val uid = mAuth.uid ?: return
        val firebaseRole = SessionHelper.firebaseRoleForAppRole(userRole)
        
        val userMap = mutableMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "role" to firebaseRole,
            "available" to true,
            "rating" to 0.0,
            "completedJobs" to 0,
            "walletBalance" to 5000.0,
            "isVerified" to false,
            "verificationStatus" to "Not Verified"
        )
        
        if (phone.isNotEmpty()) userMap["phone"] = phone
        
        FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(userMap)
            .addOnCompleteListener {
                SessionHelper.saveRole(this, userRole)
                mAuth.signOut()
                Toast.makeText(this, "Account created! Please sign in.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("ROLE", userRole)
                startActivity(intent)
                finish()
            }
    }
}
