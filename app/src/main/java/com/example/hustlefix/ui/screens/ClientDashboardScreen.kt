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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
    walletBalance: String = "R0.00",
    totalBookings: Int,
    activeBookings: Int,
    completedBookings: Int,
    recentBookings: List<Booking>,
    upcomingBooking: Booking? = null,
    unreadMessagesCount: Int = 0,
    nearbyServices: List<com.example.hustlefix.Service> = emptyList(),
    unreadNotifications: Int = 0,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onCategoryClick: (String) -> Unit,
    onQuickActionClick: (String) -> Unit,
    onBookingClick: (Booking) -> Unit,
    onServiceClick: (com.example.hustlefix.Service) -> Unit = {},
    onMenuClick: () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            HustleFixTopBar(
                title = "HustleFix",
                navigationIcon = Icons.Default.Menu,
                onNavigationClick = onMenuClick,
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
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
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
                    AnimatedEntrance {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${getTimeBasedGreeting()}, $clientName!",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "What service do you need today?",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // Wallet Quick View
                                    Surface(
                                        onClick = { onQuickActionClick("wallet") },
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Wallet", style = MaterialTheme.typography.labelSmall)
                                            Text(walletBalance, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                // Search Bar Placeholder
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Search for experts...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { onQuickActionClick("find_query?q=$searchQuery") }) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Search")
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    if (upcomingBooking != null) {
                        Text(
                            text = "Upcoming Today",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StandardCard(
                            onClick = { onBookingClick(upcomingBooking) },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.EventAvailable, contentDescription = null, tint = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = upcomingBooking.getServiceTitleCompatibility(), 
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text("Scheduled for today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", modifier = Modifier.clickable { onQuickActionClick("chat_${upcomingBooking.getWorkerId()}") })
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

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

                    if (nearbyServices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pros Near You",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { onQuickActionClick("find") }) {
                                Text("See All")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(nearbyServices) { service ->
                                NearbyServiceCard(service, onServiceClick)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        AnimatedEntrance(delay = 100, modifier = Modifier.weight(1f)) {
                            AnimatedStatCard("Total", totalBookings.toString(), MaterialTheme.colorScheme.primary) {
                                onQuickActionClick("bookings")
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        AnimatedEntrance(delay = 200, modifier = Modifier.weight(1f)) {
                            AnimatedStatCard("Active", activeBookings.toString(), Color(0xFFFF4081)) {
                                onQuickActionClick("active_bookings")
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        AnimatedEntrance(delay = 300, modifier = Modifier.weight(1f)) {
                            AnimatedStatCard("Done", completedBookings.toString(), Color(0xFF03DAC6)) {
                                onQuickActionClick("done_bookings")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard("Find Workers", Icons.Default.Groups, MaterialTheme.colorScheme.secondary, { onQuickActionClick("find") }, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        QuickActionCard("My Bookings", Icons.Default.EventNote, Color(0xFFFF4081), { onQuickActionClick("bookings") }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard("Saved Services", Icons.Default.Bookmark, Color(0xFFFFC107), { onQuickActionClick("saved") }, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            QuickActionCard("Messages", Icons.AutoMirrored.Filled.Chat, MaterialTheme.colorScheme.secondary, { onQuickActionClick("messages") })
                            if (unreadMessagesCount > 0) {
                                Badge(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text(unreadMessagesCount.toString())
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        UrgentRequestButton({ onQuickActionClick("emergency") }, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        QuickActionCard("My Wallet", Icons.Default.AccountBalanceWallet, MaterialTheme.colorScheme.primary, { onQuickActionClick("wallet") }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (recentBookings.isEmpty()) {
                        EmptyState(
                            title = "No recent activity",
                            description = "Your recent bookings and jobs will appear here.",
                            icon = Icons.Default.Info,
                            actionLabel = "Find Experts",
                            onActionClick = { onQuickActionClick("find") }
                        )
                    } else {
                        StandardCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                recentBookings.forEach { booking ->
                                    BookingItem(
                                        booking = booking,
                                        isServiceProvider = false,
                                        onClick = onBookingClick
                                    )
                                    if (booking != recentBookings.last()) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            if (pullToRefreshState.isRefreshing) {
                LaunchedEffect(true) {
                    onRefresh()
                }
            }
        }
    }
}
