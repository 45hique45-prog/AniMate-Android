# AniMate Android

A native Java Android starter for the AniMate floating companion.

## Current MVP
- Select up to five launcher apps manually.
- Dashboard links to Android Usage Access and overlay permission settings.
- Floating mascot overlay with tap reaction and drag movement.
- Overlay visibility follows selected apps using Android UsageEvents polling.
- Android 6.0+ compatibility target; designed to build on JDK 17 / Android Gradle Plugin 8.7.3.

## Build
GitHub Actions builds a debug APK on every push to `main` and on manual workflow dispatch. Open the repository's **Actions** tab and download the `AniMate-debug-apk` artifact from a successful run.

## Important limitations in this starter
This is a reconstructed MVP scaffold because the earlier v1.2 source archive was not available in the current workspace. Per-app daily usage totals/limits, notifications, saved overlay position, polished mascot animations, widget, and live wallpaper are not implemented yet. The app does not block other apps. Device testing has not yet been performed.

Repository: https://github.com/45hique45-prog/AniMate-Android
