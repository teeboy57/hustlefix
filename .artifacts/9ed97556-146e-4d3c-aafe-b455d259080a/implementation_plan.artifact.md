# M3 and Compose Migration Plan

Update all app screens to Jetpack Compose with Material Design 3, dynamic color, modern typography, and smooth navigation animations.

## User Review Required

> [!IMPORTANT]
> The migration involves moving from a multi-Activity architecture to a single-Activity architecture with Compose Navigation. This will change how deep links and some intent-based interactions work.

## Proposed Changes

### Theme and Styling
Update the theme to fully leverage M3 features, including dynamic color and a refined typography scale.

#### [MODIFY] [Color.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/theme/Color.kt)
#### [MODIFY] [Type.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/theme/Type.kt)
#### [MODIFY] [Theme.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/theme/Theme.kt)

### Navigation
Implement a single-Activity host with `NavHost` and modern animations.

#### [NEW] [NavGraph.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/navigation/NavGraph.kt)
#### [NEW] [MainActivity.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/MainActivity.kt)
#### [DELETE] [MainActivity.java](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/MainActivity.java)

### Screen Refactoring
Ensure all existing Compose screens are updated to use M3 components and consistent styling.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/screens/LoginScreen.kt)
#### [MODIFY] [WelcomeScreen.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/screens/WelcomeScreen.kt)
#### [MODIFY] [RegisterScreen.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/screens/RegisterScreen.kt)
#### [MODIFY] [ClientDashboardScreen.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/screens/ClientDashboardScreen.kt)

## Verification Plan

### Automated Tests
- Run `gradlew assembleDebug` to ensure compilation.
- Create unit tests for navigation logic.

### Manual Verification
- Verify dynamic color changes on Android 12+.
- Verify rounded button corners across all screens.
- Check smooth transitions between Welcome, Login, and Dashboard.
