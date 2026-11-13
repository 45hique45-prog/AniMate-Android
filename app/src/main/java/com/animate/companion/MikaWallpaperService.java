package com.animate.companion;

import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import java.util.Random;

public class MikaWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine() { return new MikaEngine(); }
    private class MikaEngine extends Engine {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Runnable drawTask = new Runnable() { public void run() { drawFrame(); } };
        private boolean visible;
        private long started = System.currentTimeMillis();
        private void fill(Canvas c, int color) { p.setShader(null); p.setColor(color); p.setStyle(Paint.Style.FILL); }
        private void drawFrame() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    int w=c.getWidth(), h=c.getHeight();
                    p.setShader(new LinearGradient(0,0,w,h,0xff171323,0xff493064,Shader.TileMode.CLAMP));
                    c.drawRect(0,0,w,h,p); p.setShader(null);
                    float t=(System.currentTimeMillis()-started)/1000f;
                    android.content.SharedPreferences prefs=getSharedPreferences(MainActivity.PREF,0);
                    int hair=prefs.getInt("mika_hair",0xff35264f);
                    int outfit=prefs.getInt("mika_outfit",0xff7550a8);
                    int mood=prefs.getInt("mika_mood",MascotView.IDLE);
                    float cx=w*.5f, cy=h*.57f+(float)Math.sin(t*1.4f)*h*.012f, s=Math.min(w,h)*.00105f;
                    fill(c,0x223f245f); c.drawOval(cx-100*s,cy+155*s,cx+100*s,cy+178*s,p);
                    fill(c,hair); c.drawOval(cx-93*s,cy-105*s,cx+93*s,cy+105*s,p);
                    fill(c,0xffffe0d7); c.drawOval(cx-70*s,cy-85*s,cx+70*s,cy+72*s,p);
                    fill(c,0xff35264f);
                    c.drawRoundRect(new RectF(cx-75*s,cy-24*s,cx-57*s,cy+100*s),12*s,12*s,p);
                    c.drawRoundRect(new RectF(cx+57*s,cy-24*s,cx+75*s,cy+100*s),12*s,12*s,p);
                    // Cat ears
                    PathHelper.triangle(c,p,cx-62*s,cy-70*s,cx-80*s,cy-145*s,cx-22*s,cy-93*s,hair);
                    PathHelper.triangle(c,p,cx+62*s,cy-70*s,cx+80*s,cy-145*s,cx+22*s,cy-93*s,hair);
                    // Fringe and big eyes
                    fill(c,hair); c.drawOval(cx-74*s,cy-118*s,cx+74*s,cy-40*s,p);
                    if(mood==MascotView.SLEEPY || mood==MascotView.HAPPY || mood==MascotView.SAD){
                        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4*s);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(0xff4a2d58);
                        if(mood==MascotView.SLEEPY){c.drawArc(cx-44*s,cy-12*s,cx-22*s,cy+8*s,190,160,false,p);c.drawArc(cx+22*s,cy-12*s,cx+44*s,cy+8*s,190,160,false,p);}
                        else if(mood==MascotView.HAPPY){c.drawArc(cx-45*s,cy-20*s,cx-20*s,cy+8*s,195,145,false,p);c.drawArc(cx+20*s,cy-20*s,cx+45*s,cy+8*s,195,145,false,p);}
                        else {c.drawArc(cx-44*s,cy-10*s,cx-22*s,cy+12*s,200,140,false,p);c.drawArc(cx+22*s,cy-10*s,cx+44*s,cy+12*s,200,140,false,p);}
                        p.setStyle(Paint.Style.FILL);
                    } else {
                        fill(c,0xff4a2d58); c.drawOval(cx-43*s,cy-24*s,cx-24*s,cy+8*s,p); c.drawOval(cx+24*s,cy-24*s,cx+43*s,cy+8*s,p);
                        fill(c,Color.WHITE); c.drawCircle(cx-35*s,cy-15*s,5*s,p); c.drawCircle(cx+32*s,cy-15*s,5*s,p);
                    }
                    fill(c,0xfff5a5b5); c.drawOval(cx-58*s,cy+17*s,cx-31*s,cy+34*s,p); c.drawOval(cx+31*s,cy+17*s,cx+58*s,cy+34*s,p);
                    fill(c,outfit); c.drawRoundRect(new RectF(cx-82*s,cy+70*s,cx+82*s,cy+200*s),34*s,34*s,p);
                    fill(c,0xffd6a4ff); c.drawCircle(cx+73*s,cy-82*s,17*s,p);
                    fill(c,0xffffd9f6); c.drawCircle(cx+73*s,cy-82*s,6*s,p);
                    fill(c,0xff35264f); c.drawOval(cx-74*s,cy-118*s,cx+74*s,cy-40*s,p);
                    p.setColor(0xffead9ff); p.setTextSize(20*s); c.drawText("Mika ♡",cx-38*s,cy+245*s,p);
                    // Floating stars
                    fill(c,0xffffd88a); c.drawCircle(cx-135*s,cy-145*s,5*s,p); c.drawCircle(cx+140*s,cy+10*s,4*s,p);
                }
            } catch (Exception ignored) { } finally { if(c!=null) holder.unlockCanvasAndPost(c); }
            handler.removeCallbacks(drawTask);
            if(visible) handler.postDelayed(drawTask, 1000L);
        }
        @Override public void onVisibilityChanged(boolean v) { visible=v; handler.removeCallbacks(drawTask); if(v) drawFrame(); }
        @Override public void onSurfaceChanged(SurfaceHolder h,int f,int w,int he) { super.onSurfaceChanged(h,f,w,he); drawFrame(); }
        @Override public void onSurfaceDestroyed(SurfaceHolder h) { visible=false; handler.removeCallbacks(drawTask); super.onSurfaceDestroyed(h); }
        @Override public void onDestroy() { visible=false; handler.removeCallbacks(drawTask); super.onDestroy(); }
    }
    static class PathHelper {
        static void triangle(Canvas c, Paint p, float x1,float y1,float x2,float y2,float x3,float y3,int color) {
            p.setShader(null); p.setColor(color); android.graphics.Path path=new android.graphics.Path();
            path.moveTo(x1,y1); path.lineTo(x2,y2); path.lineTo(x3,y3); path.close(); c.drawPath(path,p);
        }
    }
}