package com.secure.documentwallet;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.function.Consumer;

/** Theme-matched PIN keypad that never invokes or overlaps with the system keyboard. */
final class PinPadScreen {
 private PinPadScreen(){}
 static void show(MainActivity host,PinSecurity security,Runnable openVault,Consumer<String> migrateLegacy){
  int length=security.pinLength();Ui.lightBars(host);host.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  FrameLayout root=new FrameLayout(host);root.setBackgroundColor(0xfffbfaf6);LinearLayout page=Ui.column(host);page.setGravity(Gravity.CENTER_HORIZONTAL);page.setPadding(Ui.dp(host,26),Ui.dp(host,20),Ui.dp(host,26),Ui.dp(host,20));
  ImageView emblem=new ImageView(host);emblem.setImageResource(R.drawable.ic_vault_square);emblem.setScaleType(ImageView.ScaleType.CENTER_CROP);emblem.setBackground(Ui.bg(0xff061f3b,Ui.dp(host,25)));emblem.setClipToOutline(true);emblem.setElevation(Ui.dp(host,7));page.addView(emblem,new LinearLayout.LayoutParams(Ui.dp(host,96),Ui.dp(host,96)));
  TextView name=Ui.title(host,"My Vault",33);name.setTextColor(0xff061f3b);name.setGravity(Gravity.CENTER);LinearLayout.LayoutParams nameParams=new LinearLayout.LayoutParams(-1,-2);nameParams.topMargin=Ui.dp(host,10);page.addView(name,nameParams);
  TextView privacy=Ui.text(host,"Private · Encrypted · Available Offline",13,0xff727a87);privacy.setGravity(Gravity.CENTER);page.addView(privacy);
  LinearLayout card=Ui.column(host);card.setGravity(Gravity.CENTER_HORIZONTAL);card.setPadding(Ui.dp(host,18),Ui.dp(host,17),Ui.dp(host,18),Ui.dp(host,18));card.setBackground(Ui.bg(Color.WHITE,Ui.dp(host,26)));card.setElevation(Ui.dp(host,8));
  TextView prompt=Ui.title(host,length==4?"Enter Your 4-Digit PIN":"Enter Your Existing 6-Digit PIN",18);prompt.setTextColor(0xff061f3b);prompt.setGravity(Gravity.CENTER);card.addView(prompt);
  TextView dots=Ui.text(host,dots(0,length),30,Ui.TEAL);dots.setGravity(Gravity.CENTER);dots.setLetterSpacing(.18f);dots.setBackground(Ui.bg(0xffe7f8f4,Ui.dp(host,24)));LinearLayout.LayoutParams dotsParams=new LinearLayout.LayoutParams(-1,Ui.dp(host,58));dotsParams.setMargins(0,Ui.dp(host,12),0,Ui.dp(host,12));card.addView(dots,dotsParams);
  GridLayout keypad=new GridLayout(host);keypad.setColumnCount(3);String[] keys={"1","2","3","4","5","6","7","8","9","","0","⌫"};StringBuilder entered=new StringBuilder();boolean[] busy={false};
  for(int i=0;i<keys.length;i++){String value=keys[i];TextView key=key(host,value);if(value.isEmpty()){key.setVisibility(View.INVISIBLE);key.setEnabled(false);}else key.setOnClickListener(v->{if(busy[0])return;v.animate().scaleX(.86f).scaleY(.86f).setDuration(65).withEndAction(()->v.animate().scaleX(1).scaleY(1).setDuration(90).start()).start();if("⌫".equals(value)){if(entered.length()>0)entered.deleteCharAt(entered.length()-1);}else if(entered.length()<length)entered.append(value);dots.setText(dots(entered.length(),length));dots.setTextColor(Ui.TEAL);if(entered.length()==length){busy[0]=true;card.postDelayed(()->{String code=entered.toString();if(!security.verify(code)){shake(host,card);dots.setTextColor(0xffb3261e);entered.setLength(0);dots.postDelayed(()->{dots.setText(dots(0,length));dots.setTextColor(Ui.TEAL);busy[0]=false;},360);return;}if(length==6)migrateLegacy.accept(code);else try{Session.unlock(code,security.vaultSalt());openVault.run();}catch(Exception error){busy[0]=false;entered.setLength(0);dots.setText(dots(0,length));Ui.toast(host,"Could Not Unlock Encryption");}},150);}});
   GridLayout.LayoutParams kp=new GridLayout.LayoutParams();kp.width=0;kp.height=Ui.dp(host,54);kp.columnSpec=GridLayout.spec(i%3,1f);kp.rowSpec=GridLayout.spec(i/3);kp.setMargins(Ui.dp(host,5),Ui.dp(host,4),Ui.dp(host,5),Ui.dp(host,4));keypad.addView(key,kp);
  }
  card.addView(keypad,new LinearLayout.LayoutParams(-1,Ui.dp(host,248)));TextView automatic=Ui.text(host,"My Vault Opens Automatically After The Final Digit",12,Ui.MUTED);automatic.setGravity(Gravity.CENTER);card.addView(automatic);
  LinearLayout.LayoutParams cardParams=new LinearLayout.LayoutParams(-1,-2);cardParams.topMargin=Ui.dp(host,16);page.addView(card,cardParams);FrameLayout.LayoutParams pageParams=new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER);root.addView(page,pageParams);host.setContentView(root);
 }
 private static TextView key(MainActivity host,String label){TextView key=Ui.text(host,label,20,0xff061f3b);key.setGravity(Gravity.CENTER);key.setTypeface(null,android.graphics.Typeface.BOLD);key.setBackground(Ui.stroke(0xfff7fbfa,Ui.dp(host,1),0xffc6ddd7,Ui.dp(host,18)));key.setContentDescription(label.equals("⌫")?"Delete Last Digit":"PIN "+label);return key;}
 private static String dots(int filled,int length){StringBuilder value=new StringBuilder();for(int i=0;i<length;i++){if(i>0)value.append("  ");value.append(i<filled?"●":"○");}return value.toString();}
 private static void shake(MainActivity host,View view){ObjectAnimator shake=ObjectAnimator.ofFloat(view,"translationX",0,-Ui.dp(host,14),Ui.dp(host,14),-Ui.dp(host,9),Ui.dp(host,9),0);shake.setDuration(330);shake.start();}
}
