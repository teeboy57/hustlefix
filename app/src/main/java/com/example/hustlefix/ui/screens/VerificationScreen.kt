package com.example.hustlefix.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    idImageUri: Uri?,
    selfieImageUri: Uri?,
    certImageUri: Uri?,
    remoteIdUrl: String?,
    remoteSelfieUrl: String?,
    remoteCertUrl: String?,
    isLoading: Boolean,
    isSuccess: Boolean,
    error: String?,
    currentStatus: String,
    rejectionReason: String?,
    onIdImageSelected: (Uri?) -> Unit,
    onSelfieImageSelected: (Uri?) -> Unit,
    onCertImageSelected: (Uri?) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
    onClearStatus: () -> Unit
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val idLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onIdImageSelected(uri) }
    )

    val selfieLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onSelfieImageSelected(uri) }
    )

    val certLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onCertImageSelected(uri) }
    )

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            snackbarHostState.showSnackbar("Verification documents submitted successfully!")
            onClearStatus()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            onClearStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Trust & Verification", fontWeight = FontWeight.ExtraBold) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Header & Visual Timeline Tracker
            StatusBanner(status = currentStatus)
            Spacer(modifier = Modifier.height(16.dp))
            VerificationTimeline(currentStatus = currentStatus)

            if (currentStatus == "rejected" && !rejectionReason.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rejection Reason", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(rejectionReason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Verify your account to build trust with clients and unlock premium marketplace features.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ID Section
            DocumentUploadCard(
                title = "Identity Document",
                description = "Clear photo of your ID or Driver's License",
                imageUri = idImageUri,
                remoteUrl = remoteIdUrl,
                enabled = currentStatus != "verified",
                onDelete = { onDeleteDocument("id") },
                onClick = {
                    idLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Selfie Verification Section
            DocumentUploadCard(
                title = "Selfie / Face Verification",
                description = "Clear photo of your face for identity matching",
                imageUri = selfieImageUri,
                remoteUrl = remoteSelfieUrl,
                enabled = currentStatus != "verified",
                onDelete = { onDeleteDocument("selfie") },
                onClick = {
                    selfieLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Certificate Section
            DocumentUploadCard(
                title = "Trade Certificate (Optional)",
                description = "Certificates or proof of professional skills",
                imageUri = certImageUri,
                remoteUrl = remoteCertUrl,
                enabled = currentStatus != "verified",
                onDelete = { onDeleteDocument("cert") },
                onClick = {
                    certLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )

            Spacer(modifier = Modifier.height(36.dp))

            val hasId = idImageUri != null || !remoteIdUrl.isNullOrEmpty()
            val hasSelfie = selfieImageUri != null || !remoteSelfieUrl.isNullOrEmpty()
            val canSubmit = currentStatus != "verified" && hasId && hasSelfie

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading && canSubmit
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (currentStatus == "rejected") "RE-SUBMIT FOR REVIEW" else "SUBMIT FOR REVIEW", fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Documents are reviewed instantly with AI face match.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun VerificationTimeline(currentStatus: String) {
    val step = when (currentStatus) {
        "verified" -> 3
        "pending" -> 2
        else -> 1
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimelineStep(stepNumber = 1, title = "Upload Docs", active = step >= 1)
        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), thickness = 2.dp, color = if (step >= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
        TimelineStep(stepNumber = 2, title = "Under Review", active = step >= 2)
        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), thickness = 2.dp, color = if (step >= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
        TimelineStep(stepNumber = 3, title = "Verified", active = step >= 3)
    }
}

@Composable
fun TimelineStep(stepNumber: Int, title: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun StatusBanner(status: String) {
    val (color, icon, text) = when (status) {
        "verified" -> Triple(Color(0xFF4CAF50), Icons.Default.Verified, "Verified Expert")
        "pending" -> Triple(Color(0xFFFF9800), Icons.Default.HourglassBottom, "Review in Progress")
        "rejected" -> Triple(MaterialTheme.colorScheme.error, Icons.Default.Cancel, "Verification Rejected")
        else -> Triple(MaterialTheme.colorScheme.primary, Icons.Default.Shield, "Not Verified")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun DocumentUploadCard(
    title: String,
    description: String,
    imageUri: Uri?,
    remoteUrl: String?,
    enabled: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
            val displayImage = imageUri ?: remoteUrl
            if (displayImage != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(displayImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                }

                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center).size(48.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap to Upload", fontWeight = FontWeight.Bold, color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                }
            }
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
