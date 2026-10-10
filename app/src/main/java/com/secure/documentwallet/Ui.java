package com.secure.documentwallet;

import android.content.*;import android.graphics.Color;import android.graphics.Typeface;import android.graphics.drawable.GradientDrawable;import android.view.*;import android.widget.*;

final class Ui {
 static final int NAVY=Color.rgb(18,55,47),TEAL=Color.rgb(21,154,140),BG=Color.rgb(245,247,246),INK=Color.rgb(25,39,36),MUTED=Color.rgb(100,116,111),SOFT=Color.rgb(231,243,239);
 static int dp(Context c,int n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
 static GradientDrawable bg(int color,float radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
 static GradientDrawable stroke(int fill,int line,int color,float radius){GradientDrawable d=bg(fill,radius);d.setStroke(line,color);return d;}
 static TextView text(Context c,String s,int sp,int color){TextView v=new TextView(c);v.setText(s);v.setTextSize(sp);v.setTextColor(color);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
 static TextView title(Context c,String s,int sp){TextView v=text(c,s,sp,INK);v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
 static TextView pill(Context c,String s,int color){TextView v=text(c,s,14,Color.WHITE);v.setGravity(Gravity.CENTER);v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);v.setBackground(bg(color,dp(c,24)));v.setPadding(dp(c,16),0,dp(c,16),0);return v;}
 static EditText field(Context c,String hint,boolean numeric){EditText e=new EditText(c);e.setHint(hint);e.setTextSize(16);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setSingleLine(true);e.setPadding(dp(c,15),0,dp(c,15),0);e.setBackground(stroke(Color.WHITE,dp(c,1),0xffc8d8d3,dp(c,14)));if(numeric)e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);return e;}
 static LinearLayout column(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l;}
 static void toast(Context c,String s){Toast.makeText(c,s,Toast.LENGTH_LONG).show();}
 static void safeScreen(android.app.Activity a){a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);darkBars(a);}
 static void darkBars(android.app.Activity a){a.getWindow().setStatusBarColor(NAVY);a.getWindow().setNavigationBarColor(NAVY);a.getWindow().getDecorView().setSystemUiVisibility(0);}
 static void lightBars(android.app.Activity a){a.getWindow().setStatusBarColor(0xfffbfaf6);a.getWindow().setNavigationBarColor(0xfffbfaf6);a.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);}
}
