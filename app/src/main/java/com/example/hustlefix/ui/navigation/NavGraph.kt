package com.example.hustlefix.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.hustlefix.SessionHelper
import com.example.hustlefix.ui.screens.*
import com.example.hustlefix.ui.viewmodels.*
import com.example.hustlefix.ApiClient

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object ClientDashboard : Screen("client_dashboard")
    object ServiceProviderDashboard : Screen("service_provider_dashboard")
    object MyBookings : Screen("my_bookings")
    object FindServices : Screen("find_services?category={category}") {
        fun createRoute(category: String = "All") = "find_services?category=$category"
    }
    object Profile : Screen("profile")
    object Wallet : Screen("wallet")
    object Settings : Screen("settings")
    object PostService : Screen("post_service")
    object MyServices : Screen("my_services")
    object Analytics : Screen("analytics")
    object Notifications : Screen("notifications")
    object Emergency : Screen("emergency")
    object Map : Screen("map")
    object Verification : Screen("verification")
    object UrgentJobs : Screen("urgent_jobs")
    object Insights : Screen("insights")
    object IncomeStatement : Screen("income_statement")
    object LiveTracking : Screen("live_tracking/{workerId}") {
        fun createRoute(workerId: String) = "live_tracking/$workerId"
    }
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{partnerId}/{partnerName}") {
        fun createRoute(partnerId: String, partnerName: String) = "chat/$partnerId/$partnerName"
    }
    object SavedServices : Screen("saved_services")
    object PaymentMethods : Screen("payment_methods")
    object Ratings : Screen("ratings")
    object WorkerProfile : Screen("worker_profile/{workerId}") {
        fun createRoute(workerId: String) = "worker_profile/$workerId"
    }
    object ServiceDetail : Screen("service_detail/{serviceId}") {
        fun createRoute(serviceId: String) = "service_detail/$serviceId"
    }
    object PayfastCheckout : Screen("payfast_checkout?url={url}") {
        fun createRoute(url: String) = "payfast_checkout?url=${java.net.URLEncoder.encode(url, "UTF-8")}"
    }
    
    // New Job Screens
    object PostJob : Screen("post_job")
    object AvailableJobs : Screen("available_jobs")
    object JobDetail : Screen("job_detail/{jobId}") {
        fun createRoute(jobId: String) = "job_detail/$jobId"
    }
    object JobQuotes : Screen("job_quotes/{jobId}") {
        fun createRoute(jobId: String) = "job_quotes/$jobId"
    }
}

@Composable
fun HustleFixNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Welcome.route,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel() as T
        }
    })
    val authState by authViewModel.uiState.collectAsState()

    // Navigation logic after successful auth
    LaunchedEffect(authState.isLoginSuccessful, authState.isRegisterSuccessful) {
        if (authState.isLoginSuccessful || authState.isRegisterSuccessful) {
            val role = SessionHelper.getRole(context)
            val destination = if (role == "service_provider") {
                Screen.ServiceProviderDashboard.route
            } else {
                Screen.ClientDashboard.route
            }
            navController.navigate(destination) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(500)) + slideInHorizontally(tween(500)) { it } },
        exitTransition = { fadeOut(tween(500)) + slideOutHorizontally(tween(500)) { -it } },
        popEnterTransition = { fadeIn(tween(500)) + slideInHorizontally(tween(500)) { -it } },
        popExitTransition = { fadeOut(tween(500)) + slideOutHorizontally(tween(500)) { it } }
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onServiceProviderClick = { 
                    SessionHelper.saveRole(context, "service_provider")
                    navController.navigate(Screen.Login.route) 
                },
                onClientClick = { 
                    SessionHelper.saveRole(context, "client")
                    navController.navigate(Screen.Login.route)
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { e, p -> authViewModel.login(e, p, context) },
                onRegisterClick = { 
                    navController.navigate(Screen.Register.route) 
                },
                onForgotPasswordClick = { /* Handle */ },
                onGoogleLoginClick = { /* Handle */ },
                onAppleLoginClick = { /* Handle */ },
                isLoading = authState.isLoading,
                error = authState.error,
                onClearError = { authViewModel.clearError() }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = { n: String, e: String, ph: String, p: String -> 
                    val role = SessionHelper.getRole(context)
                    authViewModel.register(n, e, ph, p, role, context) 
                },
                onLoginClick = { 
                    navController.navigate(Screen.Login.route) 
                },
                isLoading = authState.isLoading,
                error = authState.error,
                onClearError = { authViewModel.clearError() }
            )
        }

        composable(Screen.ClientDashboard.route) {
            val viewModel: ClientDashboardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return ClientDashboardViewModel() as T
                }
            })
            val uiState by viewModel.uiState.collectAsState()
            
            val notificationViewModel: NotificationViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return NotificationViewModel() as T
                }
            })
            val notifState by notificationViewModel.uiState.collectAsState()

            ClientDashboardScreen(
                clientName = uiState.clientName,
                totalBookings = uiState.totalBookings,
                activeBookings = uiState.activeBookings,
                completedBookings = uiState.completedBookings,
                recentBookings = uiState.recentBookings,
                unreadNotifications = notifState.unreadCount,
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                onCategoryClick = { category -> 
                    navController.navigate(Screen.FindServices.createRoute(category))
                },
                onQuickActionClick = { action ->
                    when (action) {
                        "post_job" -> navController.navigate(Screen.PostJob.route)
                        "find" -> navController.navigate(Screen.Map.route)
                        "bookings" -> navController.navigate(Screen.MyBookings.route)
                        "saved" -> navController.navigate(Screen.SavedServices.route)
                        "messages" -> navController.navigate(Screen.ChatList.route)
                        "notifications" -> navController.navigate(Screen.Notifications.route)
                        "emergency" -> navController.navigate(Screen.Emergency.route)
                    }
                },
                onBookingClick = { booking ->
                    navController.navigate("booking_detail/${booking.bookingId}")
                },
                onMenuClick = onMenuClick
            )
        }

        composable(Screen.ServiceProviderDashboard.route) {
            val viewModel: ServiceProviderDashboardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return ServiceProviderDashboardViewModel() as T
                }
            })
            val uiState by viewModel.uiState.collectAsState()
            
            val notificationViewModel: NotificationViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return NotificationViewModel() as T
                }
            })
            val notifState by notificationViewModel.uiState.collectAsState()

            ServiceProviderDashboardScreen(
                businessName = uiState.businessName,
                totalEarnings = uiState.totalEarnings,
                totalSkills = uiState.totalSkills,
                totalJobs = uiState.totalJobs,
                averageRating = uiState.averageRating,
                recentOrders = uiState.recentOrders,
                unreadNotifications = notifState.unreadCount,
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                onQuickActionClick = { action ->
                    when (action) {
                        "find_jobs" -> navController.navigate(Screen.AvailableJobs.route)
                        "my_services" -> navController.navigate(Screen.MyServices.route)
                        "new" -> navController.navigate(Screen.PostService.route)
                        "work" -> navController.navigate(Screen.UrgentJobs.route)
                        "settings" -> navController.navigate(Screen.Settings.route)
                        "notifications" -> navController.navigate(Screen.Notifications.route)
                    }
                },
                onBookingClick = { booking ->
                    navController.navigate("booking_detail/${booking.bookingId}?isServiceProvider=true")
                },
                onMenuClick = onMenuClick
            )
        }

        composable(Screen.MyBookings.route) {
            val viewModel: MyBookingsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            MyBookingsScreen(
                bookings = uiState.bookings,
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                onBookingClick = { booking ->
                    val isProvider = SessionHelper.getRole(context) == "service_provider"
                    navController.navigate("booking_detail/${booking.bookingId}?isServiceProvider=$isProvider")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.FindServices.route,
            arguments = listOf(navArgument("category") { defaultValue = "All" })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "All"
            val viewModel: FindServicesViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(category) {
                viewModel.onCategoryChange(category)
            }
            
            FindServicesScreen(
                services = uiState.filteredServices,
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                onServiceClick = { service ->
                    val sid = service.serviceId ?: ""
                    if (sid.isNotEmpty()) {
                        navController.navigate(Screen.ServiceDetail.createRoute(sid))
                    }
                },
                onSortClick = { /* Sort */ },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ServiceDetail.route,
            arguments = listOf(navArgument("serviceId") { defaultValue = "" })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            val viewModel: ServiceDetailViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(serviceId) {
                if (serviceId.isNotEmpty()) {
                    viewModel.loadService(serviceId)
                }
            }
            
            ServiceDetailScreen(
                service = uiState.service,
                isLoading = uiState.isLoading,
                onBackClick = { navController.popBackStack() },
                onBookNowClick = { /* Book */ },
                onSaveClick = { /* Save */ },
                onProviderClick = { 
                    val pid = uiState.service?.getserviceProviderId() ?: ""
                    if (pid.isNotEmpty()) {
                        navController.navigate(Screen.WorkerProfile.createRoute(pid))
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            val viewModel: ProfileViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            ProfileScreen(
                name = uiState.name,
                email = uiState.email,
                phone = uiState.phone,
                location = uiState.location,
                skill = uiState.skill,
                category = uiState.category,
                about = uiState.about,
                experience = uiState.experience,
                photoUrl = uiState.photoUrl,
                selectedImageUri = uiState.selectedImageUri,
                walletBalance = uiState.walletBalance,
                role = uiState.role,
                isLoading = uiState.isLoading,
                isSuccess = uiState.isSuccess,
                error = uiState.error,
                onNameChange = { viewModel.onNameChange(it) },
                onPhoneChange = { viewModel.onPhoneChange(it) },
                onLocationChange = { viewModel.onLocationChange(it) },
                onSkillChange = { viewModel.onSkillChange(it) },
                onCategoryChange = { viewModel.onCategoryChange(it) },
                onAboutChange = { viewModel.onAboutChange(it) },
                onExperienceChange = { viewModel.onExperienceChange(it) },
                onImageSelected = { viewModel.onImageSelected(it) },
                onSaveClick = { viewModel.saveProfile() },
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    SessionHelper.logout(context)
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0)
                    }
                },
                onClearStatus = { viewModel.clearStatus() },
                onVerificationClick = { navController.navigate(Screen.Verification.route) }
            )
        }

        composable(Screen.Verification.route) {
            val viewModel: VerificationViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            VerificationScreen(
                idImageUri = uiState.idImageUri,
                certImageUri = uiState.certImageUri,
                isLoading = uiState.isLoading,
                isSuccess = uiState.isSuccess,
                error = uiState.error,
                currentStatus = uiState.currentStatus,
                rejectionReason = uiState.rejectionReason,
                onIdImageSelected = { viewModel.onIdImageSelected(it) },
                onCertImageSelected = { viewModel.onCertImageSelected(it) },
                onSubmit = { viewModel.submitVerification() },
                onBackClick = { navController.popBackStack() },
                onClearStatus = { viewModel.clearStatus() }
            )
        }

        composable(Screen.Wallet.route) {
            val viewModel: WalletViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            WalletScreen(
                balance = uiState.balance,
                transactions = uiState.transactions,
                isLoading = uiState.isLoading,
                onTopUpClick = { viewModel.topUp(it) },
                onStatementClick = { navController.navigate(Screen.IncomeStatement.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.IncomeStatement.route) {
            val viewModel: IncomeStatementViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            IncomeStatementScreen(
                transactions = uiState.transactions,
                totalIncome = uiState.totalIncome,
                totalFees = uiState.totalFees,
                netPay = uiState.netPay,
                isLoading = uiState.isLoading,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Insights.route) {
            val viewModel: InsightsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            InsightsScreen(
                stats = uiState.stats,
                isLoading = uiState.isLoading,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.UrgentJobs.route) {
            val viewModel: UrgentJobsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            UrgentJobsFeedScreen(
                urgentJobs = uiState.urgentJobs,
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                onAcceptJob = { viewModel.acceptJob(it) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isLoggedOut, uiState.isAccountDeleted) {
                if (uiState.isLoggedOut || uiState.isAccountDeleted) {
                    SessionHelper.logout(context)
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            SettingsScreen(
                userName = uiState.userName,
                userEmail = uiState.userEmail,
                isNotificationsEnabled = uiState.isNotificationsEnabled,
                isDarkModeEnabled = uiState.isDarkModeEnabled,
                error = uiState.error,
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onPaymentMethodsClick = { navController.navigate(Screen.PaymentMethods.route) },
                onToggleNotifications = { viewModel.toggleNotifications(it) },
                onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                onLogoutClick = { viewModel.logout() },
                onDeleteAccountClick = { viewModel.deleteAccount() },
                onClearError = { viewModel.clearError() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.PaymentMethods.route) {
            val viewModel: PaymentMethodsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            PaymentMethodsScreen(
                methods = uiState.methods,
                onAddMethod = { t, n, e -> viewModel.addMethod(t, n, e) },
                onRemoveMethod = { viewModel.removeMethod(it) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.PostService.route) {
            val viewModel: PostServiceViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.popBackStack()
                }
            }

            PostServiceScreen(
                isLoading = uiState.isLoading,
                onPostClick = { t, d, c, p, i -> viewModel.postService(t, d, c, p, i) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.MyServices.route) {
            val viewModel: MyServicesViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            MyServicesScreen(
                services = uiState.services,
                isLoading = uiState.isLoading,
                onServiceClick = { /* Edit? */ },
                onDeleteClick = { viewModel.deleteService(it) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.SavedServices.route) {
            val viewModel: SavedServicesViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            SavedServicesScreen(
                services = uiState.services,
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                onServiceClick = { service ->
                    val sid = service.serviceId ?: ""
                    if (sid.isNotEmpty()) {
                        navController.navigate(Screen.ServiceDetail.createRoute(sid))
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Analytics.route) {
            val viewModel: AnalyticsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            AnalyticsScreen(
                stats = uiState.stats,
                isLoading = uiState.isLoading,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            val viewModel: NotificationViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            NotificationScreen(
                notifications = uiState.notifications,
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                onNotificationClick = { notification ->
                    viewModel.markAsRead(notification.id)
                },
                onDeleteNotification = { viewModel.deleteNotification(it) },
                onMarkAllRead = { viewModel.markAllAsRead() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Emergency.route) {
            val viewModel: EmergencyRequestViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(Unit) {
                viewModel.fetchCurrentLocation()
            }
            
            EmergencyRequestScreen(
                currentAddress = uiState.currentAddress,
                isLoading = uiState.isLoading,
                isSuccess = uiState.isSuccess,
                error = uiState.error,
                onSendEmergency = { t, d -> viewModel.sendEmergencyRequest(t, d) },
                onBackClick = { navController.popBackStack() },
                onClearError = { viewModel.clearError() }
            )
        }

        composable(Screen.Map.route) {
            val viewModel: MapViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            MapScreen(
                workers = uiState.workers,
                userLat = uiState.userLatitude,
                userLng = uiState.userLongitude,
                isLoading = uiState.isLoading,
                onWorkerClick = { worker ->
                    val wid = worker.id ?: ""
                    if (wid.isNotEmpty()) {
                        navController.navigate(Screen.WorkerProfile.createRoute(wid))
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LiveTracking.route,
            arguments = listOf(navArgument("workerId") { defaultValue = "" })
        ) { backStackEntry ->
            val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
            val viewModel: MapViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(workerId) {
                if (workerId.isNotEmpty()) {
                    viewModel.startTrackingWorker(workerId)
                }
            }
            
            LiveTrackingScreen(
                worker = uiState.trackedWorker,
                userLat = uiState.userLatitude,
                userLng = uiState.userLongitude,
                onBackClick = { navController.popBackStack() },
                onChatClick = {
                    if (workerId.isNotEmpty()) {
                        navController.navigate(Screen.Chat.createRoute(workerId, uiState.trackedWorker?.name ?: "Pro"))
                    }
                }
            )
        }

        composable(Screen.ChatList.route) {
            val viewModel: ChatViewModel = viewModel()
            val uiState by viewModel.listUiState.collectAsState()
            
            LaunchedEffect(Unit) {
                viewModel.loadChatList()
            }
            
            ChatListScreen(
                chats = uiState.chats,
                isLoading = uiState.isLoading,
                onChatClick = { chat ->
                    val pid = chat.partnerId ?: ""
                    val pname = chat.partnerName ?: ""
                    if (pid.isNotEmpty()) {
                        navController.navigate(Screen.Chat.createRoute(pid, pname))
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("partnerId") { defaultValue = "" },
                navArgument("partnerName") { defaultValue = "Chat" }
            )
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId") ?: ""
            val partnerName = backStackEntry.arguments?.getString("partnerName") ?: "Chat"
            val viewModel: ChatViewModel = viewModel()
            val uiState by viewModel.chatUiState.collectAsState()
            
            LaunchedEffect(partnerId, partnerName) {
                if (partnerId.isNotEmpty()) {
                    viewModel.loadChat(partnerId, partnerName)
                }
            }
            
            ChatScreen(
                partnerName = uiState.partnerName,
                messages = uiState.messages,
                isLoading = uiState.isLoading,
                onSendMessage = { 
                    if (partnerId.isNotEmpty()) {
                        viewModel.sendMessage(partnerId, partnerName, it)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Ratings.route) {
            val viewModel: RatingsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            RatingsScreen(
                ratings = uiState.ratings,
                isLoading = uiState.isLoading,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WorkerProfile.route,
            arguments = listOf(navArgument("workerId") { defaultValue = "" })
        ) { backStackEntry ->
            val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
            val viewModel: WorkerProfileViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(workerId) {
                if (workerId.isNotEmpty()) {
                    viewModel.loadWorker(workerId)
                }
            }
            
            WorkerProfileScreen(
                worker = uiState.worker,
                reviews = uiState.reviews,
                isLoading = uiState.isLoading,
                onChatClick = {
                    if (workerId.isNotEmpty()) {
                        navController.navigate(Screen.Chat.createRoute(workerId, uiState.worker?.name ?: "User"))
                    }
                },
                onCallClick = {
                    val phone = uiState.worker?.phone ?: ""
                    if (phone.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "booking_detail/{bookingId}?isServiceProvider={isServiceProvider}",
            arguments = listOf(
                navArgument("bookingId") { defaultValue = "" },
                navArgument("isServiceProvider") { defaultValue = "false" }
            )
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            val isServiceProvider = backStackEntry.arguments?.getString("isServiceProvider")?.toBoolean() ?: false
            val viewModel: BookingDetailViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            val payfastViewModel: PayfastViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val api = ApiClient.getClient().create(com.example.hustlefix.data.PayfastApi::class.java)
                    return PayfastViewModel(com.example.hustlefix.data.PayfastRepository(api)) as T
                }
            })
            val payfastState by payfastViewModel.uiState.collectAsState()

            LaunchedEffect(payfastState.checkoutUrl) {
                payfastState.checkoutUrl?.let { url ->
                    navController.navigate(Screen.PayfastCheckout.createRoute(url))
                }
            }

            LaunchedEffect(bookingId) {
                if (bookingId.isNotEmpty()) {
                    viewModel.loadBooking(bookingId)
                }
            }
            
            BookingDetailScreen(
                booking = uiState.booking,
                service = uiState.service,
                isServiceProvider = isServiceProvider,
                isLoading = uiState.isLoading,
                onStatusUpdate = { viewModel.updateStatus(it) },
                onRatingSubmit = { s, c, a -> viewModel.submitRating(s, c, a) },
                onChatClick = {
                    val partnerId = if (isServiceProvider) uiState.booking?.getClientId() else uiState.booking?.getWorkerId()
                    val partnerName = if (isServiceProvider) uiState.booking?.getClientName() else uiState.booking?.getWorkerName()
                    if (partnerId != null) {
                        navController.navigate(Screen.Chat.createRoute(partnerId, partnerName ?: "User"))
                    }
                },
                onTrackClick = { workerId ->
                    if (workerId.isNotEmpty()) {
                        navController.navigate(Screen.LiveTracking.createRoute(workerId))
                    }
                },
                onPayClick = {
                    uiState.booking?.let { booking ->
                        payfastViewModel.initiatePayment(booking)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.PostJob.route) {
            val viewModel: JobViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(uiState.successMessage) {
                if (uiState.successMessage != null) {
                    navController.popBackStack()
                }
            }
            
            PostJobScreen(
                isLoading = uiState.isLoading,
                onPostClick = { t, c, d, a, l -> viewModel.postJob(t, c, d, a, l) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AvailableJobs.route) {
            val viewModel: JobViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            AvailableJobsScreen(
                jobs = uiState.availableJobs,
                isLoading = uiState.isLoading,
                onJobClick = { job ->
                    navController.navigate(Screen.JobDetail.createRoute(job.jobId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.JobDetail.route,
            arguments = listOf(navArgument("jobId") { defaultValue = "" })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            val viewModel: JobViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            val job = uiState.availableJobs.find { it.jobId == jobId }
            
            JobDetailScreen(
                job = job,
                isLoading = uiState.isLoading,
                onQuoteSubmit = { amount, message ->
                    viewModel.submitQuote(jobId, amount, message)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.JobQuotes.route,
            arguments = listOf(navArgument("jobId") { defaultValue = "" })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            val viewModel: JobViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(jobId) {
                viewModel.loadQuotesForJob(jobId)
            }
            
            QuoteListScreen(
                quotes = uiState.quotes,
                isLoading = uiState.isLoading,
                onAcceptQuote = { quote ->
                    val job = uiState.myJobs.find { it.jobId == jobId }
                    if (job != null) {
                        viewModel.acceptQuote(job, quote)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.PayfastCheckout.route,
            arguments = listOf(navArgument("url") { defaultValue = "" })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val viewModel: PayfastViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val api = ApiClient.getClient().create(com.example.hustlefix.data.PayfastApi::class.java)
                    return PayfastViewModel(com.example.hustlefix.data.PayfastRepository(api)) as T
                }
            })
            
            PayfastCheckoutScreen(
                url = java.net.URLDecoder.decode(url, "UTF-8"),
                onSuccess = {
                    viewModel.onPaymentSuccess()
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
