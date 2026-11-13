package com.animate.companion;

import android.app.*;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.app.AppOpsManager;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    static final String PREF="animate", APPS="apps";
    ArrayList<String> packages=new ArrayList<>(), labels=new ArrayList<>();
    LinearLayout root, usageRows, selectedRows;
    TextView selectedLine, usageLine, overlayLine, heroCaption;
    MascotView mascot;
    CountDownTimer breakTimer;
    TextView breakClock;
    final int bg=Color.rgb(18,15,27), panel=Color.rgb(39,31,54), panel2=Color.rgb(51,39,70);
    final int accent=Color.rgb(218,178,255), soft=Color.rgb(190,180,207), white=Color.WHITE;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);build();}
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    GradientDrawable gradient(int a,int b,int radius){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(dp(radius));d.setStroke(dp(1),0xff75519a);return d;}
    TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    LinearLayout.LayoutParams spaced(int top){LinearLayout.LayoutParams p=lp(-1,-2);p.topMargin=dp(top);return p;}
    void build(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(bg);
        root=column();root.setPadding(dp(20),dp(18),dp(20),dp(30));scroll.addView(root);setContentView(scroll);
        LinearLayout header=row();root.addView(header);
        TextView logo=text("🐱",29,white);header.addView(logo,lp(42,48));
        LinearLayout names=column();header.addView(names,new LinearLayout.LayoutParams(0,-2,1));
        TextView brand=text("AniMate",27,accent);brand.setTypeface(Typeface.DEFAULT,Typeface.BOLD);names.addView(brand);
        names.addView(text("YOUR TINY ANIME COMPANION",9,soft));
        TextView version=text("v1.9.2",11,accent);version.setPadding(dp(11),dp(7),dp(11),dp(7));version.setBackground(shape(panel2,30));header.addView(version);
        TextView hello=text("A little companion for a better balance.",13,soft);root.addView(hello,spaced(5));

        LinearLayout hero=column();hero.setGravity(Gravity.CENTER);hero.setPadding(dp(16),dp(16),dp(16),dp(18));hero.setBackground(gradient(0xff59377e,0xff261c39,26));root.addView(hero,spaced(20));
        TextView tag=text("✦  YOUR COMPANION  ✦",10,0xffe7d4ff);hero.addView(tag);
        mascot=new MascotView(this);LinearLayout.LayoutParams mp=lp(142,148);mp.topMargin=dp(5);hero.addView(mascot,mp);
        TextView name=text("Mika",21,white);name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);hero.addView(name);
        heroCaption=text("I'm here with you ♡",12,0xffe3d6f0);hero.addView(heroCaption,spaced(3));
        HorizontalScrollView moodScroll=new HorizontalScrollView(this);moodScroll.setHorizontalScrollBarEnabled(false);LinearLayout states=row();states.setGravity(Gravity.CENTER_VERTICAL);states.setPadding(0,dp(12),0,0);moodScroll.addView(states);hero.addView(moodScroll,lp(-1,-2));
        stateChip(states,"✿ Idle",MascotView.IDLE);stateChip(states,"♥ Happy",MascotView.HAPPY);stateChip(states,"☾ Sleepy",MascotView.SLEEPY);stateChip(states,"☹ Sad",MascotView.SAD);stateChip(states,"怒 Angry",MascotView.ANGRY);stateChip(states,"✧ Wow",MascotView.SURPRISED);stateChip(states,"♡ Shy",MascotView.SHY);stateChip(states,"… Think",MascotView.THINKING);

        TextView section=text("YOUR SPACE",12,accent);section.setTypeface(Typeface.DEFAULT,Typeface.BOLD);root.addView(section,spaced(22));
        LinearLayout status=column();status.setPadding(dp(15),dp(14),dp(15),dp(14));status.setBackground(shape(panel,20));root.addView(status,spaced(9));
        selectedLine=text("",13,white);usageLine=text("",12,soft);overlayLine=text("",12,soft);
        status.addView(selectedLine);status.addView(usageLine,spaced(8));status.addView(overlayLine,spaced(7));

        LinearLayout actions=row();root.addView(actions,spaced(14));
        actionTile(actions,"▦","Choose apps","Up to five",()->chooseApps());
        actionTile(actions,"◷","Daily limits","Set a budget",()->manageLimits());
        LinearLayout actions2=row();root.addView(actions2,spaced(10));
        actionTile(actions2,"✧","Character","Customize Mika",()->characterSettings());
        actionTile(actions2,"⚙","Permissions","Usage & overlay",()->permissions());

        TextView usageHead=text("TODAY'S SCREEN TIME",12,accent);usageHead.setTypeface(Typeface.DEFAULT,Typeface.BOLD);root.addView(usageHead,spaced(22));
        TextView hint=text("Tap an app to change its daily limit.",11,soft);root.addView(hint,spaced(3));
        LinearLayout usageCard=column();usageCard.setPadding(dp(15),dp(8),dp(15),dp(8));usageCard.setBackground(shape(panel,20));root.addView(usageCard,spaced(10));
        usageRows=column();usageCard.addView(usageRows);

        LinearLayout start=row();start.setGravity(Gravity.CENTER);root.addView(start,spaced(18));
        TextView startButton=text("▶   START COMPANION",13,white);startButton.setTypeface(Typeface.DEFAULT,Typeface.BOLD);startButton.setGravity(Gravity.CENTER);startButton.setPadding(dp(12),dp(16),dp(12),dp(16));startButton.setBackground(gradient(0xffa35de4,0xff7040ad,17));start.addView(startButton,new LinearLayout.LayoutParams(0,dp(52),1));startButton.setOnClickListener(v->startCompanion());
        TextView stop=text("■",18,0xffffc6d5);stop.setGravity(Gravity.CENTER);stop.setBackground(shape(0xff482b45,16));LinearLayout.LayoutParams stopLp=lp(54,52);stopLp.leftMargin=dp(9);start.addView(stop,stopLp);stop.setOnClickListener(v->stopCompanion());
        LinearLayout extras=row();root.addView(extras,spaced(10));
        actionTile(extras,"◴","30–20 Break","Start a 20-minute rest",()->startBreakTimer());
        actionTile(extras,"▥","Statistics","Usage overview",()->showStatistics());
        LinearLayout extras2=row();root.addView(extras2,spaced(10));
        actionTile(extras2,"✦","Live Wallpaper","Set Mika as wallpaper",()->openLiveWallpaper());
        actionTile(extras2,"▣","Home Widget","Add from home screen",()->new AlertDialog.Builder(this).setTitle("Add AniMate widget").setMessage("Long-press an empty area on your home screen, choose Widgets, then find AniMate and place the Mika widget.").setPositiveButton("GOT IT",null).show());
        TextView footer=text("MADE FOR YOUR LITTLE BREAKS  ♡",9,soft);footer.setGravity(Gravity.CENTER);root.addView(footer,spaced(18));
        refresh();
    }
    void stateChip(LinearLayout parent,String title,int state){TextView chip=text(title,10,white);chip.setGravity(Gravity.CENTER);chip.setPadding(dp(12),dp(8),dp(12),dp(8));chip.setBackground(shape(0x554d3a65,30));LinearLayout.LayoutParams p=lp(-2,-2);p.setMargins(dp(4),0,dp(4),0);parent.addView(chip,p);chip.setOnClickListener(v->{mascot.setState(state);String[] captions={"I'm here with you ♡","You made my day! ♡","Let's take a little break…","Aww, what's wrong?","Hmph! That's not fair!","Oh! Really?","Hehe… you're making me blush","Hmm… let me think"};heroCaption.setText(captions[Math.max(0,Math.min(state,7))]);getSharedPreferences(PREF,0).edit().putInt("mika_mood",state).apply();updateWidget();});}
    void actionTile(LinearLayout parent,String icon,String title,String sub,Runnable click){LinearLayout tile=column();tile.setPadding(dp(13),dp(13),dp(10),dp(13));tile.setBackground(shape(panel,18));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(4),0,dp(4),0);parent.addView(tile,p);TextView i=text(icon,23,accent);tile.addView(i);TextView t=text(title,13,white);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);tile.addView(t,spaced(7));tile.addView(text(sub,10,soft),spaced(2));tile.setOnClickListener(v->click.run());}
    Set<String> selected(){return new HashSet<>(getSharedPreferences(PREF,0).getStringSet(APPS,new HashSet<>()));}
    void chooseApps(){
        packages.clear();labels.clear();Intent q=new Intent(Intent.ACTION_MAIN);q.addCategory(Intent.CATEGORY_LAUNCHER);
        for(android.content.pm.ResolveInfo ri:getPackageManager().queryIntentActivities(q,0)){String p=ri.activityInfo.packageName;if(!p.equals(getPackageName())){packages.add(p);labels.add(ri.loadLabel(getPackageManager()).toString());}}
        boolean[] checked=new boolean[packages.size()];Set<String> old=selected();for(int i=0;i<checked.length;i++)checked[i]=old.contains(packages.get(i));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Choose up to 5 apps").setMultiChoiceItems(labels.toArray(new String[0]),checked,(d,w,c)->{int n=0;for(boolean x:checked)if(x)n++;if(c&&n>5){checked[w]=false;((AlertDialog)d).getListView().setItemChecked(w,false);Toast.makeText(this,"Maximum 5 apps",Toast.LENGTH_SHORT).show();}else checked[w]=c;}).setPositiveButton("SAVE",(d,w)->{HashSet<String>s=new HashSet<>();for(int i=0;i<checked.length;i++)if(checked[i])s.add(packages.get(i));getSharedPreferences(PREF,0).edit().putStringSet(APPS,s).apply();refresh();}).setNegativeButton("CANCEL",null).create();dialog.show();
    }
    void manageLimits(){ArrayList<String> pkgs=new ArrayList<>(selected());if(pkgs.isEmpty()){toast("Choose apps first");return;}String[] rows=new String[pkgs.size()];ArrayList<String> names=new ArrayList<>();for(int i=0;i<pkgs.size();i++){String n=appName(pkgs.get(i));names.add(n);rows[i]=n+"  ·  "+getSharedPreferences(PREF,0).getInt("limit_"+pkgs.get(i),60)+" min/day";}new AlertDialog.Builder(this).setTitle("Daily app limits").setItems(rows,(d,w)->editLimit(pkgs.get(w),names.get(w))).setNegativeButton("DONE",null).show();}
    void editLimit(String pkg,String name){EditText input=new EditText(this);input.setInputType(2);input.setHint("Minutes per day (1–1440)");input.setText(""+getSharedPreferences(PREF,0).getInt("limit_"+pkg,60));new AlertDialog.Builder(this).setTitle(name).setMessage("Set your daily time budget").setView(input).setPositiveButton("SAVE",(d,w)->{try{int n=Integer.parseInt(input.getText().toString());if(n<1||n>1440)throw new Exception();getSharedPreferences(PREF,0).edit().putInt("limit_"+pkg,n).apply();toast("Daily limit saved");refreshUsage();}catch(Exception e){toast("Enter 1 to 1440 minutes");}}).setNegativeButton("CANCEL",null).show();}
    String appName(String pkg){try{return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)).toString();}catch(Exception e){return pkg;}}
    void characterSettings(){
        String[] choices={"Idle","Happy","Sleepy","Sad","Angry","Surprised","Shy","Thinking","Hair: Lavender","Hair: Midnight","Hair: Rose","Outfit: Violet","Outfit: Pink","Outfit: Blue"};
        int[] moods={MascotView.IDLE,MascotView.HAPPY,MascotView.SLEEPY,MascotView.SAD,MascotView.ANGRY,MascotView.SURPRISED,MascotView.SHY,MascotView.THINKING};
        new AlertDialog.Builder(this).setTitle("Mika · Choose her mood").setItems(choices,(d,w)->{
            if(w<8){mascot.setState(moods[w]);String[] captions={"I'm here with you ♡","You made my day! ♡","Let's take a little break…","Aww, what's wrong?","Hmph! That's not fair!","Oh! Really?","Hehe… you're making me blush","Hmm… let me think"};heroCaption.setText(captions[w]);getSharedPreferences(PREF,0).edit().putInt("mika_mood",w).apply();updateWidget();}
            else if(w<11){int[] colors={0xff35264f,0xff24243d,0xff70405f};int color=colors[w-8];mascot.setHairColor(color);getSharedPreferences(PREF,0).edit().putInt("mika_hair",color).apply();}
            else {int[] colors={0xff7550a8,0xffb84f87,0xff426caa};int color=colors[w-11];mascot.setOutfitColor(color);getSharedPreferences(PREF,0).edit().putInt("mika_outfit",color).apply();}
            toast("Mika updated ♡");
        }).setNegativeButton("CLOSE",null).show();
    }
    void startBreakTimer(){
        if(breakTimer!=null){breakTimer.cancel();breakTimer=null;}
        LinearLayout panel=column();panel.setPadding(dp(18),dp(12),dp(18),dp(8));
        panel.addView(text("30 minutes focus · 20 minutes rest",13,soft));
        breakClock=text("20:00",38,accent);breakClock.setGravity(Gravity.CENTER);breakClock.setPadding(0,dp(15),0,dp(15));panel.addView(breakClock);
        TextView note=text("Put your phone aside and take a little break. Mika will keep time for you.",12,white);note.setGravity(Gravity.CENTER);panel.addView(note);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Your 20-minute break").setView(panel).setNegativeButton("CLOSE",null).setPositiveButton("START BREAK",null).create();
        dlg.setOnShowListener(v->dlg.getButton(-1).setOnClickListener(b->{
            dlg.getButton(-1).setEnabled(false);
            breakTimer=new CountDownTimer(20*60*1000L,1000L){
                public void onTick(long ms){if(breakClock!=null)breakClock.setText(String.format(Locale.US,"%02d:%02d",ms/60000,(ms/1000)%60));}
                public void onFinish(){if(breakClock!=null)breakClock.setText("Break complete ♡");toast("Break complete! Welcome back.");breakTimer=null;}
            }.start();
        }));dlg.show();
    }
    void openLiveWallpaper(){
        try{
            Intent intent=new Intent(android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new android.content.ComponentName(this,MikaWallpaperService.class));
            startActivity(intent);
        }catch(Exception e){
            try{startActivity(new Intent(android.app.WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));}
            catch(Exception ignored){toast("Live wallpaper picker is unavailable on this device");}
        }
    }
    void showStatistics(){
        if(!usageAllowed()){toast("Enable Usage Access to view statistics");startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));return;}
        Calendar end=Calendar.getInstance(),start=(Calendar)end.clone();start.add(Calendar.DAY_OF_YEAR,-7);
        UsageStatsManager mgr=(UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);Map<String,UsageStats> map=mgr.queryAndAggregateUsageStats(start.getTimeInMillis(),end.getTimeInMillis());
        long total=0;ArrayList<String> lines=new ArrayList<>();
        for(String pkg:selected()){UsageStats stat=map.get(pkg);long ms=stat==null?0:stat.getTotalTimeInForeground();total+=ms;lines.add(appName(pkg)+"  ·  "+(ms/3600000)+"h "+((ms/60000)%60)+"m");}
        StringBuilder body=new StringBuilder("Selected apps · last 7 days\nTotal tracked: "+(total/3600000)+"h "+((total/60000)%60)+"m\n\n");
        if(lines.isEmpty())body.append("Choose apps to start tracking.");else for(String line:lines)body.append("• ").append(line).append("\n");
        new AlertDialog.Builder(this).setTitle("Your usage statistics").setMessage(body.toString()).setPositiveButton("DONE",null).show();
    }
    void permissions(){String[] choices={"Usage access","Display over other apps"};new AlertDialog.Builder(this).setTitle("AniMate permissions").setItems(choices,(d,w)->{if(w==0)startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));else if(Build.VERSION.SDK_INT>=23)startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));}).setNegativeButton("CLOSE",null).show();}
    boolean usageAllowed(){try{AppOpsManager a=(AppOpsManager)getSystemService(APP_OPS_SERVICE);return a.checkOpNoThrow("android:get_usage_stats",android.os.Process.myUid(),getPackageName())==AppOpsManager.MODE_ALLOWED;}catch(Exception e){return false;}}
    void startCompanion(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},33);toast("Allow notifications, then tap Start again");return;}if(!usageAllowed()){startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));toast("Enable usage access, then tap Start again");return;}if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));toast("Allow display over other apps, then tap Start again");return;}if(selected().isEmpty()){toast("Choose at least one app first");return;}Intent service=new Intent(this,OverlayService.class);if(Build.VERSION.SDK_INT>=26)startForegroundService(service);else startService(service);toast("AniMate is running");}
    void stopCompanion(){stopService(new Intent(this,OverlayService.class));stopService(new Intent(this,CompanionService.class));toast("Companion stopped");}
    void updateWidget(){android.appwidget.AppWidgetManager manager=android.appwidget.AppWidgetManager.getInstance(this);int[] ids=manager.getAppWidgetIds(new ComponentName(this,AniMateWidget.class));if(ids.length>0){Intent update=new Intent(this,AniMateWidget.class);update.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);update.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);sendBroadcast(update);}}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    void refresh(){if(selectedLine==null)return;selectedLine.setText("✦  Selected apps     "+selected().size()+" / 5");usageLine.setText("◷  Usage access     "+(usageAllowed()?"Enabled":"Not enabled"));overlayLine.setText("♡  Floating companion     "+(Build.VERSION.SDK_INT<23||Settings.canDrawOverlays(this)?"Enabled":"Not enabled"));refreshUsage();}
    void refreshUsage(){
        if(usageRows==null)return;usageRows.removeAllViews();Set<String> chosen=selected();
        if(chosen.isEmpty()){TextView empty=text("No apps selected yet. Choose your first app above.",12,soft);empty.setPadding(0,dp(12),0,dp(12));usageRows.addView(empty);return;}
        Calendar cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY,0);cal.set(Calendar.MINUTE,0);cal.set(Calendar.SECOND,0);cal.set(Calendar.MILLISECOND,0);
        Map<String,UsageStats> totals=Collections.emptyMap();if(usageAllowed()){UsageStatsManager m=(UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);Map<String,UsageStats> r=m.queryAndAggregateUsageStats(cal.getTimeInMillis(),System.currentTimeMillis());if(r!=null)totals=r;}
        ArrayList<String> sorted=new ArrayList<>(chosen);Collections.sort(sorted);
        for(String pkg:sorted){String name=appName(pkg);UsageStats stat=totals.get(pkg);int used=(int)((stat==null?0:stat.getTotalTimeInForeground())/60000L);int limit=getSharedPreferences(PREF,0).getInt("limit_"+pkg,60);
            LinearLayout item=column();item.setPadding(0,dp(10),0,dp(10));usageRows.addView(item);LinearLayout top=row();item.addView(top);TextView app=text(name,13,white);top.addView(app,new LinearLayout.LayoutParams(0,-2,1));top.addView(text(used+" / "+limit+" min",11,soft));ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(limit);bar.setProgress(Math.min(used,limit));bar.setProgressTintList(android.content.res.ColorStateList.valueOf(used>=limit?0xffff91b0:0xffc69af2));bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff594b68));item.addView(bar,lp(-1,6));item.setOnClickListener(v->editLimit(pkg,name));
        }
    }
    @Override protected void onResume(){super.onResume();if(selectedLine!=null)refresh();}
    @Override protected void onDestroy(){if(breakTimer!=null)breakTimer.cancel();super.onDestroy();}
}