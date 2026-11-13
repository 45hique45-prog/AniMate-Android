package com.animate.companion;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class AniMateWidget extends AppWidgetProvider {
    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_animate);
            Intent open = new Intent(context, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, open,
                    PendingIntent.FLAG_UPDATE_CURRENT | (android.os.Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
            views.setOnClickPendingIntent(R.id.widget_root, pi);
            views.setOnClickPendingIntent(R.id.widget_open, pi);
            int mood=context.getSharedPreferences(MainActivity.PREF,0).getInt("mika_mood",0);
            String[] moods={"Mika is here for you ♡","Mika is happy today! ♡","Mika feels sleepy…","Mika feels a little sad","Mika is grumpy!","Mika is surprised!","Mika is feeling shy","Mika is thinking…"};
            views.setTextViewText(R.id.widget_greeting, moods[Math.max(0,Math.min(mood,moods.length-1))]);
            manager.updateAppWidget(id, views);
        }
    }
}