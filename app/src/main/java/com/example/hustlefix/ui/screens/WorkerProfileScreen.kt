package com.example.hustlefix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.hustlefix.R
import com.example.hustlefix.Rating
import com.example.hustlefix.Worker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(
    worker: Worker?,
    reviews: List<Rating>,
    isLoading: Boolean,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (worker != null) {
                Surface(tonalElevation = 12.dp, shadowElevation = 12.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCallClick,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CALL")
                        }
                        Button(
                            onClick = onChatClick,
                            modifier = Modifier.weight(2f).height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("START CHAT", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (worker == null || isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
            ) {
                // Header with Image
                Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(worker.profileImage)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_profile_default),
                        error = painterResource(R.drawable.ic_profile_default),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
                    
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(worker.name ?: "Unknown", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
                            if (worker.isVerified) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                            }
                        }
                        Text(worker.skill ?: "Professional", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProfileStatItem("Rating", worker.formattedRating + "★", Icons.Default.Star, Color(0xFFFFC107))
                        ProfileStatItem("Jobs", worker.completedJobs.toString(), Icons.Default.Work, MaterialTheme.colorScheme.primary)
                        ProfileStatItem("Exp", "${worker.experience} yrs", Icons.Default.Timeline, MaterialTheme.colorScheme.secondary)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("About", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        worker.about ?: "No description provided.",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Reviews (${reviews.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (reviews.isEmpty()) {
                        Text("No reviews yet.", color = MaterialTheme.colorScheme.outline)
                    } else {
                        reviews.take(5).forEach { rating ->
                            ReviewItem(rating)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileStatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(value, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun ReviewItem(rating: Rating) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(rating.displayName ?: "User", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text("${rating.rating}★", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(rating.review ?: "", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
