package com.example.hustlefix.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hustlefix.ui.screens.*

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object ClientDashboard : Screen("client_dashboard")
    object ServiceProviderDashboard : Screen("service_provider_dashboard")
}

@Composable
fun HustleFixNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Welcome.route
) {
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
                    navController.navigate(Screen.Register.route) 
                },
                onClientClick = { 
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { _, _ -> 
                    // To be handled by Activity/ViewModel
                },
                onRegisterClick = { 
                    navController.navigate(Screen.Register.route) 
                },
                onForgotPasswordClick = { /* Handle */ },
                onGoogleLoginClick = { /* Handle */ },
                onAppleLoginClick = { /* Handle */ }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = { _, _, _, _ -> 
                    // To be handled by Activity/ViewModel
                },
                onLoginClick = { 
                    navController.navigate(Screen.Login.route) 
                }
            )
        }

        composable(Screen.ClientDashboard.route) {
            ClientDashboardScreen()
        }

        composable(Screen.ServiceProviderDashboard.route) {
            ServiceProviderDashboardScreen()
        }
    }
}
