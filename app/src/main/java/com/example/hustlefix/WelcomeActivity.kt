package com.example.hustlefix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.hustlefix.ui.screens.WelcomeScreen
import com.example.hustlefix.ui.theme.HustleFixTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageManager.applyLanguage(this)
        super.onCreate(savedInstanceState)
        
        setContent {
            HustleFixTheme {
                WelcomeScreen(
                    onServiceProviderClick = { goToLogin("service_provider") },
                    onClientClick = { goToLogin("CLIENT") }
                )
            }
        }
    }

    private fun goToLogin(role: String) {
        SessionHelper.saveRole(this, role)
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("ROLE", role)
        startActivity(intent)
    }
}
