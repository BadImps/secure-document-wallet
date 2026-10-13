package com.secure.documentwallet;

import android.content.*;

/** Vault palettes. Document previews keep their own neutral paper/card surface. */
final class ThemeManager {
 static final String AEGIS="aegis",EMERALD="emerald",OCEAN="ocean",SAND="sand",DARK="dark",GLASS="glass";private static final String PREF="appearance",KEY="theme";
 static String current(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE).getString(KEY,AEGIS);}
 static int selected(Context c){String value=current(c);return EMERALD.equals(value)?1:OCEAN.equals(value)?2:SAND.equals(value)?3:DARK.equals(value)?4:GLASS.equals(value)?5:0;}
 static void set(Context c,int index){String value=index==1?EMERALD:index==2?OCEAN:index==3?SAND:index==4?DARK:index==5?GLASS:AEGIS;c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString(KEY,value).apply();apply(c);}
 static boolean dark(Context c){return DARK.equals(current(c));}
 static void apply(Context c){String value=current(c);if(AEGIS.equals(value)){Ui.NAVY=0xff174d9f;Ui.TEAL=0xff2368d7;Ui.BG=0xffeef3fa;Ui.INK=0xff101828;Ui.MUTED=0xff667085;Ui.SOFT=0xffe8f0ff;Ui.BORDER=0xffdfe5ed;Ui.ACCENT_DARK=0xff174d9f;Ui.GOLD=0xffd9a83e;Ui.SURFACE=0xffffffff;}else if(OCEAN.equals(value)){Ui.NAVY=0xff12364f;Ui.TEAL=0xff147fa3;Ui.BG=0xfff3f7fa;Ui.INK=0xff172b39;Ui.MUTED=0xff607682;Ui.SOFT=0xffe3f1f7;Ui.BORDER=0xffc5dde8;Ui.ACCENT_DARK=0xff185f7c;Ui.GOLD=0xffd5a542;Ui.SURFACE=0xffffffff;}else if(SAND.equals(value)){Ui.NAVY=0xff49372c;Ui.TEAL=0xffa5653e;Ui.BG=0xfffbf7ef;Ui.INK=0xff30251f;Ui.MUTED=0xff75685e;Ui.SOFT=0xfff3e8d9;Ui.BORDER=0xffe1cdb5;Ui.ACCENT_DARK=0xff80513a;Ui.GOLD=0xffc98c31;Ui.SURFACE=0xfffffdf9;}else if(DARK.equals(value)){Ui.NAVY=0xff61a0ff;Ui.TEAL=0xff61a0ff;Ui.BG=0xff0b111b;Ui.INK=0xfff7f9fc;Ui.MUTED=0xff9cacc0;Ui.SOFT=0xff182e50;Ui.BORDER=0xff2b3849;Ui.ACCENT_DARK=0xff174d9f;Ui.GOLD=0xffffbd18;Ui.SURFACE=0xff151d29;}else if(GLASS.equals(value)){Ui.NAVY=0xff31456e;Ui.TEAL=0xff635bdf;Ui.BG=0xffedf3ff;Ui.INK=0xff17243b;Ui.MUTED=0xff65728b;Ui.SOFT=0xdde9efff;Ui.BORDER=0xffc8d4eb;Ui.ACCENT_DARK=0xff5b57cb;Ui.GOLD=0xffd5a951;Ui.SURFACE=0xeef9fbff;}else{Ui.NAVY=0xff12372f;Ui.TEAL=0xff159a8c;Ui.BG=0xfff5f7f6;Ui.INK=0xff192724;Ui.MUTED=0xff64746f;Ui.SOFT=0xffe7f3ef;Ui.BORDER=0xffc8ddd6;Ui.ACCENT_DARK=0xff0b665f;Ui.GOLD=0xffd6a947;Ui.SURFACE=0xffffffff;}}
 static int headerStart(Context c){return AEGIS.equals(current(c))?0xff153b65:OCEAN.equals(current(c))?0xff102e48:SAND.equals(current(c))?0xff432f23:DARK.equals(current(c))?0xff080d0e:GLASS.equals(current(c))?0xff493b9c:0xff0d302c;}
 static int headerEnd(Context c){return AEGIS.equals(current(c))?0xff2f7fce:OCEAN.equals(current(c))?0xff167da0:SAND.equals(current(c))?0xffa5653e:DARK.equals(current(c))?0xff1e2d2a:GLASS.equals(current(c))?0xff728bd8:0xff0b8177;}
 static int tileStart(Context c,int index){int[] values={0xff174d9f,0xff0f897c,0xff167fa2,0xff9b6543,0xff101819,0xff4b83c4};return values[index%values.length];}
 static int tileEnd(Context c,int index){int[] values={0xff2f80ed,0xff16b39f,0xff28abc2,0xffdda847,0xff293231,0xffaaa0e5};return values[index%values.length];}
}
