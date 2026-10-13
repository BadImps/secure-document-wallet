package com.secure.documentwallet;

import android.app.*;import android.view.*;import android.widget.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35,application=Application.class)
public class VaultV8PagesTest {
 @Before public void unlock()throws Exception{Application app=RuntimeEnvironment.getApplication();ThemeManager.set(app,0);PinSecurity security=new PinSecurity(app);if(!security.configured())security.set("1234");Session.unlock("1234",security.vaultSalt());}
 @After public void lock(){Session.lock();}
 @Test public void settingsAndNewV8PagesOpenWithoutCrashing(){page(VaultSettingsActivity.class,"My Vault Settings");page(AppearanceActivity.class,"Appearance");page(AppUpdatesActivity.class,"App Updates");page(AccountDevicesActivity.class,"Account And Devices");page(ExpiryCentreActivity.class,"Expiry Centre");page(SecureShareHubActivity.class,"Secure Share Hub");page(EmergencyAccessActivity.class,"Emergency Access Kit");page(VaultActivityTimelineActivity.class,"Vault Activity");page(VaultHealthActivity.class,"Security And Backup");page(VaultSearchActivity.class,"Search Results");}
 private <T extends Activity> void page(Class<T> type,String title){var controller=Robolectric.buildActivity(type).create().start().resume();View root=controller.get().getWindow().getDecorView();assertTrue(type.getSimpleName()+" should show "+title,hasText(root,title));controller.pause().stop().destroy();}
 private boolean hasText(View view,String expected){if(view instanceof TextView&&expected.contentEquals(((TextView)view).getText()))return true;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)if(hasText(group.getChildAt(i),expected))return true;}return false;}
}
