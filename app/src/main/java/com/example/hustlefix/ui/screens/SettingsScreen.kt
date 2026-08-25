package com.example.hustlefix.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userName: String,
    userEmail: String,
    isNotificationsEnabled: Boolean,
    isDarkModeEnabled: Boolean,
    error: String?,
    onProfileClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onClearError: () -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // User Summary Section
            Card(
                onClick = onProfileClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(userName.take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(userEmail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Preferences", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            SettingsToggleItem(
                title = "Push Notifications",
                icon = Icons.Default.Notifications,
                checked = isNotificationsEnabled,
                onCheckedChange = onToggleNotifications
            )
            
            SettingsToggleItem(
                title = "Dark Mode",
                icon = Icons.Default.DarkMode,
                checked = isDarkModeEnabled,
                onCheckedChange = onToggleDarkMode
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Account & Security", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            SettingsClickItem(title = "Payment Methods", icon = Icons.Default.Payment, onClick = onPaymentMethodsClick)
            SettingsClickItem(title = "Privacy Policy", icon = Icons.Default.Security, onClick = { showPrivacyDialog = true })

            Spacer(modifier = Modifier.height(32.dp))

            Text("Support", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            SettingsClickItem(title = "Help & Support", icon = Icons.AutoMirrored.Filled.HelpOutline, onClick = { showHelpDialog = true })
            SettingsClickItem(title = "About HustleFix", icon = Icons.Default.Info, onClick = { showAboutDialog = true })

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("LOGOUT", fontWeight = FontWeight.ExtraBold)
            }

            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
            ) {
                Text("Delete Account", color = MaterialTheme.colorScheme.outline)
            }
        }
    }

    if (showPrivacyDialog) {
        SettingsInfoDialog(
            title = "Privacy Policy",
            content = "HustleFix is committed to your privacy. This policy outlines how we handle your data:\\n\\n" +
                    "1. Data Collection: We collect only essential information required to connect service providers with clients, including names, contact details, and location.\\n\\n" +
                    "2. Data Usage: Your information is used strictly for job matching, transaction processing, and app functionality enhancement.\\n\\n" +
                    "3. Data Sharing: We do not sell your personal data to third parties. Contact information is shared only between the client and provider once a booking is confirmed.\\n\\n" +
                    "4. Security: We implement industry-standard encryption to protect your account and payment details.",
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showAboutDialog) {
        SettingsInfoDialog(
            title = "About HustleFix",
            content = "HustleFix v2.5.0\\n\\n" +
                    "The premier marketplace for local professional services. Our mission is to empower independent experts and provide clients with reliable, high-quality help for any task.\\n\\n" +
                    "© 2026 HustleFix. All rights reserved.\\n" +
                    "Designed and built with ❤️ in South Africa.",
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help & Support", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Need assistance? Our team is here to help.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("✉ Email: support@hustlefix.com", style = MaterialTheme.typography.bodyMedium)
                    Text("📞 Phone: +27 711 524 060", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@hustlefix.com")
                        putExtra(Intent.EXTRA_SUBJECT, "HustleFix Support Request")
                    }
                    context.startActivity(Intent.createChooser(intent, "Send Email"))
                }) {
                    Text("EMAIL SUPPORT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("CLOSE")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account?", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
            text = { Text("This action is permanent and cannot be undone. All your data, bookings, and services will be removed from HustleFix.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAccountClick()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("DELETE PERMANENTLY")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun SettingsInfoDialog(title: String, content: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("GOT IT")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun SettingsToggleItem(title: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsClickItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline)
    }
}
