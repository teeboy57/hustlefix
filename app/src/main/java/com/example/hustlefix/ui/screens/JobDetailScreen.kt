package com.example.hustlefix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hustlefix.Job

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    job: Job?,
    isLoading: Boolean,
    onQuoteSubmit: (Double, String) -> Unit,
    onBackClick: () -> Unit
) {
    var quoteAmount by remember { mutableStateOf("") }
    var quoteMessage by remember { mutableStateOf("") }
    var showQuoteSheet by remember { mutableStateOf(false) }

    LaunchedEffect(job) {
        if (job != null && quoteAmount.isEmpty()) {
            quoteAmount = job.getQuotedAmount().toString()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Job Details", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (job != null && job.status == "open") {
                Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showQuoteSheet = true },
                        modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("SUBMIT QUOTE", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    ) { padding ->
        if (job == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(job.getTitle(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(job.getCategory()) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📍 " + job.getLocation(), style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Client's Budget", style = MaterialTheme.typography.labelMedium)
                        Text(job.getFormattedAmount(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(job.getDescription(), style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text("About the Client", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(job.getClientName(), fontWeight = FontWeight.Bold)
                        Text("Member since 2024", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showQuoteSheet) {
        ModalBottomSheet(onDismissRequest = { showQuoteSheet = false }) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                Text("Submit your Quote", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = quoteAmount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) quoteAmount = it },
                    label = { Text("Your Quote Amount (R)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    prefix = { Text("R ") }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Payout Calculation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    val amount = quoteAmount.toDoubleOrNull() ?: 0.0
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Platform Fee (0%)", style = MaterialTheme.typography.bodyMedium)
                            Text("R0.00", fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Your Total Payout", fontWeight = FontWeight.Black)
                            Text("R${String.format("%.2f", amount)}", fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = quoteMessage,
                    onValueChange = { quoteMessage = it },
                    label = { Text("Message to Client") },
                    placeholder = { Text("Explain why you are the best for this job...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        onQuoteSubmit(quoteAmount.toDoubleOrNull() ?: 0.0, quoteMessage)
                        showQuoteSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading && quoteAmount.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("CONFIRM QUOTE", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
