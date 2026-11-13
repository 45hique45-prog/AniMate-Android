package com.animate.companion;


import android.app.*;
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

    private final Runnable clearHappy = () -> { happy = false; if (bubble != null) bubble.setState(MascotView.IDLE); };

    private final Runnable monitor = new Runnable() {
        @Override public void run() {
            try {
                UsageStatsManager us = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
                long now = System.currentTimeMillis();
                UsageEvents events = us.queryEvents(now - 10000L, now);
                if (events != null) {
                    UsageEvents.Event event = new UsageEvents.Event();
                    while (events.hasNextEvent()) {
                        events.getNextEvent(event);
                        if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            foregroundNow = event.getPackageName();
                        } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND
                                && event.getPackageName().equals(foregroundNow)) {
                            foregroundNow = "";
                        }
                    }
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
                        int limit = prefs.getInt("limit_" + pkg, 60);
                        UsageStats stat = totals.get(pkg);
                        long used = stat == null ? 0L : stat.getTotalTimeInForeground();

                        if (used >= limit * 60000L) {
                            if (pkg.equals(foregroundNow)) sleepy = true;

                            String key = "limit_notified_" + day + "_" + pkg;
                            if (!prefs.getBoolean(key, false)) {
                                sendLimitNotification(pkg, limit);
                                prefs.edit().putBoolean(key, true).apply();
                            }
                        }
                    }
                }

                if (bubble != null && !happy) {
                    bubble.setState(sleepy ? MascotView.SLEEPY : prefs.getInt("mika_mood", MascotView.IDLE));
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

    private void sendLimitNotification(String pkg, int limit) {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(new NotificationChannel(
                        "animate_limits", "AniMate reminders", NotificationManager.IMPORTANCE_DEFAULT));
            }

            String label = pkg;
            try {
                label = getPackageManager().getApplicationLabel(
                        getPackageManager().getApplicationInfo(pkg, 0)).toString();
            } catch (Exception ignored) { }

            Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(this, "animate_limits")
                    : new Notification.Builder(this);

            Notification notification = builder
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("AniMate daily limit reached")
                    .setContentText(label + " reached its " + limit
                            + " minute daily limit. Time for a break!")
                    .setAutoCancel(true)
                    .build();

            nm.notify(pkg.hashCode() & 0x7fffffff, notification);
        } catch (Exception ignored) { }
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
        lp.x = x;
        lp.y = y;

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
                        lp.x = originX + (int) dx;
                        lp.y = originY + (int) dy;
                        try {
                            wm.updateViewLayout(bubble, lp);
                        } catch (Exception ignored) { }
                        return true;

                    case MotionEvent.ACTION_UP:
                        savePosition();
                        if (!moved && System.currentTimeMillis() - downTime < 350L) {
                            playHappyReaction();
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
