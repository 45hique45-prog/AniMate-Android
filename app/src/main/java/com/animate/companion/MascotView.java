package com.animate.companion;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class MascotView extends View {
    public static final int IDLE=0, HAPPY=1, SLEEPY=2, SAD=3, ANGRY=4, SURPRISED=5, SHY=6, THINKING=7;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private int state=IDLE;
    private int hair=0xff35264f, outfit=0xff7550a8;
    private float breathe=1f, blink=0f;
    private ValueAnimator animator;
    public MascotView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);if(c!=null){hair=c.getSharedPreferences(MainActivity.PREF,0).getInt("mika_hair",hair);outfit=c.getSharedPreferences(MainActivity.PREF,0).getInt("mika_outfit",outfit);}}
    public void setState(int s){state=s;invalidate();}
    public int getState(){return state;}
    public void setHairColor(int color){hair=color;invalidate();}
    public void setOutfitColor(int color){outfit=color;invalidate();}
    private void fill(Canvas c,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);p.setStrokeWidth(1);}
    private void stroke(int color,float width){p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);}
    private void startBreathing(){
        animator=ValueAnimator.ofFloat(0.985f,1.015f);animator.setDuration(1450);animator.setRepeatMode(ValueAnimator.REVERSE);animator.setRepeatCount(ValueAnimator.INFINITE);animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(a->{breathe=(Float)a.getAnimatedValue();invalidate();});animator.start();
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        c.save();float scale=Math.min(w/132f,h/150f);c.translate(w/2f,h/2f);c.scale(scale*breathe,scale*breathe);
        // soft ground shadow and tiny legs
        fill(c,0x443d285d);c.drawOval(-43,59,43,69,p);
        fill(c,0xffffd9d2);c.drawRoundRect(-20,43,-5,64,7,7,p);c.drawRoundRect(5,43,20,64,7,7,p);
        fill(c,0xff352747);c.drawRoundRect(-24,57,-3,66,5,5,p);c.drawRoundRect(3,57,24,66,5,5,p);
        // oversized kawaii hoodie
        fill(c,outfit);c.drawRoundRect(-43,17,43,58,19,19,p);
        fill(c,0xffd8b5f7);c.drawRoundRect(-8,23,8,42,5,5,p);
        stroke(0xffead9ff,1.5f);c.drawArc(-14,28,14,52,15,150,false,p);
        // long back hair
        fill(c,hair);c.drawOval(-48,-51,48,49,p);c.drawRoundRect(-48,-7,-34,50,13,13,p);c.drawRoundRect(34,-7,48,50,13,13,p);
        // cat ears with inner pink
        Path ears=new Path();ears.moveTo(-42,-39);ears.lineTo(-47,-73);ears.quadTo(-28,-69,-18,-48);ears.close();ears.moveTo(42,-39);ears.lineTo(47,-73);ears.quadTo(28,-69,18,-48);ears.close();
        fill(c,hair);c.drawPath(ears,p);
        Path inner=new Path();inner.moveTo(-39,-49);inner.lineTo(-42,-65);inner.lineTo(-26,-49);inner.close();inner.moveTo(39,-49);inner.lineTo(42,-65);inner.lineTo(26,-49);inner.close();fill(c,0xffffb9d2);c.drawPath(inner,p);
        // face
        fill(c,0xffffe0d7);c.drawOval(-37,-45,37,39,p);
        // bangs and side locks
        fill(c,hair);Path fringe=new Path();fringe.moveTo(-38,-20);fringe.quadTo(-39,-61,0,-59);fringe.quadTo(39,-57,38,-17);fringe.quadTo(25,-30,14,-20);fringe.quadTo(6,-8,-1,-23);fringe.quadTo(-14,-6,-23,-21);fringe.quadTo(-31,-12,-38,-20);fringe.close();c.drawPath(fringe,p);
        fill(c,0xffa98bc9);c.drawOval(-30,-48,-25,-35,p);c.drawOval(23,-47,28,-35,p);
        // eyes
        if(state==SLEEPY){stroke(0xff392642,3.2f);c.drawArc(-25,-4,-11,8,10,160,false,p);c.drawArc(11,-4,25,8,10,160,false,p);}
        else if(state==HAPPY){stroke(0xff392642,3.2f);c.drawArc(-26,-7,-10,12,195,150,false,p);c.drawArc(10,-7,26,12,195,150,false,p);}
        else if(state==SAD){stroke(0xff392642,3f);c.drawArc(-25,-1,-11,13,200,140,false,p);c.drawArc(11,-1,25,13,200,140,false,p);}
        else if(state==ANGRY){stroke(0xff392642,3f);c.drawLine(-26,-5,-12,-1,p);c.drawLine(12,-1,26,-5,p);fill(c,0xff4a2d58);c.drawOval(-24,-3,-12,10,p);c.drawOval(12,-3,24,10,p);}
        else if(state==SURPRISED){fill(c,0xff4a2d58);c.drawOval(-24,-8,-12,13,p);c.drawOval(12,-8,24,13,p);fill(c,Color.WHITE);c.drawCircle(-19,-3,3,p);c.drawCircle(17,-3,3,p);}
        else if(state==THINKING){fill(c,0xff4a2d58);c.drawOval(-24,-7,-12,12,p);c.drawOval(12,-7,24,12,p);}
        else {fill(c,0xff4a2d58);c.drawOval(-24,-7,-12,12,p);c.drawOval(12,-7,24,12,p);fill(c,Color.WHITE);c.drawCircle(-19,-2,3,p);c.drawCircle(17,-2,3,p);fill(c,0xffb77bdc);c.drawCircle(-14,7,1.7f,p);c.drawCircle(22,7,1.7f,p);}
        // blush and mouth
        fill(c,0xfff5a5b5);c.drawOval(-31,12,-17,20,p);c.drawOval(17,12,31,20,p);
        stroke(0xffa85d76,2.5f);if(state==HAPPY)c.drawArc(-6,14,6,25,0,180,false,p);else if(state==SLEEPY)c.drawLine(-4,18,4,18,p);else c.drawArc(-5,13,5,22,15,150,false,p);
        // lavender side bow
        Path bow=new Path();bow.moveTo(28,-42);bow.quadTo(13,-58,21,-66);bow.quadTo(36,-65,35,-48);bow.quadTo(48,-61,55,-50);bow.quadTo(52,-35,35,-41);bow.close();fill(c,0xffd6a4ff);c.drawPath(bow,p);fill(c,0xffffd9f6);c.drawCircle(34,-47,4,p);
        // tiny star hairpin and expression sparkles
        fill(c,0xffffd88a);Path star=new Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float r=i%2==0?5:2.2f;float x=(float)Math.cos(a)*r-28,y=(float)Math.sin(a)*r-29;if(i==0)star.moveTo(x,y);else star.lineTo(x,y);}star.close();c.drawPath(star,p);
        if(state==SLEEPY){p.setTextSize(12);fill(c,0xffe6c8ff);c.drawText("z",24,-25,p);c.drawText("Z",33,-37,p);}
        if(state==HAPPY){fill(c,0xffffa6d4);c.drawCircle(-42,-5,3,p);c.drawCircle(43,-5,3,p);}
        if(state==SAD){fill(c,0xff8ed4ff);c.drawCircle(28,15,3,p);}
        if(state==ANGRY){fill(c,0xffff7e8a);c.drawCircle(0,-29,3,p);}
        if(state==SURPRISED){stroke(0xffffd88a,2);c.drawCircle(0,-72,5,p);}
        if(state==SHY){fill(c,0xffff8db4);c.drawOval(-33,8,-14,22,p);c.drawOval(14,8,33,22,p);}
        if(state==THINKING){fill(c,0xffe6c8ff);c.drawCircle(44,-29,3,p);}
        c.restore();
    }
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();if(animator==null||!animator.isStarted())startBreathing();}
    @Override protected void onVisibilityChanged(View changedView,int visibility){super.onVisibilityChanged(changedView,visibility);if(animator==null)return;if(visibility==VISIBLE){if(!animator.isStarted())animator.start();}else animator.cancel();}
    @Override protected void onDetachedFromWindow(){if(animator!=null)animator.cancel();super.onDetachedFromWindow();}
}