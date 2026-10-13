package com.secure.documentwallet;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/** Lightweight Lucide-style icon renderer used by the V8 interface. */
final class VaultIconView extends View {
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private String icon="vault";private int color=Ui.TEAL;
 VaultIconView(Context c){super(c);init();}VaultIconView(Context c, AttributeSet a){super(c,a);init();}
 private void init(){p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
 VaultIconView setIcon(String value){icon=value==null?"vault":value;invalidate();return this;}
 VaultIconView setColor(int value){color=value;invalidate();return this;}
 @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),s=Math.min(w,h),l=(w-s)/2f,t=(h-s)/2f;p.setColor(color);p.setStrokeWidth(Math.max(1.7f,s*.085f));p.setStyle(Paint.Style.STROKE);RectF r=new RectF(l+s*.2f,t+s*.2f,l+s*.8f,t+s*.8f);
  switch(icon){
   case "vault":c.drawRoundRect(new RectF(l+s*.18f,t+s*.18f,l+s*.82f,t+s*.82f),s*.08f,s*.08f,p);circle(c,l+s*.5f,t+s*.5f,s*.18f);circle(c,l+s*.5f,t+s*.5f,s*.035f);line(c,l+s*.5f,t+s*.32f,l+s*.5f,t+s*.41f);line(c,l+s*.65f,t+s*.59f,l+s*.73f,t+s*.64f);break;
   case "settings":circle(c,l+s*.5f,t+s*.5f,s*.16f);for(int i=0;i<8;i++){double a=i*Math.PI/4;line(c,l+s*.5f+(float)Math.cos(a)*s*.25f,t+s*.5f+(float)Math.sin(a)*s*.25f,l+s*.5f+(float)Math.cos(a)*s*.36f,t+s*.5f+(float)Math.sin(a)*s*.36f);}break;
   case "search":circle(c,l+s*.43f,t+s*.43f,s*.23f);line(c,l+s*.6f,t+s*.6f,l+s*.8f,t+s*.8f);break;
   case "mic":c.drawRoundRect(new RectF(l+s*.38f,t+s*.18f,l+s*.62f,t+s*.62f),s*.14f,s*.14f,p);c.drawArc(new RectF(l+s*.27f,t+s*.37f,l+s*.73f,t+s*.78f),0,180,false,p);line(c,l+s*.5f,t+s*.78f,l+s*.5f,t+s*.9f);break;
   case "shield":path(c,new float[]{.5f,.14f,.78f,.25f,.75f,.6f,.5f,.84f,.25f,.6f,.22f,.25f,.5f,.14f},l,t,s);check(c,l+s*.34f,t+s*.49f,s*.16f);break;
   case "id":c.drawRoundRect(r,s*.06f,s*.06f,p);circle(c,l+s*.39f,t+s*.42f,s*.09f);c.drawArc(new RectF(l+s*.27f,t+s*.47f,l+s*.51f,t+s*.67f),180,180,false,p);line(c,l+s*.58f,t+s*.38f,l+s*.7f,t+s*.38f);line(c,l+s*.58f,t+s*.53f,l+s*.7f,t+s*.53f);break;
   case "card":c.drawRoundRect(r,s*.07f,s*.07f,p);line(c,l+s*.2f,t+s*.38f,l+s*.8f,t+s*.38f);line(c,l+s*.3f,t+s*.65f,l+s*.45f,t+s*.65f);break;
   case "file":case "certificate":path(c,new float[]{.3f,.15f,.62f,.15f,.78f,.31f,.78f,.85f,.3f,.85f,.3f,.15f},l,t,s);line(c,l+s*.61f,t+s*.16f,l+s*.61f,t+s*.32f);line(c,l+s*.61f,t+s*.32f,l+s*.76f,t+s*.32f);line(c,l+s*.4f,t+s*.48f,l+s*.67f,t+s*.48f);line(c,l+s*.4f,t+s*.62f,l+s*.67f,t+s*.62f);if(icon.equals("certificate")){circle(c,l+s*.53f,t+s*.72f,s*.08f);}break;
   case "info":c.drawRoundRect(r,s*.08f,s*.08f,p);circle(c,l+s*.5f,t+s*.4f,s*.09f);c.drawArc(new RectF(l+s*.34f,t+s*.47f,l+s*.66f,t+s*.72f),180,180,false,p);break;
   case "image":c.drawRoundRect(r,s*.07f,s*.07f,p);circle(c,l+s*.64f,t+s*.36f,s*.06f);path(c,new float[]{.24f,.72f,.4f,.52f,.53f,.64f,.63f,.54f,.78f,.72f},l,t,s);break;
   case "video":c.drawRoundRect(new RectF(l+s*.2f,t+s*.28f,l+s*.62f,t+s*.72f),s*.07f,s*.07f,p);path(c,new float[]{.62f,.42f,.82f,.31f,.82f,.69f,.62f,.58f},l,t,s);break;
   case "audio":case "voice":c.drawRoundRect(new RectF(l+s*.39f,t+s*.18f,l+s*.61f,t+s*.62f),s*.12f,s*.12f,p);c.drawArc(new RectF(l+s*.28f,t+s*.4f,l+s*.72f,t+s*.8f),0,180,false,p);line(c,l+s*.5f,t+s*.8f,l+s*.5f,t+s*.88f);break;
   case "home":path(c,new float[]{.16f,.47f,.5f,.18f,.84f,.47f,.75f,.47f,.75f,.82f,.25f,.82f,.25f,.47f},l,t,s);break;
   case "folder":path(c,new float[]{.16f,.3f,.42f,.3f,.5f,.4f,.84f,.4f,.78f,.78f,.16f,.78f,.16f,.3f},l,t,s);break;
   case "scan":for(int i=0;i<4;i++){float x=(i%2==0?.2f:.8f),y=(i<2?.2f:.8f),dx=(i%2==0?.17f:-.17f),dy=(i<2?.17f:-.17f);line(c,l+x*s,t+y*s,l+(x+dx)*s,t+y*s);line(c,l+x*s,t+y*s,l+x*s,t+(y+dy)*s);}break;
   case "clock":circle(c,l+s*.5f,t+s*.5f,s*.32f);line(c,l+s*.5f,t+s*.5f,l+s*.5f,t+s*.31f);line(c,l+s*.5f,t+s*.5f,l+s*.66f,t+s*.58f);break;
   case "user":circle(c,l+s*.5f,t+s*.35f,s*.14f);c.drawArc(new RectF(l+s*.24f,t+s*.48f,l+s*.76f,t+s*.86f),180,180,false,p);break;
   case "back":line(c,l+s*.72f,t+s*.2f,l+s*.34f,t+s*.5f);line(c,l+s*.34f,t+s*.5f,l+s*.72f,t+s*.8f);break;
   case "plus":line(c,l+s*.5f,t+s*.2f,l+s*.5f,t+s*.8f);line(c,l+s*.2f,t+s*.5f,l+s*.8f,t+s*.5f);break;
   case "upload":line(c,l+s*.5f,t+s*.72f,l+s*.5f,t+s*.2f);line(c,l+s*.5f,t+s*.2f,l+s*.32f,t+s*.4f);line(c,l+s*.5f,t+s*.2f,l+s*.68f,t+s*.4f);c.drawArc(new RectF(l+s*.2f,t+s*.52f,l+s*.8f,t+s*.9f),0,180,false,p);break;
   case "edit":line(c,l+s*.28f,t+s*.72f,l+s*.7f,t+s*.3f);path(c,new float[]{.25f,.75f,.37f,.72f,.28f,.63f,.25f,.75f},l,t,s);break;
   case "crop":path(c,new float[]{.25f,.15f,.25f,.68f,.78f,.68f},l,t,s);path(c,new float[]{.75f,.85f,.75f,.32f,.22f,.32f},l,t,s);break;
   case "share":circle(c,l+s*.28f,t+s*.5f,s*.08f);circle(c,l+s*.7f,t+s*.28f,s*.08f);circle(c,l+s*.7f,t+s*.72f,s*.08f);line(c,l+s*.35f,t+s*.46f,l+s*.63f,t+s*.32f);line(c,l+s*.35f,t+s*.54f,l+s*.63f,t+s*.68f);break;
   case "check":check(c,l+s*.25f,t+s*.5f,s*.25f);break;
   case "bell":c.drawArc(new RectF(l+s*.3f,t+s*.2f,l+s*.7f,t+s*.72f),180,180,false,p);line(c,l+s*.3f,t+s*.46f,l+s*.24f,t+s*.72f);line(c,l+s*.24f,t+s*.72f,l+s*.76f,t+s*.72f);circle(c,l+s*.5f,t+s*.82f,s*.04f);break;
   case "download":line(c,l+s*.5f,t+s*.2f,l+s*.5f,t+s*.65f);line(c,l+s*.5f,t+s*.65f,l+s*.32f,t+s*.47f);line(c,l+s*.5f,t+s*.65f,l+s*.68f,t+s*.47f);line(c,l+s*.25f,t+s*.82f,l+s*.75f,t+s*.82f);break;
   case "close":line(c,l+s*.25f,t+s*.25f,l+s*.75f,t+s*.75f);line(c,l+s*.75f,t+s*.25f,l+s*.25f,t+s*.75f);break;
   default:c.drawRoundRect(r,s*.08f,s*.08f,p);circle(c,l+s*.5f,t+s*.5f,s*.18f);break;
  }
 }
 private void line(Canvas c,float x1,float y1,float x2,float y2){c.drawLine(x1,y1,x2,y2,p);}private void circle(Canvas c,float x,float y,float r){c.drawCircle(x,y,r,p);}private void check(Canvas c,float x,float y,float s){line(c,x,y,x+s*.35f,y+s*.35f);line(c,x+s*.35f,y+s*.35f,x+s,y-s*.35f);}private void path(Canvas c,float[] pts,float l,float t,float s){Path q=new Path();q.moveTo(l+pts[0]*s,t+pts[1]*s);for(int i=2;i<pts.length;i+=2)q.lineTo(l+pts[i]*s,t+pts[i+1]*s);c.drawPath(q,p);}
}
