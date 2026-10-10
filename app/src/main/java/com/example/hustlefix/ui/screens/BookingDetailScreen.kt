package com.example.hustlefix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hustlefix.Booking
import com.example.hustlefix.Quote
import com.example.hustlefix.R
import com.example.hustlefix.Service
import com.example.hustlefix.ui.components.StandardCard
import com.example.hustlefix.ui.theme.getStatusColor
import com.example.hustlefix.util.SoundHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    booking: Booking?,
    service: Service?,
    quote: Quote? = null,
    isServiceProvider: Boolean,
    isLoading: Boolean,
    onStatusUpdate: (String, String?) -> Unit,
    onSubmitQuote: (Double, String) -> Unit = { _, _ -> },
    onAcceptQuote: () -> Unit = {},
    onPayQuoteWalletClick: () -> Unit = {},
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
            SoundHelper.playSuccess(context)
        }
    }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showCompletionCodeDialog by remember { mutableStateOf(false) }
    var showDisputeDialog by remember { mutableStateOf(false) }
    var showQuoteDialog by remember { mutableStateOf(false) }
    var quoteAmount by remember { mutableStateOf("") }
    var quoteMessage by remember { mutableStateOf("") }
    var disputeReason by remember { mutableStateOf("") }
    var inputCode by remember { mutableStateOf("") }
    var pendingStatusUpdate by remember { mutableStateOf<String?>(null) }

    if (showQuoteDialog) {
        AlertDialog(
            onDismissRequest = { showQuoteDialog = false },
            title = { Text("Submit On-Site Quote", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter total quoted amount (including parts & labor) and breakdown notes.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = quoteAmount,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) quoteAmount = it },
                        label = { Text("Quoted Amount (R)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        prefix = { Text("R ") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = quoteMessage,
                        onValueChange = { quoteMessage = it },
                        label = { Text("Breakdown (e.g. Parts R500, Labor R800)") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = quoteAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onSubmitQuote(amt, quoteMessage)
                            showQuoteDialog = false
                        }
                    },
                    enabled = quoteAmount.isNotBlank() && quoteMessage.isNotBlank()
                ) {
                    Text("SUBMIT QUOTE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuoteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDisputeDialog) {
        AlertDialog(
            onDismissRequest = { showDisputeDialog = false },
            title = { Text(stringResource(R.string.report_problem), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(stringResource(R.string.dispute_instruction))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = disputeReason,
                        onValueChange = { disputeReason = it },
                        label = { Text(stringResource(R.string.issue_details)) },
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
                    Text(stringResource(R.string.submit_to_admin))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisputeDialog = false }) { Text(stringResource(R.string.cancel)) }
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
            title = { Text(stringResource(R.string.complete_job), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(stringResource(R.string.code_instruction))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { if (it.length <= 4) inputCode = it },
                        label = { Text(stringResource(R.string.job_completion_code)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    Text(stringResource(R.string.mark_as_completed))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletionCodeDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.confirm_cancellation)) },
            text = { Text(stringResource(R.string.cancel_confirmation_text)) },
            confirmButton = {
                TextButton(onClick = { 
                    pendingStatusUpdate?.let { onStatusUpdate(it, null) }
                    showCancelDialog = false 
                }) { 
                    Text(stringResource(R.string.yes_cancel), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text(stringResource(R.string.go_back)) }
            }
        )
    }

                    if (showRatingDialog) {
        var ratingScore by remember { mutableStateOf(5f) }
        var comment by remember { mutableStateOf("") }
        var isAnonymous by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text(stringResource(R.string.rate_the_pro), fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text(stringResource(R.string.how_was_experience, booking?.getWorkerName() ?: "this Pro"))
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
                        label = { Text(stringResource(R.string.your_review)) },
                        placeholder = { Text(stringResource(R.string.review_placeholder)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Checkbox(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
                        Text(stringResource(R.string.post_anonymous), style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    onRatingSubmit(ratingScore, comment, isAnonymous)
                    showRatingDialog = false 
                }) {
                    Text(stringResource(R.string.submit_review))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) { Text(stringResource(R.string.not_now)) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.booking_summary), fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                                text = booking.getServiceTitleCompatibility() ?: "Service Details", 
                                style = MaterialTheme.typography.headlineSmall, 
                                fontWeight = FontWeight.Black,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            val timestamp = booking.getTimestamp()
                            val dateStr = try {
                                if (timestamp > 0) SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp)) else "N/A"
                            } catch (e: Exception) { "N/A" }
                            
                            Text(
                                stringResource(R.string.date_label) + ": $dateStr",
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
                    BookingInfoRow(label = stringResource(R.string.booking_fee), value = "R${String.format(Locale.getDefault(), "%.2f", booking.getPrice() ?: 0.0)}", icon = Icons.Default.Payments)
                    if (isServiceProvider) {
                        val fee = booking.getPlatformFee()
                        val payout = booking.getWorkerEarnings()
                        BookingInfoRow(label = stringResource(R.string.platform_fee), value = "R${String.format(Locale.getDefault(), "%.2f", fee)} (10%)", icon = Icons.Default.Info)
                        BookingInfoRow(label = stringResource(R.string.your_payout), value = "R${String.format(Locale.getDefault(), "%.2f", payout)}", icon = Icons.Default.AccountBalanceWallet)
                    }
                    BookingInfoRow(label = stringResource(R.string.payment_status), value = booking.getPaymentStatus() ?: "UNPAID", icon = Icons.Default.Security)
                    
                    if (!isServiceProvider && (booking.status == "confirmed" || booking.status == "paid" || booking.status == "in_progress" || quote?.status == "settled")) {
                        StandardCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.job_completion_code), style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = booking.completionCode ?: "----",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 8.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    stringResource(R.string.code_instruction),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    BookingInfoRow(
                        label = if (isServiceProvider) stringResource(R.string.role_client) else stringResource(R.string.role_service_provider), 
                        value = if (isServiceProvider) booking.getClientName() ?: "User" else booking.getServiceProviderName() ?: "Pro", 
                        icon = Icons.Default.Person
                    )

                    if (quote != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        StandardCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Official On-Site Quote", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("R${String.format(Locale.getDefault(), "%.2f", quote.amount)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Breakdown / Notes:", style = MaterialTheme.typography.labelMedium)
                                Text(quote.message ?: "No breakdown provided.", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Status: ${(quote.status ?: "pending").uppercase()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    if (!isServiceProvider && quote.status == "pending") {
                                        Button(
                                            onClick = onAcceptQuote,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                        ) {
                                            Text("ACCEPT QUOTE", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (!isServiceProvider && quote.status == "accepted") {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    if (walletBalance >= quote.amount) {
                                        Button(
                                            onClick = onPayQuoteWalletClick,
                                            modifier = Modifier.fillMaxWidth().height(50.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                                        ) {
                                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("SETTLE QUOTE WITH WALLET (R${String.format(Locale.getDefault(), "%.2f", quote.amount)})", fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    Button(
                                        onClick = onPayClick,
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("PAY QUOTE NOW", fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (quote.status == "settled") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("QUOTE PAID & SETTLED", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Actions
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(stringResource(R.string.manage_booking), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                                        Text(stringResource(R.string.verifying_payment), style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (booking.status != "completed" && booking.status != "cancelled") {
                                OutlinedButton(
                                    onClick = { 
                                        pendingStatusUpdate = "cancelled"
                                        showCancelDialog = true 
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.cancel_booking), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (isServiceProvider && booking.status == "pending") {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { onStatusUpdate("confirmed", null) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Text(stringResource(R.string.accept))
                                    }
                                    OutlinedButton(
                                        onClick = { 
                                            pendingStatusUpdate = "cancelled"
                                            showCancelDialog = true 
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text(stringResource(R.string.reject))
                                    }
                                }
                            } else if (booking.status == "confirmed" || booking.status == "paid" || booking.status == "completed" || booking.status == "in_progress" || booking.status == "quoted") {
                                if (isServiceProvider && booking.paymentStatus == "UNPAID") {
                                    val payLinkPrefix = stringResource(R.string.send_pay_link)
                                    Button(
                                        onClick = { onSharePayLink("$payLinkPrefix: ") },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Green
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(stringResource(R.string.send_pay_link), fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                if (isServiceProvider) {
                                    if (booking.paymentStatus == "PAID") {
                                        if (quote == null || quote.status == "pending") {
                                            Button(
                                                onClick = { showQuoteDialog = true },
                                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                            ) {
                                                Icon(Icons.Default.RequestQuote, contentDescription = null)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(if (quote == null) "SUBMIT ON-SITE QUOTE" else "UPDATE QUOTE", fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                    } else {
                                        Text(
                                            "Awaiting booking fee payment from client before submitting quote.",
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }

                                Button(
                                    onClick = onChatClick,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(stringResource(R.string.open_chat), fontWeight = FontWeight.Bold)
                                }
                                
                                if (isServiceProvider && (booking.status == "confirmed" || booking.status == "paid" || booking.status == "in_progress")) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    if (booking.paymentStatus == "PAID") {
                                        if (booking.status == "in_progress") {
                                            OutlinedButton(
                                                onClick = { showCompletionCodeDialog = true },
                                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Text(stringResource(R.string.mark_as_completed), fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = { onStatusUpdate("in_progress", null) },
                                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("START JOB", fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    } else {
                                        Text(
                                            stringResource(R.string.awaiting_payment),
                                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                if (!isServiceProvider && (booking.status == "confirmed" || booking.status == "paid" || booking.status == "in_progress")) {
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
                                                Text(stringResource(R.string.pay_with_wallet) + " (R${String.format(Locale.getDefault(), "%.2f", walletBalance)})", fontWeight = FontWeight.Bold)
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
                                            Text(stringResource(R.string.pay_now), fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                    
                                    /*
                                    Button(
                                        onClick = { onTrackClick(booking.getWorkerId() ?: "") },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Default.MyLocation, contentDescription = null)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(stringResource(R.string.track_worker), fontWeight = FontWeight.Bold)
                                    }
                                    */
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
                                        Text(stringResource(R.string.leave_a_review), fontWeight = FontWeight.Bold)
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
                                    Text(stringResource(R.string.report_problem))
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
                                    Text(stringResource(R.string.report_problem) + " ${if (isServiceProvider) stringResource(R.string.role_client) else stringResource(R.string.role_service_provider)}")
                                }
                            } else {
                                Text(stringResource(R.string.no_actions_available), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
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
fun BookingInfoRow(label: String, value: String, icon: ImageVector) {
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
