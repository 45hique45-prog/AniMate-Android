package com.animate.companion;


import android.app.*;
import android.appwidget.AppWidgetManager;
import android.app.usage.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class OverlayService extends Service {
    private WindowManager wm;
    private MascotView bubble;
    private WindowManager.LayoutParams lp;
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    private boolean happy = false;
    private String foregroundNow = "";
    private boolean foregroundInitialized = false;
    private int lastWidgetMood = -1;

    private final Runnable clearHappy = () -> {
        happy = false;
        if (bubble != null) {
            int savedMood = getSharedPreferences(MainActivity.PREF, 0)
                    .getInt("mika_mood", MascotView.IDLE);
            bubble.setState(savedMood);
        }
    };

    private final Runnable monitor = new Runnable() {
        @Override public void run() {
            try {
                UsageStatsManager us = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
                long now = System.currentTimeMillis();
                // On first start, reconstruct the current foreground app from recent events.
                // Android 10+ uses ACTIVITY_RESUMED/PAUSED; older versions use MOVE_TO_*.
                long eventStart = foregroundInitialized ? now - 10000L : now - 24L * 60L * 60L * 1000L;
                UsageEvents events = us.queryEvents(eventStart, now);
                if (events != null) {
                    UsageEvents.Event event = new UsageEvents.Event();
                    while (events.hasNextEvent()) {
                        events.getNextEvent(event);
                        String eventPackage = event.getPackageName();
                        if (eventPackage == null) continue;
                        int eventType = event.getEventType();
                        boolean resumed = Build.VERSION.SDK_INT >= 29
                                ? eventType == UsageEvents.Event.ACTIVITY_RESUMED
                                : eventType == UsageEvents.Event.MOVE_TO_FOREGROUND;
                        boolean paused = Build.VERSION.SDK_INT >= 29
                                ? eventType == UsageEvents.Event.ACTIVITY_PAUSED
                                : eventType == UsageEvents.Event.MOVE_TO_BACKGROUND;
                        if (resumed) {
                            foregroundNow = eventPackage;
                        } else if (paused && eventPackage.equals(foregroundNow)) {
                            foregroundNow = "";
                        }
                    }
                    foregroundInitialized = true;
                }

                Set<String> chosen = getSharedPreferences(MainActivity.PREF, 0)
                        .getStringSet(MainActivity.APPS, new HashSet<String>());
                if (bubble != null) {
                    bubble.setVisibility(chosen.contains(foregroundNow) ? View.VISIBLE : View.GONE);
                }

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                Map<String, UsageStats> totals =
                        us.queryAndAggregateUsageStats(cal.getTimeInMillis(), now);
                boolean sleepy = false;
                String day = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
                android.content.SharedPreferences prefs = getSharedPreferences(MainActivity.PREF, 0);

                if (totals != null) {
                    for (String pkg : chosen) {
                        int limit = Math.max(1, Math.min(1440, prefs.getInt("limit_" + pkg, 60)));
                        UsageStats stat = totals.get(pkg);
                        long used = stat == null ? 0L : stat.getTotalTimeInForeground();
                        long limitMs = limit * 60000L;

                        String warningKey = "limit_warning_80_" + day + "_" + pkg;
                        String reachedKey = "limit_notified_" + day + "_" + pkg;
                        if (used >= limitMs) {
                            if (pkg.equals(foregroundNow)) sleepy = true;
                            if (!prefs.getBoolean(reachedKey, false)
                                    && sendUsageNotification(pkg, limit, used, true)) {
                                prefs.edit().putBoolean(reachedKey, true).apply();
                            }
                        } else if (used >= (limitMs * 80L / 100L)
                                && !prefs.getBoolean(warningKey, false)) {
                            if (sendUsageNotification(pkg, limit, used, false)) {
                                prefs.edit().putBoolean(warningKey, true).apply();
                            }
                        }
                    }
                }

                int displayedMood = sleepy ? MascotView.SLEEPY
                        : prefs.getInt("mika_mood", MascotView.IDLE);
                if (bubble != null && !happy) {
                    bubble.setState(displayedMood);
                }
                // Refresh the home-screen widget only when Mika's displayed mood changes.
                if (displayedMood != lastWidgetMood) {
                    lastWidgetMood = displayedMood;
                    Intent widgetUpdate = new Intent(OverlayService.this, AniMateWidget.class);
                    widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int[] widgetIds = AppWidgetManager.getInstance(OverlayService.this)
                            .getAppWidgetIds(new ComponentName(OverlayService.this, AniMateWidget.class));
                    widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
                    sendBroadcast(widgetUpdate);
                }
            } catch (Exception ignored) {
                // Usage Access can be revoked while the service is running.
            }
            handler.postDelayed(this, 2500L);
        }
    };

    private void playHappyReaction() {
        if (bubble == null) return;
        happy = true;
        handler.removeCallbacks(clearHappy);
        bubble.setState(MascotView.HAPPY);
        bubble.setAlpha(1f);
        bubble.animate().cancel();
        bubble.setScaleX(1f);
        bubble.setScaleY(1f);
        bubble.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(150L)
                .withEndAction(() -> {
                    if (bubble != null) {
                        bubble.animate().scaleX(1f).scaleY(1f).setDuration(190L).start();
                    }
                })
                .start();
        handler.postDelayed(clearHappy, 1000L);
    }

    private void savePosition() {
        if (lp == null) return;
        getSharedPreferences(MainActivity.PREF, 0).edit()
                .putInt("overlay_x", lp.x)
                .putInt("overlay_y", lp.y)
                .apply();
    }

    private int notificationId(String pkg, boolean reached) {
        int hash = 0x811c9dc5;
        for (int i = 0; i < pkg.length(); i++) {
            hash ^= pkg.charAt(i);
            hash *= 0x01000193;
        }
        return (hash & 0x3fffffff) | (reached ? 0x40000000 : 0);
    }

    private boolean sendUsageNotification(String pkg, int limit, long usedMs, boolean reached) {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm == null) return false;
            if (Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(new NotificationChannel(
                        "animate_limits", "AniMate reminders", NotificationManager.IMPORTANCE_DEFAULT));
            }

            String label = pkg;
            try {
                label = getPackageManager().getApplicationLabel(
                        getPackageManager().getApplicationInfo(pkg, 0)).toString();
            } catch (Exception ignored) { }

            long usedMinutes = (usedMs + 59999L) / 60000L;
            String usage = Math.min(usedMinutes, (long) limit) + " / " + limit + " min used";
            String title = reached
                    ? "Mika says: daily limit reached"
                    : "Mika says: almost time for a break";
            String body = reached
                    ? label + " · " + usage + ". Take a little break."
                    : label + " · " + usage + " (80% of your daily limit).";

            int id = notificationId(pkg, reached);
            Intent open = new Intent(this, MainActivity.class);
            open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 23) pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
            PendingIntent contentIntent = PendingIntent.getActivity(this, id, open, pendingFlags);

            Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(this, "animate_limits")
                    : new Notification.Builder(this);

            Notification notification = builder
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(new Notification.BigTextStyle().bigText(body))
                    .setContentIntent(contentIntent)
                    .setCategory(Notification.CATEGORY_REMINDER)
                    .setAutoCancel(true)
                    .build();

            nm.notify(id, notification);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void startAsForeground() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel(
                    "animate_service", "AniMate companion", NotificationManager.IMPORTANCE_LOW));
        }

        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, "animate_service")
                : new Notification.Builder(this);

        startForeground(101, builder
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("AniMate is active")
                .setContentText("Your companion is monitoring selected apps")
                .setOngoing(true)
                .build());
    }

    @Override public void onCreate() {
        super.onCreate();
        startAsForeground();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        android.content.SharedPreferences prefs = getSharedPreferences(MainActivity.PREF, 0);
        int x = prefs.getInt("overlay_x", 40);
        int y = prefs.getInt("overlay_y", 180);

        bubble = new MascotView(this);

        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        lp = new WindowManager.LayoutParams(
                Math.round(76 * getResources().getDisplayMetrics().density),
                Math.round(84 * getResources().getDisplayMetrics().density),
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.LEFT;
        android.util.DisplayMetrics screen = new android.util.DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(screen);
        lp.x = Math.max(0, Math.min(Math.max(0, screen.widthPixels - lp.width), x));
        lp.y = Math.max(0, Math.min(Math.max(0, screen.heightPixels - lp.height), y));

        try {
            wm.addView(bubble, lp);
        } catch (Exception ex) {
            stopSelf();
            return;
        }

        final int touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        bubble.setOnTouchListener(new View.OnTouchListener() {
            float startX, startY;
            int originX, originY;
            long downTime;
            boolean moved;

            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        originX = lp.x;
                        originY = lp.y;
                        downTime = System.currentTimeMillis();
                        moved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - startX;
                        float dy = event.getRawY() - startY;
                        if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
                            moved = true;
                        }
                        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
                        wm.getDefaultDisplay().getRealMetrics(metrics);
                        int maxX = Math.max(0, metrics.widthPixels - lp.width);
                        int maxY = Math.max(0, metrics.heightPixels - lp.height);
                        lp.x = Math.max(0, Math.min(maxX, originX + (int) dx));
                        lp.y = Math.max(0, Math.min(maxY, originY + (int) dy));
                        try {
                            wm.updateViewLayout(bubble, lp);
                        } catch (Exception ignored) { }
                        return true;

                    case MotionEvent.ACTION_UP:
                        savePosition();
                        if (!moved) {
                            long pressDuration = System.currentTimeMillis() - downTime;
                            if (pressDuration < 350L) {
                                playHappyReaction();
                            } else if (pressDuration >= 500L) {
                                Intent open = new Intent(OverlayService.this, MainActivity.class);
                                open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(open);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        savePosition();
                        return true;

                    default:
                        return true;
                }
            }
        });

        handler.post(monitor);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onDestroy() {
        handler.removeCallbacks(monitor);
        handler.removeCallbacks(clearHappy);
        if (bubble != null) {
            bubble.animate().cancel();
            try {
                wm.removeView(bubble);
            } catch (Exception ignored) { }
            bubble = null;
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }
}
