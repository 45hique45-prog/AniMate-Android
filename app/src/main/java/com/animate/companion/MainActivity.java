package com.animate.companion;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.app.usage.UsageStatsManager;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    static final String PREF="animate", APPS="apps";
    ArrayList<String> packages=new ArrayList<>(), labels=new ArrayList<>();
    TextView status;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(24,32,24,24); root.setBackgroundColor(Color.rgb(249,246,255));
        TextView title=new TextView(this); title.setText("🐱 AniMate"); title.setTextSize(30); title.setTextColor(Color.rgb(91,63,130)); root.addView(title);
        TextView sub=new TextView(this); sub.setText("Your tiny anime companion"); sub.setTextSize(16); root.addView(sub);
        status=new TextView(this); status.setTextSize(15); status.setPadding(0,24,0,24); root.addView(status);
        Button pick=new Button(this); pick.setText("Choose up to 5 apps"); root.addView(pick); pick.setOnClickListener(v->chooseApps());
        Button limits=new Button(this); limits.setText("Set daily app limits"); root.addView(limits); limits.setOnClickListener(v->manageLimits());
        Button usage=new Button(this); usage.setText("Allow usage access"); root.addView(usage); usage.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        Button overlay=new Button(this); overlay.setText("Allow display over other apps"); root.addView(overlay); overlay.setOnClickListener(v->{ if(Build.VERSION.SDK_INT>=23) startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))); });
        Button start=new Button(this); start.setText("Start AniMate"); root.addView(start); start.setOnClickListener(v->startCompanion());
        Button stop=new Button(this); stop.setText("Stop AniMate"); root.addView(stop); stop.setOnClickListener(v->{stopService(new Intent(this,OverlayService.class));Toast.makeText(this,"AniMate stopped",Toast.LENGTH_SHORT).show();});
        setContentView(root); refresh();
    }
    void refresh() {
        Set<String> s=getSharedPreferences(PREF,0).getStringSet(APPS,new HashSet<>());
        boolean u=((UsageStatsManager)getSystemService(USAGE_STATS_SERVICE)).queryUsageStats(UsageStatsManager.INTERVAL_DAILY,System.currentTimeMillis()-86400000,System.currentTimeMillis()).size()>0;
        status.setText("Selected apps: "+s.size()+"/5\nUsage access: "+(u?"Ready":"Not enabled")+"\nOverlay: "+(Build.VERSION.SDK_INT<23||Settings.canDrawOverlays(this)?"Ready":"Not enabled"));
    }
    void chooseApps() {
        packages.clear(); labels.clear();
        Intent q=new Intent(Intent.ACTION_MAIN); q.addCategory(Intent.CATEGORY_LAUNCHER);
        for(android.content.pm.ResolveInfo ri:getPackageManager().queryIntentActivities(q,0)) {
            String p=ri.activityInfo.packageName; if(p.equals(getPackageName())) continue;
            packages.add(p); labels.add(ri.loadLabel(getPackageManager()).toString());
        }
        Set<String> saved=getSharedPreferences(PREF,0).getStringSet(APPS,new HashSet<>());
        boolean[] checked=new boolean[packages.size()];
        for(int i=0;i<packages.size();i++) checked[i]=saved.contains(packages.get(i));
        new AlertDialog.Builder(this).setTitle("Choose up to 5 apps").setMultiChoiceItems(labels.toArray(new String[0]),checked,(d,w,c)->{
            int count=0; for(boolean x:checked) if(x) count++;
            if(count>5){checked[w]=false; ((AlertDialog)d).getListView().setItemChecked(w,false); Toast.makeText(this,"Maximum 5 apps",Toast.LENGTH_SHORT).show();}
            else checked[w]=c;
        }).setPositiveButton("Save",(d,w)->{
            HashSet<String> chosen=new HashSet<>(); for(int i=0;i<checked.length;i++) if(checked[i]) chosen.add(packages.get(i));
            getSharedPreferences(PREF,0).edit().putStringSet(APPS,chosen).apply(); refresh();
        }).setNegativeButton("Cancel",null).show();
    }
    void manageLimits() {
        Set<String> selected=getSharedPreferences(PREF,0).getStringSet(APPS,new HashSet<>());
        if(selected.isEmpty()){Toast.makeText(this,"Choose apps first",Toast.LENGTH_SHORT).show();return;}
        ArrayList<String> pkgs=new ArrayList<>(selected);
        ArrayList<String> names=new ArrayList<>();
        for(String p:pkgs){try{names.add(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(p,0)).toString());}catch(Exception e){names.add(p);}}
        String[] items=new String[pkgs.size()];
        for(int i=0;i<items.length;i++) items[i]=names.get(i)+" — "+getSharedPreferences(PREF,0).getInt("limit_"+pkgs.get(i),60)+" min/day";
        new AlertDialog.Builder(this).setTitle("Daily limits").setItems(items,(dialog,which)->editLimit(pkgs.get(which),names.get(which))).setPositiveButton("Done",null).show();
    }
    void editLimit(String pkg,String name) {
        EditText input=new EditText(this); input.setInputType(2); input.setHint("Minutes per day (1–1440)");
        input.setText(String.valueOf(getSharedPreferences(PREF,0).getInt("limit_"+pkg,60)));
        new AlertDialog.Builder(this).setTitle(name+" daily limit").setView(input).setPositiveButton("Save",(d,w)->{
            try{int n=Integer.parseInt(input.getText().toString()); if(n<1||n>1440)throw new NumberFormatException();getSharedPreferences(PREF,0).edit().putInt("limit_"+pkg,n).apply();Toast.makeText(this,"Daily limit saved",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Enter 1 to 1440 minutes",Toast.LENGTH_LONG).show();}
        }).setNegativeButton("Cancel",null).show();
    }
    void startCompanion() {
        if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){Toast.makeText(this,"Please allow overlay first",Toast.LENGTH_LONG).show();return;}
        Set<String> apps=getSharedPreferences(PREF,0).getStringSet(APPS,new HashSet<>());
        if(apps.isEmpty()){Toast.makeText(this,"Choose at least one app first",Toast.LENGTH_LONG).show();return;}
        Intent service=new Intent(this,OverlayService.class);
        if(Build.VERSION.SDK_INT>=26) startForegroundService(service); else startService(service);
        Toast.makeText(this,"AniMate started",Toast.LENGTH_SHORT).show();
    }
    @Override protected void onResume(){super.onResume(); if(status!=null)refresh();}
}
