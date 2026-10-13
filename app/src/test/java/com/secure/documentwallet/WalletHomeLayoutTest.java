package com.secure.documentwallet;

import android.app.Application;
import android.graphics.*;
import android.view.*;
import android.widget.TextView;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.util.ReflectionHelpers;
import java.io.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,application=Application.class,qualifiers="mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class WalletHomeLayoutTest {
 @Test public void homeShowsSixCategoryTilesWithoutRecentDocuments()throws Exception{
  Application app=RuntimeEnvironment.getApplication();ThemeManager.set(app,0);PinSecurity security=new PinSecurity(app);if(!security.configured())security.set("1234");Session.unlock("1234",security.vaultSalt());
  var controller=Robolectric.buildActivity(MainActivity.class).create().start().resume();MainActivity activity=controller.get();ReflectionHelpers.callInstanceMethod(activity,"home");View root=activity.getWindow().getDecorView();
  int w=390,h=844;root.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));root.layout(0,0,w,h);
  assertTrue(hasText(root,"ID / Permit"));assertTrue(hasText(root,"Certificates"));assertTrue(hasText(root,"Bank Cards"));assertTrue(hasText(root,"Documents"));assertTrue(hasText(root,"Personal Information"));assertTrue(hasText(root,"Media"));assertFalse(hasText(root,"Recent Documents"));
  Bitmap image=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);root.draw(new Canvas(image));File out=new File("build/wallet-home-1.9.png");out.getParentFile().mkdirs();try(FileOutputStream stream=new FileOutputStream(out)){image.compress(Bitmap.CompressFormat.PNG,100,stream);}assertTrue(out.length()>10000);
  controller.pause().stop().destroy();Session.lock();
 }
 @Test public void allThreeAppearanceThemesRenderDistinctHomeScreens()throws Exception{
  Application app=RuntimeEnvironment.getApplication();PinSecurity security=new PinSecurity(app);if(!security.configured())security.set("1234");int[] backgrounds=new int[3];
  for(int theme=0;theme<3;theme++){ThemeManager.set(app,theme);backgrounds[theme]=Ui.BG;Session.unlock("1234",security.vaultSalt());var controller=Robolectric.buildActivity(MainActivity.class).create().start().resume();MainActivity activity=controller.get();ReflectionHelpers.callInstanceMethod(activity,"home");View root=activity.getWindow().getDecorView();int w=390,h=844;root.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));root.layout(0,0,w,h);Bitmap image=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);root.draw(new Canvas(image));File out=new File("build/wallet-theme-"+theme+".png");out.getParentFile().mkdirs();try(FileOutputStream stream=new FileOutputStream(out)){image.compress(Bitmap.CompressFormat.PNG,100,stream);}assertTrue(out.length()>10000);controller.pause().stop().destroy();Session.lock();}
  assertNotEquals(backgrounds[0],backgrounds[1]);assertNotEquals(backgrounds[1],backgrounds[2]);assertNotEquals(backgrounds[0],backgrounds[2]);ThemeManager.set(app,0);
 }
 @Test public void personalInformationRowsShowOnlyTheirTitle()throws Exception{
  Application app=RuntimeEnvironment.getApplication();PinSecurity security=new PinSecurity(app);if(!security.configured())security.set("1234");Session.unlock("1234",security.vaultSalt());
  try{com.google.mlkit.common.MlKit.initialize(app);}catch(IllegalStateException alreadyInitialized){}
  var controller=Robolectric.buildActivity(PersonalInformationActivity.class).create().start().resume();PersonalInformationActivity activity=controller.get();org.json.JSONObject item=new org.json.JSONObject().put("id","one").put("title","Insurance Number").put("body","SECRET-DETAIL-123").put("createdAt",System.currentTimeMillis());View row=ReflectionHelpers.callInstanceMethod(activity,"bubble",ReflectionHelpers.ClassParameter.from(org.json.JSONObject.class,item));
  assertTrue(hasText(row,"Insurance Number"));assertFalse(hasText(row,"SECRET-DETAIL-123"));controller.pause().stop().destroy();Session.lock();
 }
 private boolean hasText(View view,String expected){if(view instanceof TextView&&expected.contentEquals(((TextView)view).getText()))return true;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)if(hasText(group.getChildAt(i),expected))return true;}return false;}
}
