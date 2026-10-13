package com.secure.documentwallet;

import android.content.*;import android.graphics.Color;import android.graphics.Typeface;import android.graphics.drawable.GradientDrawable;import android.view.*;import android.widget.*;

final class Ui {
 static int NAVY=0xff12372f,TEAL=0xff159a8c,BG=0xfff5f7f6,INK=0xff192724,MUTED=0xff64746f,SOFT=0xffe7f3ef,BORDER=0xffc8ddd6,ACCENT_DARK=0xff0b665f,GOLD=0xffd6a947,SURFACE=Color.WHITE;
 static int dp(Context c,int n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
 static GradientDrawable bg(int color,float radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
 static GradientDrawable stroke(int fill,int line,int color,float radius){GradientDrawable d=bg(fill,radius);d.setStroke(line,color);return d;}
 static TextView text(Context c,String s,int sp,int color){TextView v=new TextView(c);v.setText(s);v.setTextSize(sp);v.setTextColor(color);v.setGravity(Gravity.CENTER_VERTICAL);v.setFontFeatureSettings("kern");return v;}
 static TextView title(Context c,String s,int sp){TextView v=text(c,s,sp,INK);v.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));v.setLetterSpacing(-.01f);return v;}
 static TextView pill(Context c,String s,int color){TextView v=text(c,s,14,Color.WHITE);v.setGravity(Gravity.CENTER);v.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));v.setLetterSpacing(.015f);v.setBackground(bg(color,dp(c,18)));v.setPadding(dp(c,16),0,dp(c,16),0);v.setElevation(dp(c,3));v.setStateListAnimator(null);return v;}
 static EditText field(Context c,String hint,boolean numeric){EditText e=new EditText(c);e.setHint(hint);e.setTextSize(16);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setSingleLine(true);e.setPadding(dp(c,16),0,dp(c,16),0);e.setBackground(stroke(SURFACE,dp(c,1),BORDER,dp(c,18)));e.setElevation(dp(c,1));if(numeric)e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);return e;}
 static LinearLayout column(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l;}
 static void toast(Context c,String s){Toast.makeText(c,s,Toast.LENGTH_LONG).show();}
 static void safeScreen(android.app.Activity a){ThemeManager.apply(a);a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);darkBars(a);}
 static void darkBars(android.app.Activity a){a.getWindow().setStatusBarColor(NAVY);a.getWindow().setNavigationBarColor(NAVY);a.getWindow().getDecorView().setSystemUiVisibility(0);}
 static void lightBars(android.app.Activity a){a.getWindow().setStatusBarColor(BG);a.getWindow().setNavigationBarColor(BG);a.getWindow().getDecorView().setSystemUiVisibility(Color.luminance(BG)>.55f?View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR:0);}
 static void polishPopup(Context c,PopupWindow popup,View content,int radius){GradientDrawable shell=stroke(SURFACE,dp(c,1),BORDER,dp(c,radius));content.setBackground(shell);content.setElevation(dp(c,18));popup.setBackgroundDrawable(stroke(SURFACE,dp(c,1),BORDER,dp(c,radius)));popup.setElevation(dp(c,18));popup.setOutsideTouchable(true);popup.setFocusable(true);}
}
