package com.animate.companion;

import android.app.*;
import android.os.*;
import android.content.*;
import android.app.usage.*;
import android.graphics.PixelFormat;
import android.view.*;
import android.widget.*;
import android.provider.Settings;
import java.util.*;

public class CompanionService extends Service {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private WindowManager wm; private TextView pet; private WindowManager.LayoutParams lp;
    private android.content.SharedPreferences prefs;
    private final Runnable tick=new Runnable(){@Override public void run(){checkApp();handler.postDelayed(this,1800);}};
    @Override public void onCreate(){super.onCreate();prefs=getSharedPreferences(MainActivity.PREF,0);wm=(WindowManager)getSystemService(WINDOW_SERVICE);showPet();handler.post(tick);}
    private void showPet(){
        if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){stopSelf();return;}
        pet=new TextView(this);pet.setText("૮ ˶ᵔ ᵕ ᵔ˶ ა");pet.setTextSize(27);pet.setPadding(14,9,14,9);pet.setTextColor(0xffffffff);pet.setBackgroundColor(0xdd6b4b86);
        pet.setOnClickListener(v->{pet.setText("૮ ˶ᵔ ᵕ ᵔ˶ ა ♥");handler.postDelayed(()->{if(pet!=null)pet.setText("૮ ˶ᵔ ᵕ ᵔ˶ ა");},2200);});
        int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
        lp=new WindowManager.LayoutParams(-2,-2,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.TOP|Gravity.START;lp.x=prefs.getInt("x",80);lp.y=prefs.getInt("y",250);
        pet.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int ox,oy;public boolean onTouch(View v,MotionEvent e){
            if(e.getAction()==MotionEvent.ACTION_DOWN){sx=e.getRawX();sy=e.getRawY();ox=lp.x;oy=lp.y;return true;}
            if(e.getAction()==MotionEvent.ACTION_UP){if(Math.abs(e.getRawX()-sx)<8&&Math.abs(e.getRawY()-sy)<8)pet.performClick();prefs.edit().putInt("x",lp.x).putInt("y",lp.y).apply();return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){lp.x=ox+(int)(e.getRawX()-sx);lp.y=oy+(int)(e.getRawY()-sy);try{wm.updateViewLayout(pet,lp);}catch(Exception ignored){}return true;}return true;}});
        try{wm.addView(pet,lp);}catch(Exception ignored){}
    }
    private void checkApp(){
        try{
            UsageStatsManager us=(UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);long now=System.currentTimeMillis();
            List<UsageStats> recent=us.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,now-120000,now);if(recent==null)return;
            String foreground=null;long latest=0;for(UsageStats u:recent)if(u.getLastTimeUsed()>latest){latest=u.getLastTimeUsed();foreground=u.getPackageName();}
            Set<String> chosen=prefs.getStringSet("selected",new HashSet<String>());boolean visible=foreground!=null&&chosen.contains(foreground);
            if(pet!=null)pet.setVisibility(visible?View.VISIBLE:View.GONE);
            if(visible){int limit=prefs.getInt("limit_"+foreground,60);long start=dayStart();List<UsageStats> daily=us.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,start,now);long used=0;
                if(daily!=null)for(UsageStats u:daily)if(foreground.equals(u.getPackageName()))used=u.getTotalTimeInForeground();
                String day=new java.text.SimpleDateFormat("yyyyMMdd",Locale.US).format(new Date());String key="notified_"+day+"_"+foreground;
                if(used>=limit*60000L&&!prefs.getBoolean(key,false)){notifyLimit(foreground);prefs.edit().putBoolean(key,true).apply();if(pet!=null)pet.setText("૮ ˶•́ ﻌ •̀˶ ა");}
            }
        }catch(Exception ignored){}
    }
    private long dayStart(){Calendar c=Calendar.getInstance();c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return c.getTimeInMillis();}
    private void notifyLimit(String pkg){
        String name=pkg;try{name=getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)).toString();}catch(Exception ignored){}
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel("limits","Usage limits",NotificationManager.IMPORTANCE_DEFAULT));
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"limits"):new Notification.Builder(this);
        nm.notify(pkg.hashCode(),b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("AniMate • Time for a break").setContentText(name+" reached its daily limit. Take a little rest!").setAutoCancel(true).build());
    }
    @Override public int onStartCommand(Intent i,int flags,int startId){return START_STICKY;}
    @Override public void onDestroy(){handler.removeCallbacks(tick);if(pet!=null)try{wm.removeView(pet);}catch(Exception ignored){}pet=null;super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}