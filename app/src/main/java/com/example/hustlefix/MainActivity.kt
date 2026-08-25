package com.example.hustlefix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.hustlefix.ui.navigation.HustleFixNavGraph
import com.example.hustlefix.ui.theme.HustleFixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HustleFixTheme {
                val navController = rememberNavController()
                HustleFixNavGraph(navController = navController)
            }
        }
    }
}
