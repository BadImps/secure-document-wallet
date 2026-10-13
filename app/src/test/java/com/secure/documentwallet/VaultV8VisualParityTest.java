package com.secure.documentwallet;

import android.app.*;import android.content.*;import android.graphics.*;import android.view.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.android.controller.ActivityController;import org.robolectric.annotation.*;import java.io.*;import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35,application=Application.class,qualifiers="mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class VaultV8VisualParityTest {
 private Application app;
 @Before public void unlock()throws Exception{app=RuntimeEnvironment.getApplication();ThemeManager.set(app,0);PinSecurity security=new PinSecurity(app);if(!security.configured())security.set("1234");Session.unlock("1234",security.vaultSalt());try{com.google.mlkit.common.MlKit.initialize(app);}catch(Exception ignored){}}
 @After public void lock(){Session.lock();}
 @Test public void rendersEveryAppOwnedV8Page()throws Exception{
  capture(MainActivity.class,"01-command-centre",null);
  capture(CategoryActivity.class,"02-id-permit",new Intent(app,CategoryActivity.class).putExtra("group",DocumentGroups.ID));
  capture(CategoryActivity.class,"03-certificates",new Intent(app,CategoryActivity.class).putExtra("group",DocumentGroups.CERTIFICATES));
  capture(CategoryActivity.class,"04-documents",new Intent(app,CategoryActivity.class).putExtra("group",DocumentGroups.DOCUMENTS));
  capture(PersonalInformationActivity.class,"05-personal-information",null);
  capture(PrivateMediaActivity.class,"06-private-media",null);
  capture(VaultSearchActivity.class,"07-search",null);
  capture(VaultSettingsActivity.class,"08-settings",null);
  capture(VaultHealthActivity.class,"09-security",null);
  capture(AccountDevicesActivity.class,"10-devices",null);
  capture(AppearanceActivity.class,"11-appearance",null);
  capture(AppUpdatesActivity.class,"12-updates",null);
  capture(ExpiryCentreActivity.class,"13-expiry",null);
  capture(SecureShareHubActivity.class,"14-share",null);
  capture(EmergencyAccessActivity.class,"15-emergency",null);
  capture(VaultActivityTimelineActivity.class,"16-activity",null);
 }
 private <T extends Activity> void capture(Class<T> type,String name,Intent intent)throws Exception{ActivityController<T> controller=intent==null?Robolectric.buildActivity(type):Robolectric.buildActivity(type,intent);T activity=controller.create().start().resume().get();View root=activity.getWindow().getDecorView();int w=390,h=844;root.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));root.layout(0,0,w,h);Bitmap image=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);root.draw(new Canvas(image));File out=new File("build/v8-parity/"+name+".png");out.getParentFile().mkdirs();try(FileOutputStream stream=new FileOutputStream(out)){image.compress(Bitmap.CompressFormat.PNG,100,stream);}assertTrue(name+" should render a substantial screen",out.length()>5000);controller.pause().stop().destroy();}
}
