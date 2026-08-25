package com.example.hustlefix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hustlefix.Booking
import com.example.hustlefix.R
import com.example.hustlefix.Service
import com.example.hustlefix.ui.theme.getStatusColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    booking: Booking?,
    service: Service?,
    isServiceProvider: Boolean,
    isLoading: Boolean,
    onStatusUpdate: (String) -> Unit,
    onChatClick: () -> Unit,
    onTrackClick: (String) -> Unit,
    onRatingSubmit: (Float, String, Boolean) -> Unit = { _, _, _ -> },
    onPayClick: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var pendingStatusUpdate by remember { mutableStateOf<String?>(null) }
    
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Confirm Cancellation") },
            text = { Text("Are you sure you want to cancel this booking? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { 
                    pendingStatusUpdate?.let { onStatusUpdate(it) }
                    showCancelDialog = false 
                }) { 
                    Text("YES, CANCEL", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("GO BACK") }
            }
        )
    }

                    if (showRatingDialog) {
        var ratingScore by remember { mutableStateOf(5f) }
        var comment by remember { mutableStateOf("") }
        var isAnonymous by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("Rate the Pro", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("How was your experience with " + (booking?.getWorkerName() ?: "this Pro") + "?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = ratingScore,
                        onValueChange = { ratingScore = it },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                    Text("${ratingScore.toInt()} Stars", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Your Review") },
                        placeholder = { Text("Tell others about the quality of work...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Checkbox(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
                        Text("Post review anonymously", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    onRatingSubmit(ratingScore, comment, isAnonymous)
                    showRatingDialog = false 
                }) {
                    Text("SUBMIT REVIEW")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) { Text("NOT NOW") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Booking Summary", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (booking == null || isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Service Picture Section
                Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(service?.serviceImageUrl ?: service?.serviceImageUrls?.firstOrNull())
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_image_placeholder),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    // Status Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(booking.getServiceTitle() ?: "Service", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                            Text(
                                "Date: " + SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(booking.getTimestamp())),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = getStatusColor(booking.status).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                (booking.status ?: "PENDING").uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = getStatusColor(booking.status)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Detail Items
                    BookingInfoRow(label = "Total Price", value = "R${String.format(Locale.getDefault(), "%.2f", booking.getPrice())}", icon = Icons.Default.Payments)
                    if (isServiceProvider) {
                        BookingInfoRow(label = "Platform Fee", value = "R0.00 (0%)", icon = Icons.Default.Info)
                        BookingInfoRow(label = "Your Payout", value = "R${String.format(Locale.getDefault(), "%.2f", booking.getPrice())}", icon = Icons.Default.AccountBalanceWallet)
                    }
                    BookingInfoRow(label = "Payment Status", value = booking.getPaymentStatus() ?: "UNPAID", icon = Icons.Default.Security)
                    BookingInfoRow(
                        label = if (isServiceProvider) "Client" else "Provider", 
                        value = if (isServiceProvider) booking.getClientName() ?: "User" else booking.getServiceProviderName() ?: "Pro", 
                        icon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Actions
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Manage Booking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (isServiceProvider && booking.status == "pending") {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { onStatusUpdate("confirmed") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Text("ACCEPT")
                                    }
                                    OutlinedButton(
                                        onClick = { 
                                            pendingStatusUpdate = "cancelled"
                                            showCancelDialog = true 
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("REJECT")
                                    }
                                }
                            } else if (booking.status == "confirmed" || booking.status == "paid" || booking.status == "completed") {
                                Button(
                                    onClick = onChatClick,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("OPEN CHAT", fontWeight = FontWeight.Bold)
                                }
                                
                                if (isServiceProvider && (booking.status == "confirmed" || booking.status == "paid")) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { onStatusUpdate("completed") },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text("MARK AS COMPLETED", fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (!isServiceProvider && (booking.status == "confirmed" || booking.status == "paid")) {
                                    if (booking.status == "confirmed") {
                                        Button(
                                            onClick = onPayClick,
                                            modifier = Modifier.fillMaxWidth().height(56.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = null)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("PAY NOW", fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                    
                                    Button(
                                        onClick = { onStatusUpdate("completed") },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Text("CONFIRM COMPLETION", fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { onTrackClick(booking.getWorkerId() ?: "") },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Default.MyLocation, contentDescription = null)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("TRACK WORKER LIVE", fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (!isServiceProvider && booking.status == "completed") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { showRatingDialog = true },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("LEAVE A REVIEW", fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Text("No actions available for this status.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun BookingInfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}
