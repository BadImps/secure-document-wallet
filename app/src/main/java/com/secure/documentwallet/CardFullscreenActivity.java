package com.secure.documentwallet;

import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.FragmentActivity;
import org.json.*;
import java.util.concurrent.*;

/** Secure landscape viewer opened by holding an ID or bank card. */
public class CardFullscreenActivity extends FragmentActivity {
 private final ExecutorService io=Executors.newSingleThreadExecutor();
 private VaultRepository repo; private Bitmap image; private FrameLayout card; private TextView message;

 @Override public void onCreate(Bundle state){super.onCreate(state);if(!Session.valid()){finish();return;}getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);repo=new VaultRepository(this);build();load();}

 private void build(){FrameLayout root=new FrameLayout(this);root.setBackgroundColor(0xff031426);LinearLayout column=Ui.column(this);column.setGravity(Gravity.CENTER);column.setPadding(Ui.dp(this,28),Ui.dp(this,18),Ui.dp(this,28),Ui.dp(this,18));String heading=getIntent().getStringExtra("title");TextView title=Ui.title(this,heading==null?"Secure Card":heading,20);title.setTextColor(Color.WHITE);title.setGravity(Gravity.CENTER);column.addView(title,new LinearLayout.LayoutParams(-1,-2));card=new FrameLayout(this);card.setBackground(Ui.stroke(0xff071f34,Ui.dp(this,1),0xff5ac6b7,Ui.dp(this,22)));card.setClipToOutline(true);card.setElevation(Ui.dp(this,14));ProgressBar loading=new ProgressBar(this);card.addView(loading,new FrameLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48),Gravity.CENTER));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,0,1);cp.setMargins(Ui.dp(this,34),Ui.dp(this,14),Ui.dp(this,34),Ui.dp(this,10));column.addView(card,cp);message=Ui.text(this,"Tap The Card Or Press Back To Return",13,0xffb8d8cf);message.setGravity(Gravity.CENTER);column.addView(message,new LinearLayout.LayoutParams(-1,-2));root.addView(column,new FrameLayout.LayoutParams(-1,-1));root.setOnClickListener(v->finish());card.setOnClickListener(v->finish());setContentView(root);}

 private void load(){String id=getIntent().getStringExtra("id");int page=Math.max(0,getIntent().getIntExtra("page",0));io.execute(()->{try{JSONArray all=repo.all();JSONObject found=null;for(int i=0;i<all.length();i++){JSONObject item=all.optJSONObject(i);if(item!=null&&id.equals(item.optString("id"))){found=item;break;}}if(found==null)throw new IllegalStateException("Card Not Found");JSONArray pages=found.optJSONArray("pages");if(pages==null||pages.length()==0)throw new IllegalStateException("No Card Image Was Saved");int selected=Math.min(page,pages.length()-1);byte[] raw=repo.page(found,selected);BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(raw,0,raw.length,bounds);int sample=1;while(Math.max(bounds.outWidth,bounds.outHeight)/sample>2600)sample*=2;BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=sample;Bitmap decoded=BitmapFactory.decodeByteArray(raw,0,raw.length,options);runOnUiThread(()->show(decoded));}catch(Exception error){runOnUiThread(()->showError(error.getMessage()));}});}

 private void show(Bitmap decoded){if(isFinishing()){if(decoded!=null)decoded.recycle();return;}image=decoded;card.removeAllViews();ImageView view=new ImageView(this);view.setImageBitmap(image);view.setScaleType(ImageView.ScaleType.FIT_CENTER);view.setBackgroundColor(0xff031426);card.addView(view,new FrameLayout.LayoutParams(-1,-1));card.setAlpha(0f);card.setScaleX(.86f);card.setScaleY(.86f);card.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(220).start();}
 private void showError(String detail){card.removeAllViews();TextView error=Ui.text(this,detail==null?"Could Not Open This Card":detail,16,0xffffb4ab);error.setGravity(Gravity.CENTER);card.addView(error,new FrameLayout.LayoutParams(-1,-1));message.setText("Press Back To Return");}
 @Override public boolean dispatchTouchEvent(android.view.MotionEvent event){if(Session.valid())Session.touch();return super.dispatchTouchEvent(event);}
 @Override protected void onDestroy(){super.onDestroy();io.shutdownNow();if(image!=null&&!image.isRecycled())image.recycle();}
}
