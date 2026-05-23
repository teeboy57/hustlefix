# HustleFix

Android marketplace app connecting **clients** with **workers** (Firebase Auth + Realtime Database).

## Open in Android Studio

1. **File → Open** → `AndroidStudioProjects\HustleFix` (folder with `settings.gradle.kts` and `app\`).
2. **Sync Project with Gradle Files** (Gradle **8.7**, AGP **8.5.2**).
3. Run **app** on an emulator or device.

## Command line build

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
cd AndroidStudioProjects\HustleFix
.\gradlew.bat assembleDebug
```

## Firebase setup

- Place your `google-services.json` in `app/`.
- Update Facebook placeholders in `app/src/main/res/values/strings.xml` if using Facebook login.
- Deploy [Realtime Database rules](https://firebase.google.com/docs/database/security) appropriate for your project.

## Roles

| Welcome screen | App role | Firebase `users/{uid}/role` |
|----------------|----------|-------------------------------|
| Entrepreneur   | `ENTREPRENEUR` | `worker` |
| Client         | `CLIENT`       | `client` |

## Project layout

- `settings.gradle.kts` / `build.gradle.kts` — root Gradle (Kotlin DSL)
- `gradle/libs.versions.toml` — dependency versions
- `app/build.gradle.kts` — Android app module
- `SessionHelper` — login session and navigation
- `ChatLauncher` / `ChatListActivity` — messaging
