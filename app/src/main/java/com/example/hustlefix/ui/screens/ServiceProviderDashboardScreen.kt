package com.example.hustlefix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.example.hustlefix.Booking
import com.example.hustlefix.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceProviderDashboardScreen(
    businessName: String,
    totalEarnings: String,
    totalSkills: Int,
    totalJobs: Int,
    averageRating: String,
    recentOrders: List<Booking>,
    unreadNotifications: Int = 0,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onQuickActionClick: (String) -> Unit,
    onBookingClick: (Booking) -> Unit,
    onMenuClick: () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            HustleFixTopBar(
                title = "Business Center",
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
                // Pro Header with Modern Gradient
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
                            shape = RoundedCornerShape(28.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                        )
                                    )
                                    .padding(32.dp)
                                    .fillMaxWidth()
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            modifier = Modifier.size(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White.copy(alpha = 0.2f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Business, contentDescription = null, tint = Color.White)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "${getTimeBasedGreeting()}, $businessName!",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Top Rated Provider",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    // Revenue Card Refined
                    AnimatedEntrance(delay = 100) {
                        StandardCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onQuickActionClick("work") }
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Earnings Overview",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { onQuickActionClick("work") }) {
                                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF4CAF50))
                                    }
                                }
                                
                                val animatedEarningsValue = totalEarnings.filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
                                val animatedValue by animateFloatAsState(
                                    targetValue = animatedEarningsValue,
                                    animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                                    label = "earningsAnimation"
                                )
                                
                                Text(
                                    text = "R${String.format(java.util.Locale.getDefault(), "%.2f", animatedValue)}",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                val progress by animateFloatAsState(
                                    targetValue = 0.85f,
                                    animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                                    label = "progressAnimation"
                                )
                                
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "85% of monthly goal reached",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Row with better spacing
                    Row(modifier = Modifier.fillMaxWidth()) {
                        AnimatedEntrance(delay = 200, modifier = Modifier.weight(1f)) {
                            AnimatedStatCard("Skills", totalSkills.toString(), MaterialTheme.colorScheme.secondary) {
                                onQuickActionClick("my_services")
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        AnimatedEntrance(delay = 300, modifier = Modifier.weight(1f)) {
                            AnimatedStatCard("Jobs", totalJobs.toString(), MaterialTheme.colorScheme.primary) {
                                onQuickActionClick("bookings")
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        AnimatedEntrance(delay = 400, modifier = Modifier.weight(1f)) {
                            AnimatedStatCard("Rating", "$averageRating★", Color(0xFFFFC107)) {
                                onQuickActionClick("ratings")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Manage Business",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard("My Services", Icons.Default.Engineering, MaterialTheme.colorScheme.secondary, { onQuickActionClick("my_services") }, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        QuickActionCard("Post Service", Icons.Default.AddCircle, MaterialTheme.colorScheme.tertiary, { onQuickActionClick("new") }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard("Performance", Icons.Default.Assessment, MaterialTheme.colorScheme.outline, { onQuickActionClick("work") }, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        QuickActionCard("Urgent Jobs", Icons.Default.FlashOn, Color(0xFFFF4081), { onQuickActionClick("urgent") }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Incoming Orders",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (recentOrders.isEmpty()) {
                        EmptyState(
                            title = "No active orders",
                            description = "When clients book your services, they will appear here.",
                            icon = Icons.Default.HourglassEmpty,
                            actionLabel = "My Services",
                            onActionClick = { onQuickActionClick("my_services") }
                        )
                    } else {
                        StandardCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                recentOrders.take(5).forEach { order ->
                                    BookingItem(
                                        booking = order,
                                        isServiceProvider = true,
                                        onClick = onBookingClick
                                    )
                                    if (order != recentOrders.last()) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
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
