package com.example.hustlefix

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.hustlefix.ui.screens.LoginScreen
import com.example.hustlefix.ui.theme.HustleFixTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : ComponentActivity() {
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

                LoginScreen(
                    onLoginClick = { email, password ->
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            loginUser(email, password) { success ->
                                isLoading = false
                            }
                        } else {
                            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRegisterClick = {
                        val intent = Intent(this, RegisterActivity::class.java)
                        intent.putExtra("ROLE", userRole)
                        startActivity(intent)
                    },
                    onForgotPasswordClick = { showForgotPasswordDialog() },
                    onGoogleLoginClick = { loginWithGoogle() },
                    onAppleLoginClick = { loginWithApple() },
                    isLoading = isLoading
                )
            }
        }
    }

    private fun loginUser(email: String, password: String, onComplete: (Boolean) -> Unit) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveFcmToken()
                    SessionHelper.setLoggedIn(this, true)
                    SessionHelper.saveRole(this, userRole)
                    navigateToDashboard(userRole)
                    onComplete(true)
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    onComplete(false)
                }
            }
    }

    private fun loginWithGoogle() {
        AuthHelper.signInWithGoogle(this as androidx.appcompat.app.AppCompatActivity, userRole, object : AuthHelper.AuthCallback {
            override fun onSuccess() {
                // Success is handled in onActivityResult
            }
            override fun onError(message: String) {
                Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loginWithApple() {
        AuthHelper.signInWithApple(this as androidx.appcompat.app.AppCompatActivity, userRole, object : AuthHelper.AuthCallback {
            override fun onSuccess() {
                navigateToDashboard(userRole)
            }
            override fun onError(message: String) {
                Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthHelper.RC_GOOGLE_SIGN_IN) {
            AuthHelper.handleGoogleSignInResult(this as androidx.appcompat.app.AppCompatActivity, requestCode, resultCode, data, userRole, object : AuthHelper.AuthCallback {
                override fun onSuccess() {
                    navigateToDashboard(userRole)
                }
                override fun onError(message: String) {
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun saveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val uid = mAuth.uid
                if (uid != null) {
                    FirebaseDatabase.getInstance().getReference("users").child(uid)
                        .child("fcmToken").setValue(token)
                }
            }
        }
    }

    private fun navigateToDashboard(role: String) {
        SessionHelper.openDashboard(this, role)
        finish()
    }

    private fun showForgotPasswordDialog() {
        // I'll keep the standard dialog for now as it's easier to port later
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Reset Password")
        val input = android.widget.EditText(this)
        input.hint = "Email"
        builder.setView(input)
        builder.setPositiveButton("Send") { _, _ ->
            val email = input.text.toString().trim()
            if (email.isNotEmpty()) {
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) Toast.makeText(this, "Reset email sent", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}
