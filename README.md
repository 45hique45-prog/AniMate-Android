# AniMate Android — v1.3.1

AniMate is a native Java Android floating companion designed for Android 6.0+ phones, including Android 11 devices.

## Included in v1.3.1

- Manually select up to five launcher apps.
- Grant Usage Access and Display over other apps from the dashboard.
- Start and stop the floating companion from the dashboard.
- The companion appears only while a selected app is in the foreground.
- Set a separate daily usage limit for each selected app (1–1440 minutes; default 60).
- Receive one reminder notification per app per day when its limit is reached.
- Show the Sleepy expression while a selected app that has reached its limit is in the foreground.
- Idle breathing animation and a short Happy pop animation when the companion is tapped.
- Save the overlay position after dragging and restore it when the service starts.
- Keep monitoring alive with a foreground-service notification.

## Build and download

Every push to `main` runs the **Build AniMate APK** workflow. A successful run publishes two artifacts:

1. **AniMate-debug-apk** — extract `app-debug.apk` from this ZIP.
2. **AniMate-v1.3.1-source** — the versioned Android project source ZIP.

Open the repository's **Actions** tab, select the latest successful **Build AniMate APK** run, and download both artifacts.

Repository: https://github.com/45hique45-prog/AniMate-Android

## First launch

1. Install the debug APK.
2. Open AniMate and choose up to five apps.
3. Open **Allow usage access** and enable AniMate.
4. Open **Allow floating companion** and grant the overlay permission.
5. Set a daily limit for each selected app.
6. Tap **Start AniMate**. Use **Stop AniMate** to stop the service.

## Important notes

- Daily limits are reminders only; AniMate does not block or lock selected apps.
- Usage reporting depends on Android Usage Access and Android's usage statistics. Android may delay or aggregate usage events.
- Android 13+ may require notifications to be allowed in system settings.
- This source targets SDK 32 and compiles against SDK 35, with Java 8 language compatibility and JDK 17 in CI.
- The APK has not been physically tested on a Redmi Note 9 Pro; device-specific behavior is not guaranteed.
- A home-screen widget, live wallpaper, and the final illustrated anime-girl character artwork are still pending.
