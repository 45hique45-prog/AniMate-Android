package com.animate.companion;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.app.usage.UsageStatsManager;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    static final String PREF="animate", APPS="apps";
    ArrayList<String> packages=new ArrayList<>(), labels=new ArrayList<>();
    LinearLayout root; TextView status, selectedLine, usageLine, overlayLine;
    final int bg=Color.rgb(20,17,29), panel=Color.rgb(39,33,53), accent=Color.rgb(214,174,255), soft=Color.rgb(190,181,205);
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);build();}
    GradientDrawable rounded(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
    GradientDrawable gradient(int top,int bottom,int radius){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{top,bottom});d.setCornerRadius(radius);d.setStroke(1,Color.rgb(174,111,255));return d;}
    TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    void build(){
        ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(20,24,20,28);root.setBackgroundColor(bg);scroll.addView(root);setContentView(scroll);
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setOrientation(LinearLayout.HORIZONTAL);root.addView(header);
        TextView brand=label("🐱 AniMate ✦",30,accent);brand.setTypeface(null,Typeface.BOLD);header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView version=label("♛  v1.3.0",12,Color.WHITE);version.setPadding(12,7,12,7);version.setBackground(rounded(Color.rgb(66,42,91),28));header.addView(version);
        TextView subtitle=label("Your tiny anime companion",15,soft);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=4;root.addView(subtitle,sp);
        LinearLayout hero=card();hero.setGravity(Gravity.CENTER);hero.setBackground(gradient(Color.rgb(74,42,112),Color.rgb(35,25,55),25));LinearLayout.LayoutParams hp=margin(0,20,0,0);root.addView(hero,hp);
        TextView mascot=label("૮ ˶ᵔ ᵕ ᵔ˶ ა",43,Color.WHITE);mascot.setGravity(Gravity.CENTER);hero.addView(mascot);
        TextView greeting=label("Hey there!  ♡",20,Color.WHITE);greeting.setTypeface(null,Typeface.BOLD);greeting.setGravity(Gravity.CENTER);hero.addView(greeting,margin(0,5,0,0));
        TextView welcome=label("A little friend for healthier screen time",13,soft);welcome.setGravity(Gravity.CENTER);LinearLayout.LayoutParams wp=margin(0,6,0,0);hero.addView(welcome,wp);
        LinearLayout stats=card();stats.setBackground(rounded(Color.rgb(43,34,61),22));root.addView(stats,margin(0,14,0,0));
        TextView statsTitle=label("YOUR ANI-MATE STATUS",11,accent);statsTitle.setTypeface(null,Typeface.BOLD);stats.addView(statsTitle,margin(0,0,0,10));
        selectedLine=label("",14,Color.WHITE);usageLine=label("",14,soft);overlayLine=label("",14,soft);stats.addView(selectedLine);stats.addView(usageLine,margin(0,7,0,0));stats.addView(overlayLine,margin(0,7,0,0));
        button("＋   CHOOSE UP TO 5 APPS",()->chooseApps());
        button("◷   SET DAILY APP LIMITS",()->manageLimits());
        button("▤   ALLOW USAGE ACCESS",()->startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        button("✧   ALLOW FLOATING COMPANION",()->{if(Build.VERSION.SDK_INT>=23)startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));});
        button("▶   START ANIMATE",()->startCompanion());
        button("■   STOP ANIMATE",()->{stopService(new Intent(this,CompanionService.class));Toast.makeText(this,"Companion stopped",Toast.LENGTH_SHORT).show();});
        TextView states=label("✿  IDLE     ♥  HAPPY     ☾  SLEEPY",12,accent);states.setGravity(Gravity.CENTER);root.addView(states,margin(0,20,0,0));
        TextView foot=label("A LITTLE COMPANION FOR A BETTER BALANCE",10,soft);foot.setGravity(Gravity.CENTER);root.addView(foot,margin(0,8,0,0));refresh();
    }
    LinearLayout card(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(18,18,18,18);l.setBackground(rounded(panel,22));return l;}
    LinearLayout.LayoutParams margin(int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(l,t,r,b);return p;}
    void button(String title,Runnable action){TextView b=label(title,14,Color.WHITE);b.setTypeface(null,Typeface.BOLD);b.setGravity(Gravity.CENTER);b.setPadding(14,18,14,18);b.setBackground(gradient(Color.rgb(133,68,220),Color.rgb(78,43,132),18));root.addView(b,margin(0,12,0,0));b.setOnClickListener(v->action.run());}
    Set<String> selected(){return new HashSet<>(getSharedPreferences(PREF,0).getStringSet(APPS,new HashSet<>()));}
    void chooseApps(){
        packages.clear();labels.clear();Intent q=new Intent(Intent.ACTION_MAIN);q.addCategory(Intent.CATEGORY_LAUNCHER);
        for(android.content.pm.ResolveInfo ri:getPackageManager().queryIntentActivities(q,0)){String p=ri.activityInfo.packageName;if(!p.equals(getPackageName())){packages.add(p);labels.add(ri.loadLabel(getPackageManager()).toString());}}
        boolean[] checked=new boolean[packages.size()];Set<String> old=selected();for(int i=0;i<checked.length;i++)checked[i]=old.contains(packages.get(i));
        new AlertDialog.Builder(this).setTitle("Choose up to 5 apps").setMultiChoiceItems(labels.toArray(new String[0]),checked,(d,w,c)->{int n=0;for(boolean x:checked)if(x)n++;if(n>5){checked[w]=false;((AlertDialog)d).getListView().setItemChecked(w,false);Toast.makeText(this,"Choose maximum 5 apps",Toast.LENGTH_SHORT).show();}else checked[w]=c;}).setPositiveButton("SAVE",(d,w)->{HashSet<String>s=new HashSet<>();for(int i=0;i<checked.length;i++)if(checked[i])s.add(packages.get(i));getSharedPreferences(PREF,0).edit().putStringSet(APPS,s).apply();refresh();}).setNegativeButton("CANCEL",null).show();
    }
    void manageLimits(){
        ArrayList<String> pkgs=new ArrayList<>(selected());if(pkgs.isEmpty()){Toast.makeText(this,"Choose apps first",Toast.LENGTH_SHORT).show();return;}
        ArrayList<String> names=new ArrayList<>();for(String p:pkgs){try{names.add(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(p,0)).toString());}catch(Exception e){names.add(p);}}
        String[] rows=new String[pkgs.size()];for(int i=0;i<rows.length;i++)rows[i]=names.get(i)+"  ·  "+getSharedPreferences(PREF,0).getInt("limit_"+pkgs.get(i),60)+" min/day";
        new AlertDialog.Builder(this).setTitle("Daily app limits").setItems(rows,(d,w)->editLimit(pkgs.get(w),names.get(w))).setNegativeButton("DONE",null).show();
    }
    void editLimit(String pkg,String name){EditText input=new EditText(this);input.setInputType(2);input.setHint("Minutes per day (1–1440)");input.setText(""+getSharedPreferences(PREF,0).getInt("limit_"+pkg,60));new AlertDialog.Builder(this).setTitle(name).setMessage("Set your daily time budget").setView(input).setPositiveButton("SAVE",(d,w)->{try{int n=Integer.parseInt(input.getText().toString());if(n<1||n>1440)throw new Exception();getSharedPreferences(PREF,0).edit().putInt("limit_"+pkg,n).apply();Toast.makeText(this,"Daily limit saved",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Enter 1 to 1440 minutes",Toast.LENGTH_SHORT).show();}}).setNegativeButton("CANCEL",null).show();}
    boolean usageAllowed(){try{AppOpsManager a=(AppOpsManager)getSystemService(APP_OPS_SERVICE);return a.checkOpNoThrow("android:get_usage_stats",android.os.Process.myUid(),getPackageName())==AppOpsManager.MODE_ALLOWED;}catch(Exception e){return false;}}
    void startCompanion(){if(!usageAllowed()){startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));Toast.makeText(this,"Enable usage access, then tap Start again",Toast.LENGTH_LONG).show();return;}if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));Toast.makeText(this,"Allow floating companion, then tap Start again",Toast.LENGTH_LONG).show();return;}if(selected().isEmpty()){Toast.makeText(this,"Choose at least one app first",Toast.LENGTH_LONG).show();return;}startService(new Intent(this,CompanionService.class));Toast.makeText(this,"AniMate is running",Toast.LENGTH_SHORT).show();}
    void refresh(){if(selectedLine==null)return;selectedLine.setText("✦  Selected apps: "+selected().size()+" / 5");usageLine.setText("◷  Usage access: "+(usageAllowed()?"Enabled":"Not enabled"));overlayLine.setText("♡  Floating overlay: "+(Build.VERSION.SDK_INT<23||Settings.canDrawOverlays(this)?"Enabled":"Not enabled"));}
    @Override protected void onResume(){super.onResume();if(selectedLine!=null)refresh();}
}