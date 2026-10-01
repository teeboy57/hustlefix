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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hustlefix.R
import com.example.hustlefix.ui.components.*
import com.example.hustlefix.util.SoundHelper

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
    val context = LocalContext.current

    Scaffold(
        topBar = {
            HustleFixTopBar(
                title = stringResource(R.string.admin_title),
                navigationIcon = Icons.Default.Menu,
                onNavigationClick = onMenuClick,
                actions = {
                    IconButton(onClick = { 
                        SoundHelper.playClick(context)
                        onQuickActionClick("refresh") 
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.loading))
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
                    Text(stringResource(R.string.system_overview), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        AnimatedStatCard(stringResource(R.string.stat_users), totalUsers.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
                            // Navigate to User Management
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        AnimatedStatCard(stringResource(R.string.stat_total_jobs), totalJobs.toString(), MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) {
                            // Navigate to Jobs
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(stringResource(R.string.attention_required), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
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
                                Text(stringResource(R.string.attention_required), fontWeight = FontWeight.Bold)
                                Text("$pendingVerifications " + stringResource(R.string.attention_required), style = MaterialTheme.typography.bodySmall)
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
                                Text(stringResource(R.string.nav_emergency), fontWeight = FontWeight.Bold, color = if (activeEmergencies > 0) Color.Red else Color.Unspecified)
                                Text("$activeEmergencies " + stringResource(R.string.attention_required), style = MaterialTheme.typography.bodySmall)
                            }
                            if (activeEmergencies > 0) {
                                Badge(containerColor = Color.Red) {
                                    Text(activeEmergencies.toString())
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(stringResource(R.string.control_panel), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard(stringResource(R.string.broadcast), Icons.Default.Campaign, Color(0xFF2196F3), { onQuickActionClick("broadcast") }, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        QuickActionCard(stringResource(R.string.delete_last), Icons.Default.DeleteForever, Color(0xFFFF5252), { onQuickActionClick("delete_broadcast") }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    QuickActionCard(stringResource(R.string.activity_log), Icons.Default.ListAlt, Color(0xFF4CAF50), { onQuickActionClick("logs") }, Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
