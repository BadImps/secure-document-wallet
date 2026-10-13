package com.secure.documentwallet;

import android.content.*;

/** Light, document-friendly palettes chosen to preserve scan and card legibility. */
final class ThemeManager {
 static final String EMERALD="emerald",OCEAN="ocean",SAND="sand";private static final String PREF="appearance",KEY="theme";
 static String current(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE).getString(KEY,EMERALD);}
 static int selected(Context c){String value=current(c);return OCEAN.equals(value)?1:SAND.equals(value)?2:0;}
 static void set(Context c,int index){c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString(KEY,index==1?OCEAN:index==2?SAND:EMERALD).apply();apply(c);}
 static void apply(Context c){String value=current(c);if(OCEAN.equals(value)){Ui.NAVY=0xff12364f;Ui.TEAL=0xff147fa3;Ui.BG=0xfff3f7fa;Ui.INK=0xff172b39;Ui.MUTED=0xff607682;Ui.SOFT=0xffe3f1f7;Ui.BORDER=0xffc5dde8;Ui.ACCENT_DARK=0xff185f7c;Ui.GOLD=0xffd5a542;Ui.SURFACE=0xffffffff;}else if(SAND.equals(value)){Ui.NAVY=0xff49372c;Ui.TEAL=0xffa5653e;Ui.BG=0xfffbf7ef;Ui.INK=0xff30251f;Ui.MUTED=0xff75685e;Ui.SOFT=0xfff3e8d9;Ui.BORDER=0xffe1cdb5;Ui.ACCENT_DARK=0xff80513a;Ui.GOLD=0xffc98c31;Ui.SURFACE=0xfffffdf9;}else{Ui.NAVY=0xff12372f;Ui.TEAL=0xff159a8c;Ui.BG=0xfff5f7f6;Ui.INK=0xff192724;Ui.MUTED=0xff64746f;Ui.SOFT=0xffe7f3ef;Ui.BORDER=0xffc8ddd6;Ui.ACCENT_DARK=0xff0b665f;Ui.GOLD=0xffd6a947;Ui.SURFACE=0xffffffff;}}
 static int headerStart(Context c){return OCEAN.equals(current(c))?0xff102e48:SAND.equals(current(c))?0xff432f23:0xff0d302c;}
 static int headerEnd(Context c){return OCEAN.equals(current(c))?0xff167da0:SAND.equals(current(c))?0xffa5653e:0xff0b8177;}
 static int tileStart(Context c,int index){int[] values=OCEAN.equals(current(c))?new int[]{0xff167fa2,0xff4869ad,0xff21699d,0xff2a7b78,0xff6851a3,0xffa44f77}:SAND.equals(current(c))?new int[]{0xff9b6543,0xffb57a26,0xff8b5a43,0xff73804b,0xff75536f,0xffa64f4f}:new int[]{0xff0f897c,0xffb27a19,0xff236ca3,0xff2b7b66,0xff6b4bae,0xffbd5267};return values[index%values.length];}
 static int tileEnd(Context c,int index){int[] values=OCEAN.equals(current(c))?new int[]{0xff28abc2,0xff718bc9,0xff3b91c8,0xff4aa6a1,0xff9278ca,0xffd5779b}:SAND.equals(current(c))?new int[]{0xffc78a5d,0xffdda847,0xffb57d61,0xff9ba461,0xff9e7596,0xffd87969}:new int[]{0xff16b39f,0xffe0a62a,0xff3d91c4,0xff47a887,0xff9b72d0,0xffef7c72};return values[index%values.length];}
}
