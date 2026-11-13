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
    WindowManager wm;
    TextView bubble;
    WindowManager.LayoutParams lp;
    Handler handler=new Handler();
    boolean happy=false;
    float pulse=0f;
    String foregroundNow="";
    Runnable monitor=new Runnable(){public void run(){
        try {
            UsageStatsManager us=(UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
            long now=System.currentTimeMillis();
            UsageEvents ev=us.queryEvents(now-10000,now);
            UsageEvents.Event e=new UsageEvents.Event();
            while(ev.hasNextEvent()){
                ev.getNextEvent(e);
                if(e.getEventType()==UsageEvents.Event.MOVE_TO_FOREGROUND) foregroundNow=e.getPackageName();
            }
            Set<String> chosen=getSharedPreferences(MainActivity.PREF,0).getStringSet(MainActivity.APPS,new HashSet<>());
            if(bubble!=null) bubble.setVisibility(chosen.contains(foregroundNow)?View.VISIBLE:View.GONE);
            Calendar cal=Calendar.getInstance(); cal.set(Calendar.HOUR_OF_DAY,0);cal.set(Calendar.MINUTE,0);cal.set(Calendar.SECOND,0);cal.set(Calendar.MILLISECOND,0);
            Map<String,UsageStats> totals=us.queryAndAggregateUsageStats(cal.getTimeInMillis(),now);
            boolean sleepy=false;
            for(String pkg:chosen){
                int limit=getSharedPreferences(MainActivity.PREF,0).getInt("limit_"+pkg,60);
                UsageStats stat=totals.get(pkg);
                long used=stat==null?0:stat.getTotalTimeInForeground();
                if(used>=limit*60000L){
                    if(pkg.equals(foregroundNow))sleepy=true;
                    String day=new SimpleDateFormat("yyyyMMdd",Locale.US).format(new Date());
                    String key="limit_notified_"+day+"_"+pkg;
                    android.content.SharedPreferences prefs=getSharedPreferences(MainActivity.PREF,0);
                    if(!prefs.getBoolean(key,false)){sendLimitNotification(pkg,limit);prefs.edit().putBoolean(key,true).apply();}
                }
            }
            if(bubble!=null&&!happy){
                bubble.setText(sleepy?"😴":"🐱");
                pulse+=0.06f;
                bubble.setAlpha(0.86f+(float)Math.sin(pulse)*0.14f);
            }
        } catch(Exception ignored){}
        handler.postDelayed(this,2500);
    }};
    void sendLimitNotification(String pkg,int limit){
        try{
            NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel("animate_limits","AniMate reminders",NotificationManager.IMPORTANCE_DEFAULT));
            String label=pkg;
            try{label=getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)).toString();}catch(Exception ignored){}
            Notification n;
            if(Build.VERSION.SDK_INT>=26)n=new Notification.Builder(this,"animate_limits").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("AniMate daily limit reached").setContentText(label+" reached its "+limit+" minute daily limit").setAutoCancel(true).build();
            else n=new Notification.Builder(this).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("AniMate daily limit reached").setContentText(label+" reached its "+limit+" minute daily limit").setAutoCancel(true).build();
            nm.notify(Math.abs(pkg.hashCode()),n);
        }catch(Exception ignored){}
    }
    @Override public void onCreate(){
        super.onCreate();
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        android.content.SharedPreferences prefs=getSharedPreferences(MainActivity.PREF,0);
        int x=prefs.getInt("overlay_x",40), y=prefs.getInt("overlay_y",180);
        bubble=new TextView(this);
        bubble.setText("🐱"); bubble.setTextSize(30); bubble.setPadding(12,8,12,8);
        bubble.setBackgroundColor(0xDDE8DDFB);
        int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
        lp=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.TOP|Gravity.LEFT; lp.x=x; lp.y=y;
        try{wm.addView(bubble,lp);}catch(Exception ex){stopSelf();return;}
        bubble.setOnTouchListener(new View.OnTouchListener(){
            float sx,sy; int ox,oy; long down;
            public boolean onTouch(View v,android.view.MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){sx=e.getRawX();sy=e.getRawY();ox=lp.x;oy=lp.y;down=System.currentTimeMillis();return true;}
                if(e.getAction()==MotionEvent.ACTION_MOVE){
                    lp.x=ox+(int)(e.getRawX()-sx); lp.y=oy+(int)(e.getRawY()-sy);
                    try{wm.updateViewLayout(bubble,lp);}catch(Exception ignored){}
                    getSharedPreferences(MainActivity.PREF,0).edit().putInt("overlay_x",lp.x).putInt("overlay_y",lp.y).apply();
                    return true;
                }
                if(e.getAction()==MotionEvent.ACTION_UP){
                    if(System.currentTimeMillis()-down<220){
                        happy=true; bubble.setText("😸"); bubble.setAlpha(1f);
                        handler.postDelayed(()->{if(bubble!=null){happy=false;}},1000);
                    }
                    return true;
                }
                return true;
            }
        });
        handler.post(monitor);
    }
    @Override public int onStartCommand(Intent i,int f,int id){return START_STICKY;}
    @Override public void onDestroy(){handler.removeCallbacks(monitor);if(bubble!=null){try{wm.removeView(bubble);}catch(Exception ignored){}}super.onDestroy();}
    @Override public android.os.IBinder onBind(Intent i){return null;}
}
