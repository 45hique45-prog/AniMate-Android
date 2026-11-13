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
    WindowManager wm; View bubble; WindowManager.LayoutParams lp; Handler handler=new Handler();
    String mood="😺"; long lastCheck=0; int x=40,y=180;
    Runnable monitor=new Runnable(){public void run(){
        try {
            UsageStatsManager us=(UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
            UsageEvents ev=us.queryEvents(System.currentTimeMillis()-5000,System.currentTimeMillis());
            UsageEvents.Event e=new UsageEvents.Event(); String foreground="";
            while(ev.hasNextEvent()){ev.getNextEvent(e); if(e.getEventType()==UsageEvents.Event.MOVE_TO_FOREGROUND) foreground=e.getPackageName();}
            Set<String> chosen=getSharedPreferences(MainActivity.PREF,0).getStringSet(MainActivity.APPS,new HashSet<>());
            boolean show=chosen.contains(foreground);
            if(bubble!=null) bubble.setVisibility(show?View.VISIBLE:View.GONE);
            if(System.currentTimeMillis()-lastCheck>30000){lastCheck=System.currentTimeMillis(); mood="😺";}
        } catch(Exception ignored){}
        handler.postDelayed(this,2500);
    }};
    @Override public void onCreate(){
        super.onCreate(); wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        TextView t=new TextView(this); t.setText("🐱"); t.setTextSize(30); t.setPadding(12,8,12,8); t.setBackgroundColor(0xDDE8DDFB); bubble=t;
        int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
        lp=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.TOP|Gravity.LEFT; lp.x=x;lp.y=y;
        try{wm.addView(bubble,lp);}catch(Exception ex){stopSelf();return;}
        bubble.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int ox,oy;long down;
            public boolean onTouch(View v,android.view.MotionEvent e){
                if(e.getAction()==0){sx=e.getRawX();sy=e.getRawY();ox=lp.x;oy=lp.y;down=System.currentTimeMillis();return true;}
                if(e.getAction()==2){lp.x=ox+(int)(e.getRawX()-sx);lp.y=oy+(int)(e.getRawY()-sy);wm.updateViewLayout(bubble,lp);return true;}
                if(e.getAction()==1){if(System.currentTimeMillis()-down<220){((TextView)bubble).setText("💜");handler.postDelayed(()->{if(bubble!=null)((TextView)bubble).setText("🐱");},900);}return true;}return true;
            }});
        handler.post(monitor);
    }
    @Override public int onStartCommand(Intent i,int f,int id){return START_STICKY;}
    @Override public void onDestroy(){handler.removeCallbacks(monitor);if(bubble!=null)wm.removeView(bubble);super.onDestroy();}
    @Override public android.os.IBinder onBind(Intent i){return null;}
}
