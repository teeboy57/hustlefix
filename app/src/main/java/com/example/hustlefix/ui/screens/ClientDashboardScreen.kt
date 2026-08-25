package com.example.hustlefix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hustlefix.Booking
import com.example.hustlefix.ui.components.CategoryItem
import com.example.hustlefix.ui.components.QuickActionCard
import com.example.hustlefix.ui.components.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDashboardScreen(
    clientName: String,
    totalBookings: Int,
    activeBookings: Int,
    completedBookings: Int,
    recentBookings: List<Booking>,
    onCategoryClick: (String) -> Unit,
    onQuickActionClick: (String) -> Unit,
    onBookingClick: (Booking) -> Unit,
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("HustleFix", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Welcome Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Welcome, $clientName!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Active Today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Find by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CategoryItem("Plumber", Icons.Default.Build, Color(0xFF6200EE)) { onCategoryClick("Plumber") }
                CategoryItem("Electric", Icons.Default.Settings, Color(0xFF03DAC6)) { onCategoryClick("Electrician") }
                CategoryItem("Cleaning", Icons.Default.Refresh, Color(0xFFFF4081)) { onCategoryClick("Cleaning") }
                CategoryItem("All", Icons.Default.Search, Color.Gray) { onCategoryClick("All") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard("Total", totalBookings.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                StatCard("Active", activeBookings.toString(), Color(0xFFFF4081), Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                StatCard("Done", completedBookings.toString(), Color(0xFF03DAC6), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionCard("Find Workers", Icons.Default.Search, MaterialTheme.colorScheme.primary, { onQuickActionClick("find") }, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(12.dp))
                QuickActionCard("My Bookings", Icons.Default.DateRange, Color(0xFFFF4081), { onQuickActionClick("bookings") }, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionCard("Saved", Icons.Default.Star, Color(0xFF03DAC6), { onQuickActionClick("saved") }, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(12.dp))
                QuickActionCard("Messages", Icons.Default.Email, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), { onQuickActionClick("messages") }, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (recentBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recent activity", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        recentBookings.take(3).forEach { booking ->
                            BookingItem(booking, onBookingClick)
                            if (booking != recentBookings.last()) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingItem(booking: Booking, onClick: (Booking) -> Unit) {
    ListItem(
        headlineContent = { Text(booking.serviceTitle ?: "Service", fontWeight = FontWeight.Bold) },
        supportingContent = { Text(booking.serviceProviderName ?: "Provider") },
        trailingContent = { 
            Text(
                text = "R${booking.price}", 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ) 
        },
        modifier = Modifier.clickable { onClick(booking) }
    )
}
