package com.example.hustlefix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.hustlefix.ui.navigation.HustleFixNavGraph
import com.example.hustlefix.ui.navigation.Screen
import com.example.hustlefix.ui.theme.HustleFixTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()

        setContent {
            HustleFixTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                
                val isLoggedIn = SessionHelper.isLoggedIn(this)
                val role = SessionHelper.getRole(this)
                val isProvider = role == "service_provider"
                
                // Real-time suspension check
                LaunchedEffect(Unit) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        FirebaseDatabase.getInstance().getReference("users").child(uid).child("isSuspended")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val isSuspended = when (val value = snapshot.value) {
                                        is Boolean -> value
                                        is String -> value.toBoolean()
                                        else -> false
                                    }
                                    
                                    if (isSuspended) {
                                        FirebaseAuth.getInstance().signOut()
                                        SessionHelper.setLoggedIn(this@MainActivity, false)
                                        navController.navigate(Screen.Welcome.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }
                
                val startDestination = if (isLoggedIn) {
                    if (isProvider) Screen.ServiceProviderDashboard.route else Screen.ClientDashboard.route
                } else {
                    Screen.Welcome.route
                }

                // Handle navigation from legacy components or deep links
                LaunchedEffect(intent) {
                    intent.getStringExtra("NAV_DESTINATION")?.let { destination ->
                        when (destination) {
                            "profile" -> navController.navigate(Screen.Profile.route)
                            "chat_list" -> navController.navigate(Screen.ChatList.route)
                            "settings" -> navController.navigate(Screen.Settings.route)
                            "chat" -> {
                                val pid = intent.getStringExtra(ChatLauncher.EXTRA_OTHER_USER_ID) ?: ""
                                val pname = intent.getStringExtra(ChatLauncher.EXTRA_OTHER_USER_NAME) ?: ""
                                if (pid.isNotEmpty()) {
                                    navController.navigate(Screen.Chat.createRoute(pid, pname))
                                }
                            }
                        }
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Spacer(Modifier.height(12.dp))
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                label = { Text("My Profile") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.Profile.route)
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.History, contentDescription = null) },
                                label = { Text("My Bookings") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.MyBookings.route)
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                                label = { Text("Messages") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.ChatList.route)
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                                label = { Text("My Wallet") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.Wallet.route)
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp))
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text("Settings") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                                label = { Text("Logout") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    SessionHelper.logout(this@MainActivity)
                                }
                            )
                        }
                    }
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        HustleFixNavGraph(
                            navController = navController,
                            startDestination = startDestination,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                }
            }
        }
    }
}
