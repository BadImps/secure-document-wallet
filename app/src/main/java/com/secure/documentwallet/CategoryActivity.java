package com.secure.documentwallet;

import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.FragmentActivity;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;

/** Professional animated wallet stack shared by all four vault categories. */
public class CategoryActivity extends FragmentActivity {
 private final ExecutorService io=Executors.newSingleThreadExecutor();
 private final ArrayList<JSONObject> items=new ArrayList<>();
 private VaultRepository repo; private String group; private int index; private boolean ready;
 private FrameLayout active,rearNear,rearDeep; private TextView itemTitle,counter,side;
 private Bitmap faceImage,rearNearImage,rearDeepImage; private float downY;

 @Override public void onCreate(Bundle b){super.onCreate(b);Ui.safeScreen(this);Ui.lightBars(this);if(!Session.valid()){finish();return;}group=getIntent().getStringExtra("group");if(group==null)group=DocumentGroups.DOCUMENTS;repo=new VaultRepository(this);build();load();}

 private void build(){
  LinearLayout page=Ui.column(this);page.setBackgroundColor(0xfffbfaf6);page.setPadding(Ui.dp(this,18),Ui.dp(this,14),Ui.dp(this,18),Ui.dp(this,24));
  LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);
  TextView back=Ui.text(this,"‹",40,0xff061f3b);back.setGravity(Gravity.CENTER);back.setContentDescription("Back To My Vault");back.setOnClickListener(v->finish());bar.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,54)));
  ImageView icon=new ImageView(this);icon.setImageResource(DocumentGroups.icon(group));icon.setPadding(Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8));icon.setBackground(Ui.bg(DocumentGroups.color(group),Ui.dp(this,28)));bar.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,52),Ui.dp(this,52)));
  LinearLayout names=Ui.column(this);names.setPadding(Ui.dp(this,13),0,0,0);TextView heading=Ui.title(this,group,25);heading.setTextColor(0xff061f3b);names.addView(heading);counter=Ui.text(this,"Opening Secure Stack…",13,0xff727a87);names.addView(counter);bar.addView(names,new LinearLayout.LayoutParams(0,-2,1));page.addView(bar);
  TextView instruction=Ui.text(this,"Tap To Open  ·  Flick Down For Next",14,Ui.TEAL);instruction.setTypeface(null,android.graphics.Typeface.BOLD);instruction.setGravity(Gravity.CENTER);instruction.setPadding(0,Ui.dp(this,20),0,Ui.dp(this,12));page.addView(instruction);

  int availableDp=Math.round(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density)-46;
  int faceDp;
  if(DocumentGroups.CARDS.equals(group)||DocumentGroups.ID.equals(group))faceDp=Math.round(availableDp/1.586f);
  else if(DocumentGroups.CERTIFICATES.equals(group))faceDp=Math.round(availableDp/1.294f);
  else faceDp=Math.min(430,Math.round(availableDp*1.294f));
  int faceHeight=Ui.dp(this,faceDp);
  int deckHeight=faceHeight+Ui.dp(this,42);
  FrameLayout deck=new FrameLayout(this);deck.setClipChildren(false);
  rearDeep=layer(.56f,.91f,31);rearNear=layer(.78f,.96f,17);deck.addView(rearDeep,params(faceHeight,18));deck.addView(rearNear,params(faceHeight,9));
  active=new FrameLayout(this);active.setBackground(Ui.stroke(Color.WHITE,Ui.dp(this,1),0xffd8c47b,Ui.dp(this,22)));active.setClipToOutline(true);active.setElevation(Ui.dp(this,11));active.setOnClickListener(v->open());active.setOnTouchListener((v,e)->{if(e.getActionMasked()==MotionEvent.ACTION_DOWN){downY=e.getY();return true;}if(e.getActionMasked()==MotionEvent.ACTION_UP){if(e.getY()-downY>Ui.dp(this,58))next();else v.performClick();return true;}return true;});deck.addView(active,params(faceHeight,0));page.addView(deck,new LinearLayout.LayoutParams(-1,deckHeight));
  itemTitle=Ui.title(this,"",20);itemTitle.setTextColor(0xff061f3b);itemTitle.setGravity(Gravity.CENTER);page.addView(itemTitle);side=Ui.text(this,"Encrypted On This Phone",13,Ui.MUTED);side.setGravity(Gravity.CENTER);side.setPadding(0,Ui.dp(this,5),0,0);page.addView(side);
  TextView hint=Ui.text(this,"Cards stay at their natural proportions so the full document remains visible. Flick down to bring the next saved item to the front.",13,Ui.MUTED);hint.setGravity(Gravity.CENTER);hint.setLineSpacing(0,1.12f);hint.setPadding(Ui.dp(this,16),Ui.dp(this,17),Ui.dp(this,16),0);page.addView(hint);setContentView(page);
 }

 private FrameLayout layer(float alpha,float scale,int drop){FrameLayout v=new FrameLayout(this);v.setBackground(Ui.stroke(DocumentGroups.color(group),Ui.dp(this,1),0xffbfd4cf,Ui.dp(this,22)));v.setClipToOutline(true);v.setAlpha(alpha);v.setScaleX(scale);v.setTranslationY(Ui.dp(this,drop));v.setElevation(Ui.dp(this,3));return v;}
 private FrameLayout.LayoutParams params(int h,int top){FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,h,Gravity.TOP|Gravity.CENTER_HORIZONTAL);p.setMargins(Ui.dp(this,5),Ui.dp(this,top),Ui.dp(this,5),0);return p;}
 private void load(){ready=false;io.execute(()->{try{JSONArray all=repo.all();items.clear();for(int i=all.length()-1;i>=0;i--){JSONObject d=all.optJSONObject(i);if(d!=null&&group.equals(DocumentGroups.fromCategory(d.optString("category"))))items.add(d);}runOnUiThread(()->{if(items.isEmpty())empty();else{index=0;show(false);}});}catch(Exception e){runOnUiThread(()->Ui.toast(this,"Could Not Open This Category"));}});}

 private void show(boolean animate){
  ready=false;releaseImages();active.removeAllViews();rearNear.removeAllViews();rearDeep.removeAllViews();ProgressBar p=new ProgressBar(this);active.addView(p,new FrameLayout.LayoutParams(Ui.dp(this,46),Ui.dp(this,46),Gravity.CENTER));
  JSONObject d=items.get(index);itemTitle.setText(d.optString("title","Document"));counter.setText((index+1)+" Of "+items.size());JSONArray pages=d.optJSONArray("pages");int total=pages==null?0:pages.length();side.setText(total+" Encrypted Page"+(total==1?"":"s"));
  if(animate){active.setAlpha(0);active.setTranslationY(-Ui.dp(this,46));active.setRotation(-4);}
  final int requested=index;io.execute(()->{try{Bitmap[] decoded=new Bitmap[3];decoded[0]=decode(repo.page(d,0));if(items.size()>1)decoded[1]=decode(repo.page(items.get((requested+1)%items.size()),0));if(items.size()>2)decoded[2]=decode(repo.page(items.get((requested+2)%items.size()),0));runOnUiThread(()->{if(isFinishing()||requested!=index){for(Bitmap b:decoded)if(b!=null)b.recycle();return;}faceImage=decoded[0];rearNearImage=decoded[1];rearDeepImage=decoded[2];putImage(active,faceImage,true);putImage(rearNear,rearNearImage,false);putImage(rearDeep,rearDeepImage,false);ready=true;if(animate)active.animate().alpha(1).translationY(0).rotation(0).setDuration(285).start();});}catch(Exception ex){runOnUiThread(()->{active.removeAllViews();TextView err=Ui.text(this,"Could Not Decrypt This Item",15,0xffb3261e);err.setGravity(Gravity.CENTER);active.addView(err,new FrameLayout.LayoutParams(-1,-1));});}});
 }
 private Bitmap decode(byte[] raw){BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(raw,0,raw.length,bounds);int s=1;while(Math.max(bounds.outWidth,bounds.outHeight)/s>1800)s*=2;BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=s;return BitmapFactory.decodeByteArray(raw,0,raw.length,o);}
 private void putImage(FrameLayout frame,Bitmap bitmap,boolean badge){frame.removeAllViews();if(bitmap==null)return;ImageView view=new ImageView(this);view.setImageBitmap(bitmap);view.setScaleType(ImageView.ScaleType.FIT_CENTER);view.setBackgroundColor(Color.WHITE);frame.addView(view,new FrameLayout.LayoutParams(-1,-1));if(badge){TextView seal=Ui.text(this,"ENCRYPTED",10,Color.WHITE);seal.setTypeface(null,android.graphics.Typeface.BOLD);seal.setGravity(Gravity.CENTER);seal.setBackground(Ui.bg(0xdd061f3b,Ui.dp(this,12)));FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(Ui.dp(this,86),Ui.dp(this,25),Gravity.TOP|Gravity.END);bp.setMargins(0,Ui.dp(this,12),Ui.dp(this,12),0);frame.addView(seal,bp);}}
 private void next(){if(!ready||items.size()<2){active.animate().translationY(Ui.dp(this,12)).setDuration(90).withEndAction(()->active.animate().translationY(0).setDuration(120).start()).start();return;}ready=false;active.animate().translationY(Ui.dp(this,420)).rotation(7).alpha(0).setDuration(235).withEndAction(()->{index=(index+1)%items.size();show(true);}).start();}
 private void open(){if(!ready)return;JSONObject d=items.get(index);Class<?> destination=DocumentGroups.CARDS.equals(group)?BankCardActivity.class:DocumentActivity.class;startActivity(new Intent(this,destination).putExtra("id",d.optString("id")));}
 private void empty(){active.removeAllViews();rearNear.removeAllViews();rearDeep.removeAllViews();TextView t=Ui.text(this,"No "+group+" Saved Yet\n\nReturn To My Vault And Scan Your First Item",17,Ui.MUTED);t.setGravity(Gravity.CENTER);active.addView(t,new FrameLayout.LayoutParams(-1,-1));counter.setText("0 Saved Items");itemTitle.setText("Your Stack Is Ready");side.setText("Encrypted On This Phone");}
 private void releaseImages(){for(Bitmap b:new Bitmap[]{faceImage,rearNearImage,rearDeepImage})if(b!=null&&!b.isRecycled())b.recycle();faceImage=rearNearImage=rearDeepImage=null;}
 @Override protected void onResume(){super.onResume();if(!Session.valid())finish();else if(!items.isEmpty())load();}
 @Override public boolean dispatchTouchEvent(MotionEvent e){if(Session.valid())Session.touch();return super.dispatchTouchEvent(e);}
 @Override protected void onDestroy(){super.onDestroy();io.shutdownNow();releaseImages();}
}
