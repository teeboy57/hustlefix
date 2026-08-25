# Jetpack Compose & Material 3 Migration Plan

This plan outlines the complete migration of HustleFix from XML Views to Jetpack Compose with Material Design 3, including dynamic color theming and smooth animations.

## Goals
- **Technology Stack:** XML/Java → Jetpack Compose/Kotlin.
- **Design System:** Material 2 → Material 3 (Modern UI).
- **Theming:** Dynamic Color support (Android 12+).
- **UX:** Smooth navigation transitions and fade-in animations.

## Step 1: Project Modernization
Update Gradle configurations to support Kotlin and Compose.

- **[libs.versions.toml]**: Configure Kotlin 2.0.0 and stable Compose versions.
- **[build.gradle.kts]**: Enable Compose and add core libraries (Material 3, Navigation, ViewModel).

## Step 2: Foundation & Theming
Create the design system in Kotlin.

- **Theme.kt**: Implement `HustleFixTheme` with `dynamicDarkColorScheme` and `dynamicLightColorScheme`.
- **Color.kt/Type.kt**: Define brand-specific colors and modern typography.

## Step 3: Architecture & Navigation
Implement a single-activity architecture for Compose.

- **MainActivity.kt**: Host for the `NavHost`.
- **Navigation.kt**: Define all routes and screen transitions using `AnimatedNavHost`.

## Step 4: Screen Migration (Phased)
We will migrate screens in the following order to ensure a functional app throughout the process:

### Phase 1: Authentication
- **WelcomeScreen**: Animated role selection.
- **LoginScreen**: Modern inputs with social login integration.
- **RegisterScreen**: Smooth multi-step registration.

### Phase 2: Dashboards
- **ClientDashboard**: Category grid, recent activity, and quick actions.
- **ServiceProviderDashboard**: Business stats with visual charts.

### Phase 3: Marketplace & Jobs
- **DiscoveryScreen**: Stunning grid with search and sort.
- **ServiceDetail**: Portfolios and booking actions.
- **MyBookings**: Visual job tracking.

### Phase 4: User Profile & Wallet
- **ProfileScreen**: Avatar management and personal info.
- **WalletScreen**: Balance overview, transactions, and withdrawal flow.

### Phase 5: Communication
- **ChatList**: Conversation inbox.
- **ChatScreen**: Real-time messaging bubbles.

## Verification Plan
- **Stability:** Verify no crashes during navigation.
- **Design:** Check M3 compliance (rounded corners, color accessibility).
- **Functionality:** Test all user flows (Booking, Paying, Chatting).
