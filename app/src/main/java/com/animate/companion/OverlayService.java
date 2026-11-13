package com.animate.companion;

import android.app.*;
import android.app.usage.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class OverlayService extends Service {
    WindowManager wm;
    TextView bubble;
    WindowManager.LayoutParams lp;
    Handler handler=new Handler();
    String mood="🐱";
    boolean happy=false;
    float pulse=0f;
    Runnable monitor=new Runnable(){public void run(){
        try {
            UsageStatsManager us=(UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
            long now=System.currentTimeMillis();
            UsageEvents ev=us.queryEvents(now-10000,now);
            UsageEvents.Event e=new UsageEvents.Event();
            String foreground="";
            while(ev.hasNextEvent()){
                ev.getNextEvent(e);
                if(e.getEventType()==UsageEvents.Event.MOVE_TO_FOREGROUND) foreground=e.getPackageName();
            }
            Set<String> chosen=getSharedPreferences(MainActivity.PREF,0).getStringSet(MainActivity.APPS,new HashSet<>());
            if(bubble!=null) bubble.setVisibility(chosen.contains(foreground)?View.VISIBLE:View.GONE);
            if(bubble!=null&&!happy){
                pulse+=0.06f;
                bubble.setAlpha(0.86f+(float)Math.sin(pulse)*0.14f);
            }
        } catch(Exception ignored){}
        handler.postDelayed(this,2500);
    }};
    @Override public void onCreate(){
        super.onCreate();
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        android.content.SharedPreferences prefs=getSharedPreferences(MainActivity.PREF,0);
        int x=prefs.getInt("overlay_x",40), y=prefs.getInt("overlay_y",180);
        bubble=new TextView(this);
        bubble.setText(mood); bubble.setTextSize(30); bubble.setPadding(12,8,12,8);
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
                        handler.postDelayed(()->{if(bubble!=null){happy=false;bubble.setText("🐱");}},1000);
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
