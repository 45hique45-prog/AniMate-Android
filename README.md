# AniMate Android — v1.0 MVP

AniMate is a native Java Android floating companion for Android 6.0+ (target SDK 30; built with JDK 17 and Android Gradle Plugin 8.7.3).

## Included in v1.0
- Manually select up to five launcher apps.
- Grant Usage Access and Display over other apps from the dashboard.
- Start and stop the floating cat companion.
- Companion appears while one of the selected apps is in the foreground.
- Drag the companion; its screen position is saved and restored.
- Idle pulse and tap-to-Happy reaction.
- Configure a daily usage limit for each selected app (1–1440 minutes; default 60).
- When a selected app reaches its daily limit, AniMate sends one reminder notification per app per day and shows the Sleepy face while that app is foregrounded.
- Foreground service notification while the companion is running.

## Build and download
GitHub Actions builds a debug APK on every push to `main`. Open **Actions**, select the latest successful **Build AniMate APK** run, and download its `AniMate-debug-apk` artifact ZIP. Extract the ZIP to get `app-debug.apk`.

Latest verified successful build: https://github.com/45hique45-prog/AniMate-Android/actions/runs/43410580881

## First launch
1. Install the APK on the Android phone.
2. Open AniMate and choose up to five apps.
3. Open **Allow usage access** and enable AniMate.
4. Open **Allow display over other apps** and allow it.
5. Set each app's daily limit.
6. Tap **Start AniMate**. Use **Stop AniMate** to stop the companion.

## Important notes
- Daily limits are reminders only. AniMate does not block or lock selected apps.
- Usage reporting depends on Android Usage Access and Android's usage statistics. Android may delay or aggregate some usage events.
- This APK was successfully compiled by GitHub Actions. It has **not yet been physically tested on a Redmi Note 9 Pro**, so device-specific behavior is not guaranteed.
- Home-screen widget and live wallpaper are not part of v1.0.

Repository: https://github.com/45hique45-prog/AniMate-Android
