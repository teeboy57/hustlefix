package com.example.hustlefix

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hustlefix.ui.navigation.HustleFixNavGraph
import com.example.hustlefix.ui.navigation.Screen
import com.example.hustlefix.ui.theme.HustleFixTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        
        askNotificationPermission()

        setContent {
            HustleFixTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val database = FirebaseDatabase.getInstance()
                
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                
                // Reactive session state
                var isLoggedIn by remember { mutableStateOf(SessionHelper.isLoggedIn(this@MainActivity)) }
                var role by remember { mutableStateOf(SessionHelper.getRole(this@MainActivity)) }
                var activeSuspensionReason by remember { mutableStateOf<String?>(null) }
                
                var broadcastMessage by remember { mutableStateOf<String?>(null) }
                
                if (broadcastMessage != null) {
                    AlertDialog(
                        onDismissRequest = { broadcastMessage = null },
                        title = { Text("System Announcement", fontWeight = FontWeight.Black) },
                        text = { Text(broadcastMessage!!) },
                        confirmButton = {
                            Button(onClick = { broadcastMessage = null }) { Text("OK") }
                        },
                        shape = RoundedCornerShape(24.dp)
                    )
                }
                
                // Refresh session state whenever navigation changes
                LaunchedEffect(currentBackStackEntry) {
                    val newIsLoggedIn = SessionHelper.isLoggedIn(this@MainActivity)
                    // Only update if not currently suspended (suspended logic handles its own state)
                    if (activeSuspensionReason == null) {
                        if (isLoggedIn != newIsLoggedIn) {
                            isLoggedIn = newIsLoggedIn
                        }
                        val newRole = SessionHelper.getRole(this@MainActivity)
                        if (role != newRole) {
                            role = newRole
                        }
                    }
                }

                val isProvider = role == "service_provider"
                val isAdmin = role == "admin"
                
                // Real-time suspension check
                DisposableEffect(isLoggedIn) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    var listener: ValueEventListener? = null
                    var userRef: com.google.firebase.database.DatabaseReference? = null
                    
                    var broadcastListener: ValueEventListener? = null
                    val broadcastRef = database.getReference("broadcasts")

                    if (isLoggedIn && uid != null) {
                        // ... existing FCM update ...
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                            FirebaseDatabase.getInstance().getReference("users").child(uid)
                                .child("fcmToken").setValue(token)
                        }

                        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
                        listener = object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val isSuspended = snapshot.child("isSuspended").getValue(Boolean::class.java) ?: false
                                val suspensionUntil = snapshot.child("suspensionUntil").getValue(Long::class.java)
                                
                                if (isSuspended) {
                                    if (suspensionUntil != null && suspensionUntil < System.currentTimeMillis()) {
                                        snapshot.ref.child("isSuspended").setValue(false)
                                        return
                                    }

                                    val reason = snapshot.child("suspensionReason").getValue(String::class.java)
                                        ?: "Your account has been suspended for violating our terms of service."

                                    if (activeSuspensionReason == null) {
                                        activeSuspensionReason = reason
                                        SessionHelper.setLoggedIn(this@MainActivity, false)
                                        isLoggedIn = false
                                        navController.navigate(Screen.Welcome.createRoute(reason)) {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                    }
                                } else {
                                    activeSuspensionReason = null
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        }
                        userRef.addValueEventListener(listener)
                        
                        // 2. Global Broadcast Listener
                        broadcastListener = object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val lastBroadcast = snapshot.children.lastOrNull()
                                val message = lastBroadcast?.child("message")?.getValue(String::class.java)
                                val timestamp = lastBroadcast?.child("timestamp")?.getValue(Long::class.java) ?: 0L
                                
                                // Only show if recent (last 1 hour)
                                val oneHourAgo = System.currentTimeMillis() - 3600000
                                if (message != null && timestamp > oneHourAgo) {
                                    broadcastMessage = message
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        }
                        broadcastRef.limitToLast(1).addValueEventListener(broadcastListener)
                    }

                    onDispose {
                        listener?.let { userRef?.removeEventListener(it) }
                        broadcastListener?.let { broadcastRef.removeEventListener(it) }
                    }
                }
                
                val startDestination = if (activeSuspensionReason != null) {
                    Screen.Welcome.createRoute(activeSuspensionReason)
                } else if (isLoggedIn) {
                    when (role) {
                        "admin" -> Screen.AdminDashboard.route
                        "service_provider" -> Screen.ServiceProviderDashboard.route
                        else -> Screen.ClientDashboard.route
                    }
                } else {
                    Screen.Welcome.route
                }

                // Handle navigation from legacy components or deep links
                LaunchedEffect(intent) {
                    val data = intent.data
                    if (data != null && data.scheme == "hustlefix") {
                        if (data.toString().contains("payment-success")) {
                            navController.navigate(Screen.MyBookings.route) {
                                popUpTo(Screen.ClientDashboard.route)
                            }
                        }
                    }

                    intent.getStringExtra("navigate_to")?.let { destination ->
                        when (destination) {
                            "verification" -> navController.navigate(Screen.Verification.route)
                            "emergency" -> navController.navigate(Screen.Emergency.route)
                            "wallet" -> navController.navigate(Screen.Wallet.route)
                            "chat" -> {
                                val pid = intent.getStringExtra("senderId") ?: ""
                                val pname = intent.getStringExtra("senderName") ?: "Chat"
                                if (pid.isNotEmpty()) {
                                    navController.navigate(Screen.Chat.createRoute(pid, pname))
                                }
                            }
                        }
                    }
                    
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
                            if (isAdmin) {
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                                    label = { Text("Admin Dashboard") },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(Screen.AdminDashboard.route)
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp))
                            }
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                label = { Text(getString(R.string.nav_profile)) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.Profile.route)
                                }
                            )
                            if (!isProvider) {
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    label = { Text(getString(R.string.nav_find_services)) },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(Screen.FindServices.createRoute())
                                    }
                                )
                            }
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.History, contentDescription = null) },
                                label = { Text(getString(R.string.nav_bookings)) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.MyBookings.route)
                                }
                            )
                            if (isProvider) {
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Engineering, contentDescription = null) },
                                    label = { Text(getString(R.string.nav_services)) },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(Screen.MyServices.route)
                                    }
                                )
                            }
                            if (!isProvider) {
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.FlashOn, contentDescription = null) },
                                    label = { Text(getString(R.string.nav_emergency)) },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(Screen.Emergency.route)
                                    }
                                )
                            }
                            NavigationDrawerItem(
                                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                                label = { Text(getString(R.string.nav_messages)) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.ChatList.route)
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                                label = { Text(getString(R.string.nav_wallet)) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.Wallet.route)
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp))
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text(getString(R.string.nav_settings)) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                                label = { Text(getString(R.string.nav_logout)) },
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

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
