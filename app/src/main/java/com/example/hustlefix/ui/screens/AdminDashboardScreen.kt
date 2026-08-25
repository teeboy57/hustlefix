package com.example.hustlefix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.hustlefix.Booking
import com.example.hustlefix.R
import com.example.hustlefix.User
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    totalUsers: Int,
    totalWorkers: Int,
    totalEscrow: String,
    recentBookings: List<Booking>,
    pendingVerifications: List<User>,
    onVerifyClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    isLoading: Boolean = false
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Admin Panel", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Stats Row (Horizontal Scrollable or Wrap)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdminStatCard("Escrow", totalEscrow, Icons.Default.AccountBalanceWallet, Modifier.weight(1.5f))
                    AdminStatCard("Users", totalUsers.toString(), Icons.Default.Group, Modifier.weight(1f))
                    AdminStatCard("Pros", totalWorkers.toString(), Icons.Default.Engineering, Modifier.weight(1f))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-20).dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Section: Pending Verifications
                item {
                    SectionHeader("Pending Verifications")
                }
                
                if (pendingVerifications.isEmpty()) {
                    item {
                        EmptyStateCard("All providers are verified!")
                    }
                } else {
                    items(pendingVerifications) { user ->
                        VerificationItem(user = user, onVerify = { onVerifyClick(user.email) }) // Using email as mock ID for now
                    }
                }

                // Section: Global Activity Oversight
                item {
                    SectionHeader("Global Activity Oversight")
                }

                items(recentBookings) { booking ->
                    AdminBookingItem(booking)
                }
            }
        }
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun AdminStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
            Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 24.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun VerificationItem(user: User, onVerify: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(user.profileImage).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_profile_default)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name ?: "Unknown", fontWeight = FontWeight.Bold)
                Text(user.email ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Button(
                onClick = onVerify,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("VERIFY", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AdminBookingItem(booking: Booking) {
    Card(
        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.serviceTitle ?: "Service", fontWeight = FontWeight.Bold)
                Text("${booking.clientName} → ${booking.serviceProviderName}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("R${booking.price}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Surface(
                    color = (if (booking.paymentStatus == "HELD_BY_ADMIN") Color(0xFF2196F3) else Color.Gray).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = (booking.paymentStatus ?: "UNPAID"),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (booking.paymentStatus == "HELD_BY_ADMIN") Color(0xFF2196F3) else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(text: String) {
    Card(
        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(text, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
