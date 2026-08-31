package com.example.hustlefix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.example.hustlefix.ui.components.StandardCard
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
    onStatusUpdate: (String, String?) -> Unit,
    onChatClick: () -> Unit,
    onTrackClick: (String) -> Unit,
    onRatingSubmit: (Float, String, Boolean) -> Unit = { _, _, _ -> },
    onPayClick: () -> Unit = {},
    onPayWithWalletClick: () -> Unit = {},
    onSharePayLink: (String) -> Unit = {},
    onReportClick: (String, String) -> Unit = { _, _ -> },
    onDisputeSubmit: (String) -> Unit = {},
    isVerifyingPayment: Boolean = false,
    walletBalance: Double = 0.0,
    onClearError: () -> Unit = {},
    error: String? = null,
    isUpdateSuccess: Boolean = false,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(isUpdateSuccess) {
        if (isUpdateSuccess) {
            com.example.hustlefix.util.SoundHelper.playSuccess(context)
        }
    }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showCompletionCodeDialog by remember { mutableStateOf(false) }
    var showDisputeDialog by remember { mutableStateOf(false) }
    var disputeReason by remember { mutableStateOf("") }
    var inputCode by remember { mutableStateOf("") }
    var pendingStatusUpdate by remember { mutableStateOf<String?>(null) }

    if (showDisputeDialog) {
        AlertDialog(
            onDismissRequest = { showDisputeDialog = false },
            title = { Text("Report a Problem", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Explain the issue clearly. An admin will review the case and resolve the payment escrow.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = disputeReason,
                        onValueChange = { disputeReason = it },
                        label = { Text("Issue Details") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onDisputeSubmit(disputeReason)
                        showDisputeDialog = false 
                    },
                    enabled = disputeReason.isNotBlank()
                ) {
                    Text("SUBMIT TO ADMIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisputeDialog = false }) { Text("CANCEL") }
            }
        )
    }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error)
            onClearError()
        }
    }
    
    if (showCompletionCodeDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionCodeDialog = false },
            title = { Text("Complete Job", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter the 4-digit code provided by the client to confirm work is finished.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { if (it.length <= 4) inputCode = it },
                        label = { Text("4-Digit Code") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onStatusUpdate("completed", inputCode)
                        showCompletionCodeDialog = false 
                    },
                    enabled = inputCode.length == 4
                ) {
                    Text("VERIFY & COMPLETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletionCodeDialog = false }) { Text("CANCEL") }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Confirm Cancellation") },
            text = { Text("Are you sure you want to cancel this booking? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { 
                    pendingStatusUpdate?.let { onStatusUpdate(it, null) }
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Booking Summary", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isVerifyingPayment) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
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
                            .data(service?.serviceImageUrl ?: service?.serviceImageUrls?.firstOrNull() ?: booking.serviceImageUrl)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_image_placeholder),
                        error = painterResource(R.drawable.ic_image_placeholder),
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
                            Text(
                                text = booking.getServiceTitleCompatibility(), 
                                style = MaterialTheme.typography.headlineSmall, 
                                fontWeight = FontWeight.Black,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
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
                    
                    if (!isServiceProvider && (booking.status == "confirmed" || booking.status == "paid")) {
                        com.example.hustlefix.ui.components.StandardCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Job Completion Code", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = booking.completionCode ?: "----",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 8.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    "Give this 4-digit code to the pro ONLY when the job is done.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

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
                            
                            if (isVerifyingPayment) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Verifying Payment...", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (isServiceProvider && booking.status == "pending") {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { onStatusUpdate("confirmed", null) },
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
                                if (isServiceProvider && booking.paymentStatus == "UNPAID") {
                                    Button(
                                        onClick = { onSharePayLink("Payment link for ${booking.getServiceTitle()}: ") },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Green
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("SEND PAY LINK", fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                Button(
                                    onClick = onChatClick,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("OPEN CHAT", fontWeight = FontWeight.Bold)
                                }
                                
                                if (isServiceProvider && (booking.status == "confirmed" || booking.status == "paid")) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    if (booking.paymentStatus == "PAID") {
                                        OutlinedButton(
                                            onClick = { showCompletionCodeDialog = true },
                                            modifier = Modifier.fillMaxWidth().height(56.dp),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("MARK AS COMPLETED", fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text(
                                            "Awaiting Client Payment...",
                                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                if (!isServiceProvider && (booking.status == "confirmed" || booking.status == "paid")) {
                                    if (booking.status == "confirmed" && booking.paymentStatus == "UNPAID") {
                                        // Wallet Payment Option
                                        if (walletBalance >= (booking.amount ?: 0.0)) {
                                            Button(
                                                onClick = onPayWithWalletClick,
                                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                                            ) {
                                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("PAY WITH WALLET (R${String.format(Locale.getDefault(), "%.2f", walletBalance)})", fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }

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

                                Spacer(modifier = Modifier.height(24.dp))
                                TextButton(
                                    onClick = { showDisputeDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.ReportProblem, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Report a Problem")
                                }
                                
                                TextButton(
                                    onClick = {
                                        val pid = if (isServiceProvider) booking.getClientId() else booking.getWorkerId()
                                        val pname = if (isServiceProvider) booking.getClientName() else booking.getServiceProviderName()
                                        onReportClick(pid ?: "", pname ?: "User")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.outline)
                                ) {
                                    Icon(Icons.Default.Report, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Report ${if (isServiceProvider) "Client" else "Provider"}")
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
