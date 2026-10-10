package com.secure.documentwallet;

import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.FragmentActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankCardActivity extends FragmentActivity {
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final ArrayList<JSONObject> cards = new ArrayList<>();
    private VaultRepository repo;
    private JSONObject document;
    private Bitmap front, back;
    private FrameLayout card;
    private TextView title, sideLabel, counter;
    private int cardIndex;
    private boolean showingBack, ready;
    private float touchDownY;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); Ui.safeScreen(this); Ui.lightBars(this);
        if (!Session.valid()) { finish(); return; }
        repo = new VaultRepository(this); buildPage(); loadWallet(getIntent().getStringExtra("id"));
    }

    private void buildPage() {
        LinearLayout page = Ui.column(this); page.setBackgroundColor(0xfffbfaf6); page.setPadding(Ui.dp(this,18),Ui.dp(this,16),Ui.dp(this,18),Ui.dp(this,28));
        LinearLayout bar = new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView backButton=Ui.text(this,"‹",40,0xff061f3b); backButton.setGravity(Gravity.CENTER); backButton.setContentDescription("Back To Bank Cards"); backButton.setOnClickListener(v->finish()); bar.addView(backButton,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,54)));
        LinearLayout titles=Ui.column(this); title=Ui.title(this,"Bank Cards",25); title.setTextColor(0xff061f3b); titles.addView(title); counter=Ui.text(this,"Opening Encrypted Wallet…",13,0xff727a87); titles.addView(counter); bar.addView(titles,new LinearLayout.LayoutParams(0,-2,1)); page.addView(bar);
        TextView instruction=Ui.text(this,"Tap To Flip  ·  Flick Down For Next Card",14,0xff0e6f69); instruction.setTypeface(null,android.graphics.Typeface.BOLD); instruction.setGravity(Gravity.CENTER); instruction.setPadding(0,Ui.dp(this,21),0,Ui.dp(this,10)); page.addView(instruction);

        FrameLayout deck=new FrameLayout(this); deck.setClipChildren(false); deck.addView(stackCard(.05f,.92f,29),deckCardParams(18)); deck.addView(stackCard(.10f,.96f,15),deckCardParams(10));
        card=new FrameLayout(this); card.setBackground(Ui.bg(Color.WHITE,Ui.dp(this,24))); card.setElevation(Ui.dp(this,10)); card.setCameraDistance(Ui.dp(this,9000)); card.setOnClickListener(v->flip());
        card.setOnTouchListener((view,event)->{if(event.getActionMasked()==MotionEvent.ACTION_DOWN){touchDownY=event.getY();return true;}if(event.getActionMasked()==MotionEvent.ACTION_UP){float movement=event.getY()-touchDownY;if(movement>Ui.dp(this,70))nextCard();else view.performClick();return true;}return true;});
        ProgressBar loading=new ProgressBar(this); card.addView(loading,new FrameLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48),Gravity.CENTER)); deck.addView(card,deckCardParams(0)); page.addView(deck,new LinearLayout.LayoutParams(-1,Ui.dp(this,275)));
        sideLabel=Ui.text(this,"Loading Secure Card…",13,0xff727a87); sideLabel.setGravity(Gravity.CENTER); page.addView(sideLabel,new LinearLayout.LayoutParams(-1,Ui.dp(this,28)));
        TextView privacy=Ui.text(this,"The active card is decrypted only while this private screen is open. Cards behind it stay almost invisible until you flick to the next one.",13,Ui.MUTED); privacy.setGravity(Gravity.CENTER); privacy.setLineSpacing(0,1.12f); privacy.setPadding(Ui.dp(this,16),Ui.dp(this,17),Ui.dp(this,16),Ui.dp(this,22)); page.addView(privacy);
        TextView details=Ui.pill(this,"Open Full Document And Export Options",0xff087f78); details.setOnClickListener(v->{if(document!=null)startActivity(new Intent(this,DocumentActivity.class).putExtra("id",document.optString("id")));}); page.addView(details,new LinearLayout.LayoutParams(-1,Ui.dp(this,54))); setContentView(page);
    }

    private View stackCard(float alpha,float scale,int drop){View layer=new View(this);layer.setBackground(Ui.bg(0xff0e6f69,Ui.dp(this,24)));layer.setAlpha(alpha);layer.setScaleX(scale);layer.setTranslationY(Ui.dp(this,drop));layer.setElevation(Ui.dp(this,2));return layer;}
    private FrameLayout.LayoutParams deckCardParams(int top){FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(-1,Ui.dp(this,238),Gravity.TOP|Gravity.CENTER_HORIZONTAL);params.setMargins(Ui.dp(this,4),Ui.dp(this,top),Ui.dp(this,4),0);return params;}

    private void loadWallet(String requestedId){ready=false;io.execute(()->{try{JSONArray all=repo.all();cards.clear();int selected=0;for(int i=0;i<all.length();i++){JSONObject item=all.optJSONObject(i);if(item==null||!DocumentGroups.CARDS.equals(DocumentGroups.fromCategory(item.optString("category"))))continue;if(requestedId!=null&&requestedId.equals(item.optString("id")))selected=cards.size();cards.add(item);}if(cards.isEmpty())throw new IllegalStateException("No Bank Cards Are Available");cardIndex=selected;runOnUiThread(()->loadCurrent(false));}catch(Exception error){runOnUiThread(()->showError(error.getMessage()));}});}

    private void loadCurrent(boolean animateIn){if(cards.isEmpty())return;ready=false;showingBack=false;releaseImages();document=cards.get(cardIndex);title.setText(document.optString("title","Bank Card"));counter.setText("Card "+(cardIndex+1)+" Of "+cards.size());card.setAlpha(animateIn?0f:1f);card.setTranslationY(animateIn?-Ui.dp(this,42):0f);card.setRotation(animateIn?-5f:0f);ProgressBar loading=new ProgressBar(this);card.addView(loading,new FrameLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48),Gravity.CENTER));final int loadingIndex=cardIndex;final JSONObject loadingDocument=document;io.execute(()->{try{JSONArray pages=loadingDocument.optJSONArray("pages");if(pages==null||pages.length()==0)throw new IllegalStateException("No Card Image Was Saved");Bitmap loadedFront=decode(repo.page(loadingDocument,0));Bitmap loadedBack=pages.length()>1?decode(repo.page(loadingDocument,1)):null;runOnUiThread(()->{if(loadingIndex!=cardIndex||isFinishing()){loadedFront.recycle();if(loadedBack!=null)loadedBack.recycle();return;}front=loadedFront;back=loadedBack;ready=true;showSide(false);if(animateIn)card.animate().alpha(1f).translationY(0f).rotation(0f).setDuration(260).start();});}catch(Exception error){runOnUiThread(()->showError(error.getMessage()));}});}

    private Bitmap decode(byte[] data){BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,bounds);int sample=1;while(Math.max(bounds.outWidth,bounds.outHeight)/sample>1800)sample*=2;BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=sample;return BitmapFactory.decodeByteArray(data,0,data.length,options);}
    private void flip(){if(!ready||!card.isEnabled())return;card.setEnabled(false);card.animate().rotationY(90f).setDuration(170).withEndAction(()->{showingBack=!showingBack;showSide(showingBack);card.setRotationY(-90f);card.animate().rotationY(0f).setDuration(190).withEndAction(()->card.setEnabled(true)).start();}).start();}
    private void nextCard(){if(!ready||cards.size()<2||!card.isEnabled()){if(cards.size()==1)card.animate().translationY(Ui.dp(this,12)).setDuration(90).withEndAction(()->card.animate().translationY(0).setDuration(120).start()).start();return;}ready=false;card.setEnabled(false);card.animate().translationY(Ui.dp(this,245)).rotation(9f).alpha(0f).setDuration(230).withEndAction(()->{cardIndex=(cardIndex+1)%cards.size();card.setEnabled(true);loadCurrent(true);}).start();}

    private void showSide(boolean backSide){card.removeAllViews();Bitmap image=backSide?back:front;if(image!=null){ImageView view=new ImageView(this);view.setImageBitmap(image);view.setScaleType(ImageView.ScaleType.FIT_CENTER);view.setPadding(Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8));card.addView(view,new FrameLayout.LayoutParams(-1,-1));}else{LinearLayout missing=Ui.column(this);missing.setGravity(Gravity.CENTER);missing.setBackground(Ui.bg(0xff0e6f69,Ui.dp(this,24)));TextView stripe=new TextView(this);stripe.setBackgroundColor(0xff203f49);missing.addView(stripe,new LinearLayout.LayoutParams(-1,Ui.dp(this,46)));TextView message=Ui.text(this,"No Back Image Was Saved",17,Color.WHITE);message.setGravity(Gravity.CENTER);message.setTypeface(null,android.graphics.Typeface.BOLD);missing.addView(message,new LinearLayout.LayoutParams(-1,0,1));card.addView(missing,new FrameLayout.LayoutParams(-1,-1));}TextView badge=Ui.text(this,backSide?"BACK":"FRONT",11,Color.WHITE);badge.setGravity(Gravity.CENTER);badge.setTypeface(null,android.graphics.Typeface.BOLD);badge.setBackground(Ui.bg(0xff071f3a,Ui.dp(this,12)));FrameLayout.LayoutParams badgeParams=new FrameLayout.LayoutParams(Ui.dp(this,68),Ui.dp(this,26),Gravity.TOP|Gravity.END);badgeParams.setMargins(0,Ui.dp(this,13),Ui.dp(this,13),0);card.addView(badge,badgeParams);sideLabel.setText((backSide?"Back Of ":"Front Of ")+document.optString("title","Bank Card"));}
    private void showError(String message){ready=false;card.removeAllViews();TextView error=Ui.text(this,message==null?"Could Not Open This Card":message,15,0xffb3261e);error.setGravity(Gravity.CENTER);card.addView(error,new FrameLayout.LayoutParams(-1,-1));sideLabel.setText("Card Unavailable");}
    private void releaseImages(){card.removeAllViews();if(front!=null&&!front.isRecycled())front.recycle();if(back!=null&&!back.isRecycled())back.recycle();front=null;back=null;}
    @Override public boolean dispatchTouchEvent(MotionEvent event){if(Session.valid())Session.touch();return super.dispatchTouchEvent(event);}
    @Override protected void onResume(){super.onResume();if(!Session.valid())finish();}
    @Override protected void onDestroy(){super.onDestroy();io.shutdownNow();releaseImages();}
}
