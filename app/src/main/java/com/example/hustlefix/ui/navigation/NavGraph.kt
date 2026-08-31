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
    object Welcome : Screen("welcome?reason={reason}") {
        fun createRoute(reason: String? = null): String {
            return if (reason != null) {
                val encoded = android.net.Uri.encode(reason)
                "welcome?reason=$encoded"
            } else "welcome"
        }
    }
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ClientDashboard : Screen("client_dashboard")
    object ServiceProviderDashboard : Screen("service_provider_dashboard")
    object AdminDashboard : Screen("admin_dashboard")
    object MyBookings : Screen("my_bookings?status={status}") {
        fun createRoute(status: String = "all") = "my_bookings?status=$status"
    }
    object FindServices : Screen("find_services?category={category}&query={query}") {
        fun createRoute(category: String = "All", query: String = "") = "find_services?category=$category&query=$query"
    }
    object Profile : Screen("profile")
    object Wallet : Screen("wallet")
    object WithdrawalRequest : Screen("withdrawal_request")
    object Settings : Screen("settings")
    object PostService : Screen("post_service")
    object EditService : Screen("edit_service/{serviceId}") {
        fun createRoute(serviceId: String) = "edit_service/$serviceId"
    }
    object MyServices : Screen("my_services")
    object Analytics : Screen("analytics")
    object Notifications : Screen("notifications")
    object Emergency : Screen("emergency")
    object Map : Screen("map")
    object Verification : Screen("verification")
    object ReportUser : Screen("report_user/{userId}/{userName}") {
        fun createRoute(userId: String, userName: String) = "report_user/$userId/${java.net.URLEncoder.encode(userName, "UTF-8")}"
    }
    object UrgentJobs : Screen("urgent_jobs")
    object Insights : Screen("insights")
    object IncomeStatement : Screen("income_statement")
    object LiveTracking : Screen("live_tracking/{workerId}") {
        fun createRoute(workerId: String) = "live_tracking/$workerId"
    }
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{partnerId}/{partnerName}") {
        fun createRoute(partnerId: String, partnerName: String) = "chat/$partnerId/${java.net.URLEncoder.encode(partnerName, "UTF-8")}"
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
            val destination = if (role == "admin") {
                Screen.AdminDashboard.route
            } else if (role == "service_provider") {
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
        composable(
            route = Screen.Welcome.route,
            arguments = listOf(navArgument("reason") { defaultValue = null; nullable = true })
        ) { backStackEntry ->
            val reason = backStackEntry.arguments?.getString("reason")
            val chatViewModel: ChatViewModel = viewModel()
            
            WelcomeScreen(
                suspensionReason = reason,
                onServiceProviderClick = { 
                    SessionHelper.saveRole(context, "service_provider")
                    navController.navigate(Screen.Onboarding.route) 
                },
                onClientClick = { 
                    SessionHelper.saveRole(context, "client")
                    navController.navigate(Screen.Onboarding.route)
                },
                onSupportClick = {
                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
                    chatViewModel.sendSystemMessage("admin_support", "HustleFix Support", "Support Request: I need help regarding my account suspension. (User ID: $uid, Reason: $reason)")
                    navController.navigate(Screen.Chat.createRoute("admin_support", "HustleFix Support"))
                },
                onDismissSuspension = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    SessionHelper.logout(context)
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { e, p -> 
                    val expectedRole = SessionHelper.getRole(context)
                    authViewModel.login(e, p, expectedRole, context) 
                },
                onRegisterClick = { 
                    navController.navigate(Screen.Register.route) 
                },
                onResetPassword = { authViewModel.resetPassword(it) },
                onGoogleLoginClick = { /* Handle */ },
                onAppleLoginClick = { /* Handle */ },
                isLoading = authState.isLoading,
                error = authState.error,
                isResetSent = authState.isResetSent,
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
                walletBalance = uiState.walletBalance,
                totalBookings = uiState.totalBookings,
                activeBookings = uiState.activeBookings,
                completedBookings = uiState.completedBookings,
                recentBookings = uiState.recentBookings,
                upcomingBooking = uiState.upcomingBooking,
                unreadMessagesCount = uiState.unreadMessagesCount,
                nearbyServices = uiState.nearbyServices,
                unreadNotifications = notifState.unreadCount,
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                onCategoryClick = { category -> 
                    navController.navigate(Screen.FindServices.createRoute(category))
                },
                onQuickActionClick = { action ->
                    if (action.startsWith("find_query")) {
                        val query = action.substringAfter("q=")
                        navController.navigate(Screen.FindServices.createRoute(query = query))
                    } else if (action.startsWith("chat_")) {
                        val pid = action.substringAfter("chat_")
                        navController.navigate(Screen.Chat.createRoute(pid, "Provider"))
                    } else {
                        when (action) {
                            "find" -> navController.navigate(Screen.Map.route)
                            "bookings" -> navController.navigate(Screen.MyBookings.createRoute("all"))
                            "active_bookings" -> navController.navigate(Screen.MyBookings.createRoute("active"))
                            "done_bookings" -> navController.navigate(Screen.MyBookings.createRoute("completed"))
                            "saved" -> navController.navigate(Screen.SavedServices.route)
                            "messages" -> navController.navigate(Screen.ChatList.route)
                            "notifications" -> navController.navigate(Screen.Notifications.route)
                            "emergency" -> navController.navigate(Screen.Emergency.route)
                            "wallet" -> navController.navigate(Screen.Wallet.route)
                        }
                    }
                },
                onBookingClick = { booking ->
                    navController.navigate("booking_detail/${booking.bookingId}")
                },
                onServiceClick = { service ->
                    navController.navigate(Screen.ServiceDetail.createRoute(service.serviceId ?: ""))
                },
                onMenuClick = onMenuClick
            )
        }

        composable(Screen.AdminDashboard.route) {
            val viewModel: AdminDashboardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return AdminDashboardViewModel() as T
                }
            })
            val uiState by viewModel.uiState.collectAsState()

            AdminDashboardScreen(
                profitBalance = uiState.profitBalance,
                totalUsers = uiState.totalUsers,
                totalJobs = uiState.totalJobs,
                pendingVerifications = uiState.pendingVerifications,
                activeEmergencies = uiState.activeEmergencies,
                isLoading = uiState.isLoading,
                onMenuClick = onMenuClick,
                onQuickActionClick = { action ->
                    when (action) {
                        "verifications" -> { /* Open user management */ }
                        "emergencies" -> navController.navigate(Screen.Emergency.route)
                        "logs" -> { /* Open logs screen */ }
                        "broadcast" -> { /* Open broadcast dialog */ }
                    }
                }
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
                        "work" -> navController.navigate(Screen.Analytics.route)
                        "urgent" -> navController.navigate(Screen.UrgentJobs.route)
                        "bookings" -> navController.navigate(Screen.MyBookings.createRoute("all"))
                        "settings" -> navController.navigate(Screen.Settings.route)
                        "notifications" -> navController.navigate(Screen.Notifications.route)
                        "ratings" -> navController.navigate(Screen.Ratings.route)
                    }
                },
                onBookingClick = { booking ->
                    navController.navigate("booking_detail/${booking.bookingId}?isServiceProvider=true")
                },
                onMenuClick = onMenuClick
            )
        }

        composable(
            route = Screen.MyBookings.route,
            arguments = listOf(navArgument("status") { defaultValue = "all" })
        ) { backStackEntry ->
            val status = backStackEntry.arguments?.getString("status") ?: "all"
            val viewModel: MyBookingsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(status) {
                viewModel.onStatusFilterChange(status)
            }
            
            MyBookingsScreen(
                bookings = uiState.filteredBookings,
                isLoading = uiState.isLoading,
                isServiceProvider = SessionHelper.getRole(context) == "service_provider",
                currentStatus = uiState.currentStatus,
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
            arguments = listOf(
                navArgument("category") { defaultValue = "All" },
                navArgument("query") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "All"
            val query = backStackEntry.arguments?.getString("query") ?: ""
            val viewModel: FindServicesViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(category, query) {
                viewModel.onCategoryChange(category)
                if (query.isNotEmpty()) {
                    viewModel.onSearchQueryChange(query)
                }
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
                onSortClick = { viewModel.onSortToggle() },
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
                reviews = uiState.reviews,
                isSaved = uiState.isSaved,
                isLoading = uiState.isLoading,
                onBackClick = { navController.popBackStack() },
                onBookNowClick = { date, notes ->
                    viewModel.bookService(date, notes) {
                        navController.navigate(Screen.MyBookings.route) {
                            popUpTo(Screen.ServiceDetail.route) { inclusive = true }
                        }
                    }
                },
                onSaveClick = { viewModel.toggleSaveService() },
                onProviderClick = { 
                    val pid = uiState.service?.getserviceProviderId() ?: ""
                    if (pid.isNotEmpty()) {
                        navController.navigate(Screen.WorkerProfile.createRoute(pid))
                    }
                },
                onChatClick = {
                    val pid = uiState.service?.getserviceProviderId() ?: ""
                    val pname = uiState.service?.getserviceProviderName() ?: "Pro"
                    if (pid.isNotEmpty()) {
                        navController.navigate(Screen.Chat.createRoute(pid, pname))
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
                isFlagged = uiState.isFlagged,
                adminNotes = uiState.adminNotes,
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
                onVerificationClick = { navController.navigate(Screen.Verification.route) },
                onSubmitDispute = { viewModel.submitDispute(it) }
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
            
            LaunchedEffect(uiState.topUpCheckoutUrl) {
                uiState.topUpCheckoutUrl?.let { url ->
                    navController.navigate(Screen.PayfastCheckout.createRoute(url))
                    viewModel.clearTopUpUrl()
                }
            }

            WalletScreen(
                balance = uiState.balance,
                transactions = uiState.transactions,
                isLoading = uiState.isLoading,
                onTopUpClick = { viewModel.topUp(it) },
                onWithdrawClick = { navController.navigate(Screen.WithdrawalRequest.route) },
                onStatementClick = { navController.navigate(Screen.IncomeStatement.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.WithdrawalRequest.route) {
            val viewModel: WalletViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isWithdrawalSuccess) {
                if (uiState.isWithdrawalSuccess) {
                    navController.popBackStack()
                }
            }

            WithdrawalRequestScreen(
                availableBalance = uiState.balance,
                isLoading = uiState.isLoading,
                onWithdrawClick = { a, b, h, n, br -> 
                    viewModel.requestWithdrawal(a, b, h, n, br)
                },
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

        composable(
            route = Screen.ReportUser.route,
            arguments = listOf(
                navArgument("userId") { defaultValue = "" },
                navArgument("userName") { defaultValue = "User" }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = try {
                java.net.URLDecoder.decode(backStackEntry.arguments?.getString("userName") ?: "User", "UTF-8")
            } catch (e: Exception) {
                backStackEntry.arguments?.getString("userName") ?: "User"
            }

            ReportUserScreen(
                userId = userId,
                userName = userName,
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
                onServiceClick = { service ->
                    navController.navigate(Screen.EditService.createRoute(service.serviceId))
                },
                onDeleteClick = { viewModel.deleteService(it) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditService.route,
            arguments = listOf(navArgument("serviceId") { defaultValue = "" })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            val viewModel: PostServiceViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(serviceId) {
                viewModel.loadService(serviceId)
            }

            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.popBackStack()
                }
            }

            EditServiceScreen(
                service = uiState.service,
                isLoading = uiState.isLoading,
                onSaveClick = { t, d, c, p, i -> 
                    viewModel.updateService(serviceId, t, d, c, p, i)
                },
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
                onDetailClick = { action ->
                    when (action) {
                        "wallet" -> navController.navigate(Screen.Wallet.route)
                        "ratings" -> navController.navigate(Screen.Ratings.route)
                        "bookings" -> navController.navigate(Screen.MyBookings.createRoute("all"))
                        "bookings_completed" -> navController.navigate(Screen.MyBookings.createRoute("completed"))
                        "bookings_pending" -> navController.navigate(Screen.MyBookings.createRoute("pending"))
                        "bookings_cancelled" -> navController.navigate(Screen.MyBookings.createRoute("cancelled"))
                        "bookings_weekly" -> navController.navigate(Screen.MyBookings.createRoute("all"))
                        "income_statement" -> navController.navigate(Screen.IncomeStatement.route)
                        "clients" -> { /* Future feature */ }
                    }
                },
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
                activeRequest = uiState.activeRequest,
                onSendEmergency = { t, d -> viewModel.sendEmergencyRequest(t, d) },
                onBackClick = { navController.popBackStack() },
                onClearError = { viewModel.clearError() }
            )
        }

        composable(Screen.Map.route) {
            val viewModel: MapViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            FindWorkersScreen(
                workers = uiState.workers,
                services = uiState.services,
                userLat = uiState.userLatitude,
                userLng = uiState.userLongitude,
                isLoading = uiState.isLoading,
                onWorkerClick = { worker ->
                    val wid = worker.id ?: ""
                    if (wid.isNotEmpty()) {
                        navController.navigate(Screen.WorkerProfile.createRoute(wid))
                    }
                },
                onServiceClick = { service ->
                    val sid = service.serviceId ?: ""
                    if (sid.isNotEmpty()) {
                        navController.navigate(Screen.ServiceDetail.createRoute(sid))
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
            val partnerName = try {
                java.net.URLDecoder.decode(backStackEntry.arguments?.getString("partnerName") ?: "Chat", "UTF-8")
            } catch (e: Exception) {
                backStackEntry.arguments?.getString("partnerName") ?: "Chat"
            }
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
                onEditMessage = { mid, text ->
                    if (partnerId.isNotEmpty()) {
                        viewModel.editMessage(partnerId, mid, text)
                    }
                },
                onDeleteMessage = { mid ->
                    if (partnerId.isNotEmpty()) {
                        viewModel.deleteMessage(partnerId, mid)
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
                isVerifyingPayment = uiState.isVerifyingPayment,
                walletBalance = uiState.walletBalance,
                error = uiState.error,
                onClearError = { viewModel.clearStatus() },
                onStatusUpdate = { s, c -> viewModel.updateStatus(s, c) },
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
                        viewModel.startPaymentVerification()
                        payfastViewModel.initiatePayment(booking)
                    }
                },
                onPayWithWalletClick = {
                    viewModel.payWithWallet()
                },
                onSharePayLink = { message ->
                    uiState.booking?.let { booking ->
                        payfastViewModel.getShareableLink(booking) { link ->
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "$message $link")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Invoice"))
                        }
                    }
                },
                onReportClick = { id, name ->
                    navController.navigate(Screen.ReportUser.createRoute(id, name))
                },
                onDisputeSubmit = { reason ->
                    viewModel.submitDispute(reason)
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
                onPostClick = { t, c, d, a, l, dl -> viewModel.postJob(t, c, d, a, l, dl) },
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
                    // Get the booking detail viewmodel if possible, or use a shared state.
                    // Since it's a new screen, we'll rely on the ITN to update the status.
                    // But we can trigger the "Verifying" state in the background.
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
