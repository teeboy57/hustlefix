package com.example.hustlefix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hustlefix.Booking
import com.example.hustlefix.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDashboardScreen(
    clientName: String,
    totalBookings: Int,
    activeBookings: Int,
    completedBookings: Int,
    recentBookings: List<Booking>,
    unreadNotifications: Int = 0,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onCategoryClick: (String) -> Unit,
    onQuickActionClick: (String) -> Unit,
    onBookingClick: (Booking) -> Unit,
    onMenuClick: () -> Unit
) {
    // val pullToRefreshState = rememberPullToRefreshState()
    
    // if (pullToRefreshState.isRefreshing) {
    //    LaunchedEffect(true) {
    //        onRefresh()
    //    }
    // }

    // LaunchedEffect(isRefreshing) {
    //    if (!isRefreshing) {
    //        pullToRefreshState.endRefresh()
    //    } else {
    //        pullToRefreshState.startRefresh()
    //    }
    // }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("HustleFix", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { onQuickActionClick("notifications") }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge { Text(unreadNotifications.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Premium Welcome Header with Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.surface)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Hello, $clientName!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "What service do you need today?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Search Bar Placeholder
                            OutlinedCard(
                                onClick = { onQuickActionClick("find") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Search for experts...", color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        item { CategoryItem("Plumber", Icons.Default.Build, Color(0xFF6750A4)) { onCategoryClick("Plumbing") } }
                        item { CategoryItem("Electric", Icons.Default.Settings, Color(0xFF03DAC6)) { onCategoryClick("Electrical") } }
                        item { CategoryItem("Cleaning", Icons.Default.Refresh, Color(0xFFFF4081)) { onCategoryClick("Cleaning") } }
                        item { CategoryItem("Painting", Icons.Default.Brush, Color(0xFFFFC107)) { onCategoryClick("Painting") } }
                        item { CategoryItem("Carpentry", Icons.Default.Handyman, Color(0xFF795548)) { onCategoryClick("Carpentry") } }
                        item { CategoryItem("Gardening", Icons.Default.Yard, Color(0xFF4CAF50)) { onCategoryClick("Gardening") } }
                        item { CategoryItem("Moving", Icons.Default.LocalShipping, Color(0xFF2196F3)) { onCategoryClick("Moving") } }
                        item { CategoryItem("All", Icons.Default.GridView, Color.Gray) { onCategoryClick("All") } }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatCard("Total", totalBookings.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        StatCard("Active", activeBookings.toString(), Color(0xFFFF4081), Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        StatCard("Done", completedBookings.toString(), Color(0xFF03DAC6), Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard("Post a Job", Icons.Default.AddCircle, MaterialTheme.colorScheme.primary, { onQuickActionClick("post_job") }, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        QuickActionCard("Find Workers", Icons.Default.PersonSearch, MaterialTheme.colorScheme.secondary, { onQuickActionClick("find") }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard("My Bookings", Icons.Default.EventNote, Color(0xFFFF4081), { onQuickActionClick("bookings") }, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        UrgentRequestButton({ onQuickActionClick("emergency") }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard("Messages", Icons.Default.Chat, MaterialTheme.colorScheme.secondary, { onQuickActionClick("messages") }, Modifier.weight(1f))
                        Spacer(modifier = Modifier.weight(1f)) // Empty space to balance the row
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (recentBookings.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No recent activity yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                recentBookings.forEach { booking ->
                                    BookingItem(booking, onBookingClick)
                                    if (booking != recentBookings.last()) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
