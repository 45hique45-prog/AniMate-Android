package com.animate.companion;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class MascotView extends View {
    public static final int IDLE=0, HAPPY=1, SLEEPY=2;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private int state=IDLE;
    private float breathe=1f;
    private ValueAnimator animator;
    public MascotView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    public void setState(int s){state=s;invalidate();}
    public int getState(){return state;}
    private void startBreathing(){
        animator=ValueAnimator.ofFloat(0.985f,1.015f);animator.setDuration(1450);animator.setRepeatMode(ValueAnimator.REVERSE);animator.setRepeatCount(ValueAnimator.INFINITE);animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(a->{breathe=(Float)a.getAnimatedValue();invalidate();});animator.start();
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        c.save();c.translate(w/2f,h/2f);c.scale(Math.min(w/116f,h/126f)*breathe,Math.min(w/116f,h/126f)*breathe);
        // soft lavender shadow
        p.setColor(0x443F285E);p.setStyle(Paint.Style.FILL);c.drawOval(-43,48,43,60,p);
        // hair back and long side locks
        p.setColor(0xff302447);c.drawOval(-48,-48,48,55,p);
        p.setColor(0xff49335f);c.drawRoundRect(-48,-8,-34,48,16,16,p);c.drawRoundRect(34,-8,48,48,16,16,p);
        // face
        p.setColor(0xffffdfd7);c.drawOval(-38,-43,38,43,p);
        // fringe
        p.setColor(0xff49335f);Path fringe=new Path();fringe.moveTo(-39,-22);fringe.quadTo(-38,-62,0,-56);fringe.quadTo(40,-55,39,-18);fringe.quadTo(22,-31,12,-18);fringe.quadTo(4,-8,-2,-20);fringe.quadTo(-16,-5,-23,-19);fringe.quadTo(-32,-10,-39,-22);fringe.close();c.drawPath(fringe,p);
        // tiny hair shine
        p.setColor(0xffa98bc9);p.setStrokeWidth(3);p.setStyle(Paint.Style.STROKE);c.drawArc(-25,-49,14,-17,205,48,false,p);p.setStyle(Paint.Style.FILL);
        // ears
        p.setColor(0xffffc9c5);c.drawCircle(-38,2,8,p);c.drawCircle(38,2,8,p);
        // blush
        p.setColor(0xfff5a5ad);c.drawOval(-30,12,-16,21,p);c.drawOval(16,12,30,21,p);
        // eyes and expression
        p.setColor(0xff35263f);p.setStrokeWidth(3.2f);p.setStrokeCap(Paint.Cap.ROUND);
        if(state==SLEEPY){c.drawLine(-23,0,-13,0,p);c.drawLine(13,0,23,0,p);}
        else if(state==HAPPY){c.drawArc(-25,-8,-11,8,200,140,false,p);c.drawArc(11,-8,25,8,200,140,false,p);}
        else {c.drawOval(-23,-5,-15,8,p);c.drawOval(15,-5,23,8,p);p.setColor(Color.WHITE);c.drawCircle(-18,-2,2,p);c.drawCircle(20,-2,2,p);}
        // mouth
        p.setColor(0xffa85d76);p.setStrokeWidth(2.8f);
        if(state==SLEEPY){c.drawLine(-4,17,4,17,p);p.setTextSize(12);p.setStyle(Paint.Style.FILL);c.drawText("z",24,-27,p);c.drawText("Z",33,-39,p);}
        else if(state==HAPPY){p.setStyle(Paint.Style.FILL);c.drawOval(-6,13,6,23,p);}
        else {p.setStyle(Paint.Style.STROKE);c.drawArc(-6,12,6,22,20,140,false,p);p.setStyle(Paint.Style.FILL);}
        // small bow
        p.setColor(0xffd9a8ff);Path bow=new Path();bow.moveTo(27,-45);bow.quadTo(15,-58,21,-66);bow.quadTo(34,-64,33,-49);bow.quadTo(44,-60,51,-51);bow.quadTo(48,-37,33,-42);bow.close();c.drawPath(bow,p);p.setColor(0xffffd4f3);c.drawCircle(32,-48,4,p);
        c.restore();
    }
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();if(animator==null||!animator.isStarted())startBreathing();}
    @Override protected void onVisibilityChanged(View changedView,int visibility){super.onVisibilityChanged(changedView,visibility);if(animator==null)return;if(visibility==VISIBLE){if(!animator.isStarted())animator.start();}else animator.cancel();}
    @Override protected void onDetachedFromWindow(){if(animator!=null)animator.cancel();super.onDetachedFromWindow();}
}