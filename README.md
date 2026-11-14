# AniMate Android — v1.9.6

AniMate is a native Android companion app with a customizable on-screen mascot named Mika. It is designed for Android 6.0+ and currently targets SDK 32 (compiled against SDK 35).

## Current features

- Select up to five installed launcher apps for tracking.
- Grant Usage Access and overlay permission from the app.
- Show Mika as a draggable floating companion while a selected app is in the foreground.
- Save Mika's floating position and restore it when the service starts; keep it within screen bounds.
- Set a separate daily usage reminder limit for each selected app (1–1440 minutes; default 60).
- Receive an early reminder at 80% of each app's daily limit and a separate limit-reached notification, each once per app per local day. Tapping either notification opens AniMate.
- Show Mika's sleepy mood when a selected app reaches its daily limit.
- Choose Mika's mood: Idle, Happy, Sleepy, Sad, Angry, Surprised, Shy, or Thinking.
- Customize Mika's hair and outfit colors.
- View selected-app usage summaries and a seven-day overview.
- Run a 30-minute focus timer followed by a 20-minute rest timer.
- Add the AniMate home-screen widget and select Mika live wallpaper through Android's system pickers.

## Build and download

Every push to `main` runs the **Build AniMate APK** workflow. A successful run publishes:

1. **AniMate-debug-apk** — ZIP containing `app-debug.apk`.
2. **AniMate-v1.9.6-source** — versioned Android project source ZIP.

Open the [latest GitHub Actions runs](https://github.com/45hique45-prog/AniMate-Android/actions) and download the artifacts from the latest successful run.

Repository: https://github.com/45hique45-prog/AniMate-Android

## First launch

1. Install `app-debug.apk` from the APK artifact ZIP.
2. Open AniMate and choose up to five apps.
3. Enable AniMate under Android's Usage Access settings.
4. Grant Display over other apps permission.
5. Set daily limits if you want app-specific reminders.
6. Tap **Start Companion**. Use the stop control to stop the floating service.
7. To add the widget, long-press an empty home-screen area, open Widgets, and place AniMate.
8. To use Mika live wallpaper, open the Live Wallpaper option and confirm it in Android's wallpaper picker.

## Important notes and limitations

- Daily limits are reminders only; AniMate does not block or lock selected apps.
- Usage reporting depends on Android Usage Access and UsageStats; Android may delay or aggregate usage events.
- Android 13+ may require notifications to be allowed in system settings.
- The focus/rest timer currently runs while the activity remains alive; closing the app/activity can stop the timer.
- Widget and live wallpaper components are included, but launcher-specific behavior can vary.
- Android 10+ foreground detection uses activity-resumed/paused events, and the first service check reconstructs the currently open app so Mika can appear without requiring an app switch.
- The APK is built and its package/manifest are verified in CI. It has **not** been physically tested on a Redmi Note 9 Pro, so device-specific behavior is not guaranteed.
- The final detailed illustrated Mika character artwork and a full UI/animation polish pass remain future work.

## Technical details

- Application ID: `com.animate.companion`
- Minimum SDK: 23
- Target SDK: 32
- Compile SDK: 35
- Java compatibility: 8
- CI JDK: 17
