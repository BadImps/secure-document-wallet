package com.secure.documentwallet;

import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.FragmentActivity;
import org.json.JSONObject;
import java.io.*;
import java.util.concurrent.*;

/** Theme-matched editor with a movable four-corner crop frame. */
public class EditDocumentActivity extends FragmentActivity {
 private final ExecutorService io=Executors.newSingleThreadExecutor();
 private VaultRepository repo; private JSONObject doc; private CropView crop; private EditText name; private int pageIndex;
 @Override public void onCreate(Bundle b){super.onCreate(b);Ui.safeScreen(this);Ui.lightBars(this);if(!Session.valid()){finish();return;}repo=new VaultRepository(this);pageIndex=getIntent().getIntExtra("page",0);build();load();}
 private void build(){
  LinearLayout root=Ui.column(this);root.setBackgroundColor(0xff061f3b);root.setPadding(Ui.dp(this,16),Ui.dp(this,12),Ui.dp(this,16),Ui.dp(this,16));
  LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);TextView back=Ui.text(this,"‹",40,Color.WHITE);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());bar.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,54)));TextView title=Ui.title(this,"Edit Document",24);title.setTextColor(Color.WHITE);bar.addView(title,new LinearLayout.LayoutParams(0,-2,1));root.addView(bar);
  crop=new CropView();FrameLayout frame=new FrameLayout(this);frame.setBackground(Ui.bg(0xff0b304c,Ui.dp(this,18)));frame.setPadding(Ui.dp(this,7),Ui.dp(this,7),Ui.dp(this,7),Ui.dp(this,7));frame.addView(crop,new FrameLayout.LayoutParams(-1,-1));LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,0,1);fp.setMargins(0,Ui.dp(this,8),0,Ui.dp(this,12));root.addView(frame,fp);
  TextView label=Ui.text(this,"Document Name",13,0xffd8efea);label.setTypeface(null,android.graphics.Typeface.BOLD);root.addView(label);name=Ui.field(this,"Document Name",false);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,Ui.dp(this,54));np.topMargin=Ui.dp(this,5);root.addView(name,np);
  LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);TextView rotate=action("↻\nRotate",0xfff5f7f6,Ui.INK),reset=action("⌗\nRecrop",0xfff5f7f6,Ui.INK),save=action("✓\nSave Changes",Ui.TEAL,Color.WHITE);rotate.setOnClickListener(v->crop.rotate());reset.setOnClickListener(v->crop.resetCrop());save.setOnClickListener(v->save());for(TextView a:new TextView[]{rotate,reset,save}){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Ui.dp(this,68),a==save?1.35f:1);p.setMargins(Ui.dp(this,4),Ui.dp(this,13),Ui.dp(this,4),0);actions.addView(a,p);}root.addView(actions);setContentView(root);
 }
 private TextView action(String text,int background,int foreground){TextView v=Ui.text(this,text,13,foreground);v.setGravity(Gravity.CENTER);v.setTypeface(null,android.graphics.Typeface.BOLD);v.setBackground(Ui.bg(background,Ui.dp(this,15)));return v;}
 private void load(){String id=getIntent().getStringExtra("id");io.execute(()->{try{doc=repo.get(id);if(doc==null)throw new IOException();byte[] raw=repo.page(doc,pageIndex);BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=sample(raw);Bitmap bitmap=BitmapFactory.decodeByteArray(raw,0,raw.length,o);runOnUiThread(()->{name.setText(doc.optString("title","Document"));crop.setBitmap(bitmap);});}catch(Exception e){runOnUiThread(()->{Ui.toast(this,"Could Not Open This Page");finish();});}});}
 private int sample(byte[] raw){BitmapFactory.Options b=new BitmapFactory.Options();b.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(raw,0,raw.length,b);int s=1;while(Math.max(b.outWidth,b.outHeight)/s>2400)s*=2;return s;}
 private void save(){String title=name.getText().toString().trim();if(title.isEmpty()){name.setError("Enter a document name");return;}Bitmap edited=crop.cropped();if(edited==null){Ui.toast(this,"Adjust The Crop And Try Again");return;}ProgressBar wait=new ProgressBar(this);addContentView(wait,new ViewGroup.LayoutParams(Ui.dp(this,52),Ui.dp(this,52)));io.execute(()->{File file=null;try{File dir=new File(getCacheDir(),"edited_pages");if(!dir.exists())dir.mkdirs();file=new File(dir,"edit-"+System.currentTimeMillis()+".jpg");try(OutputStream out=new FileOutputStream(file)){edited.compress(Bitmap.CompressFormat.JPEG,96,out);}edited.recycle();repo.replacePage(doc.getString("id"),pageIndex,title,Uri.fromFile(file));File finished=file;runOnUiThread(()->{wait.setVisibility(View.GONE);finished.delete();Ui.toast(this,"Document Updated Securely");setResult(RESULT_OK);finish();});}catch(Exception e){if(file!=null)file.delete();runOnUiThread(()->{wait.setVisibility(View.GONE);Ui.toast(this,"Could Not Save These Changes");});}});}
 @Override protected void onDestroy(){super.onDestroy();io.shutdownNow();if(crop!=null)crop.release();}

 private final class CropView extends View {
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG),shade=new Paint(),line=new Paint(Paint.ANTI_ALIAS_FLAG);private Bitmap bitmap;private final RectF imageRect=new RectF(),cropRect=new RectF();private int handle=-1;private float lastX,lastY;
  CropView(){super(EditDocumentActivity.this);shade.setColor(0x99000000);line.setColor(0xff35d5c4);line.setStyle(Paint.Style.STROKE);line.setStrokeWidth(Ui.dp(EditDocumentActivity.this,2));setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  void setBitmap(Bitmap b){release();bitmap=b;resetCrop();}
  void resetCrop(){if(bitmap==null||getWidth()==0){invalidate();return;}fit();cropRect.set(imageRect);float dx=imageRect.width()*.055f,dy=imageRect.height()*.055f;cropRect.inset(dx,dy);invalidate();}
  void fit(){if(bitmap==null)return;float scale=Math.min((float)getWidth()/bitmap.getWidth(),(float)getHeight()/bitmap.getHeight());float w=bitmap.getWidth()*scale,h=bitmap.getHeight()*scale;imageRect.set((getWidth()-w)/2f,(getHeight()-h)/2f,(getWidth()+w)/2f,(getHeight()+h)/2f);}
  @Override protected void onSizeChanged(int w,int h,int ow,int oh){if(bitmap!=null)resetCrop();}
  @Override protected void onDraw(Canvas c){super.onDraw(c);if(bitmap==null){paint.setColor(0xffd8efea);paint.setTextSize(Ui.dp(EditDocumentActivity.this,16));c.drawText("Opening Encrypted Page…",Ui.dp(EditDocumentActivity.this,25),getHeight()/2f,paint);return;}fit();c.drawBitmap(bitmap,null,imageRect,paint);c.drawRect(imageRect.left,imageRect.top,imageRect.right,cropRect.top,shade);c.drawRect(imageRect.left,cropRect.bottom,imageRect.right,imageRect.bottom,shade);c.drawRect(imageRect.left,cropRect.top,cropRect.left,cropRect.bottom,shade);c.drawRect(cropRect.right,cropRect.top,imageRect.right,cropRect.bottom,shade);c.drawRect(cropRect,line);for(int i=1;i<3;i++){float x=cropRect.left+cropRect.width()*i/3f,y=cropRect.top+cropRect.height()*i/3f;c.drawLine(x,cropRect.top,x,cropRect.bottom,line);c.drawLine(cropRect.left,y,cropRect.right,y,line);}paint.setColor(Color.WHITE);paint.setStyle(Paint.Style.FILL);for(float[] h:handles())c.drawCircle(h[0],h[1],Ui.dp(EditDocumentActivity.this,8),paint);paint.setColor(0xff35d5c4);for(float[] h:handles())c.drawCircle(h[0],h[1],Ui.dp(EditDocumentActivity.this,5),paint);}
  private float[][] handles(){return new float[][]{{cropRect.left,cropRect.top},{cropRect.right,cropRect.top},{cropRect.right,cropRect.bottom},{cropRect.left,cropRect.bottom}};}
  @Override public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX(),y=e.getY();if(e.getActionMasked()==MotionEvent.ACTION_DOWN){handle=nearest(x,y);lastX=x;lastY=y;return true;}if(e.getActionMasked()==MotionEvent.ACTION_MOVE){float dx=x-lastX,dy=y-lastY;if(handle>=0)moveHandle(handle,dx,dy);else if(cropRect.contains(lastX,lastY)){cropRect.offset(dx,dy);clamp();}lastX=x;lastY=y;invalidate();return true;}if(e.getActionMasked()==MotionEvent.ACTION_UP){performClick();handle=-1;return true;}return true;}
  @Override public boolean performClick(){super.performClick();return true;}
  private int nearest(float x,float y){float max=Ui.dp(EditDocumentActivity.this,38),best=max*max;int n=-1,i=0;for(float[] h:handles()){float d=(x-h[0])*(x-h[0])+(y-h[1])*(y-h[1]);if(d<best){best=d;n=i;}i++;}return n;}
  private void moveHandle(int h,float dx,float dy){float min=Ui.dp(EditDocumentActivity.this,70);if(h==0||h==3)cropRect.left=Math.min(cropRect.left+dx,cropRect.right-min);else cropRect.right=Math.max(cropRect.right+dx,cropRect.left+min);if(h==0||h==1)cropRect.top=Math.min(cropRect.top+dy,cropRect.bottom-min);else cropRect.bottom=Math.max(cropRect.bottom+dy,cropRect.top+min);clamp();}
  private void clamp(){float dx=0,dy=0;if(cropRect.left<imageRect.left)dx=imageRect.left-cropRect.left;if(cropRect.right>imageRect.right)dx=imageRect.right-cropRect.right;if(cropRect.top<imageRect.top)dy=imageRect.top-cropRect.top;if(cropRect.bottom>imageRect.bottom)dy=imageRect.bottom-cropRect.bottom;cropRect.offset(dx,dy);}
  void rotate(){if(bitmap==null)return;Matrix m=new Matrix();m.postRotate(90);Bitmap r=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,true);bitmap.recycle();bitmap=r;resetCrop();}
  Bitmap cropped(){if(bitmap==null)return null;fit();int l=Math.max(0,Math.round((cropRect.left-imageRect.left)/imageRect.width()*bitmap.getWidth())),t=Math.max(0,Math.round((cropRect.top-imageRect.top)/imageRect.height()*bitmap.getHeight())),r=Math.min(bitmap.getWidth(),Math.round((cropRect.right-imageRect.left)/imageRect.width()*bitmap.getWidth())),b=Math.min(bitmap.getHeight(),Math.round((cropRect.bottom-imageRect.top)/imageRect.height()*bitmap.getHeight()));if(r-l<10||b-t<10)return null;return Bitmap.createBitmap(bitmap,l,t,r-l,b-t);}
  void release(){if(bitmap!=null&&!bitmap.isRecycled())bitmap.recycle();bitmap=null;}
 }
}
