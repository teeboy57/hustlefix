package com.example.hustlefix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hustlefix.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    profitBalance: String,
    totalUsers: Int,
    totalJobs: Int,
    pendingVerifications: Int,
    activeEmergencies: Int,
    isLoading: Boolean,
    onMenuClick: () -> Unit,
    onQuickActionClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            HustleFixTopBar(
                title = "HustleFix Admin",
                navigationIcon = Icons.Default.Menu,
                onNavigationClick = onMenuClick,
                actions = {
                    IconButton(onClick = { onQuickActionClick("refresh") }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp)
                ) {
                    // Admin Wallet Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF6750A4), Color(0xFF4F378B))
                                    )
                                )
                                .padding(32.dp)
                                .fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Platform Profit", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = profitBalance,
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Available for Payout",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("System Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        AnimatedStatCard("Users", totalUsers.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
                            // Navigate to User Management
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        AnimatedStatCard("Total Jobs", totalJobs.toString(), MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) {
                            // Navigate to Jobs
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Attention Required", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Pending Verifications
                    StandardCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onQuickActionClick("verifications") },
                        containerColor = if (pendingVerifications > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pending Verifications", fontWeight = FontWeight.Bold)
                                Text("$pendingVerifications users waiting for approval", style = MaterialTheme.typography.bodySmall)
                            }
                            if (pendingVerifications > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text(pendingVerifications.toString())
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Active Emergencies
                    StandardCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onQuickActionClick("emergencies") },
                        containerColor = if (activeEmergencies > 0) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = Color.Red) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Active Emergencies", fontWeight = FontWeight.Bold, color = if (activeEmergencies > 0) Color.Red else Color.Unspecified)
                                Text("$activeEmergencies urgent alerts active", style = MaterialTheme.typography.bodySmall)
                            }
                            if (activeEmergencies > 0) {
                                Badge(containerColor = Color.Red) {
                                    Text(activeEmergencies.toString())
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Control Panel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard("Broadcast", Icons.Default.Campaign, Color(0xFF2196F3), { onQuickActionClick("broadcast") }, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        QuickActionCard("Activity Log", Icons.Default.ListAlt, Color(0xFF4CAF50), { onQuickActionClick("logs") }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
