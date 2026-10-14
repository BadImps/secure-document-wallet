package com.secure.documentwallet;

import android.os.Bundle;import android.view.*;import android.widget.*;import androidx.fragment.app.FragmentActivity;

public class AppUpdatesActivity extends FragmentActivity {
 @Override public void onCreate(Bundle b){super.onCreate(b);Ui.safeScreen(this);Ui.lightBars(this);if(!Session.valid()){finish();return;}LinearLayout page=VaultPageUi.page(this);page.addView(VaultPageUi.header(this,"App Updates","My Vault 3.11"));page.addView(VaultPageUi.notice(this,"✓","Your App Is Up To Date","Secure GitHub release feed is enabled",0xffe4f6ef,0xff168365));page.addView(VaultPageUi.sectionHead(this,"Current Release","Version 3.11"));VaultPageUi.addRow(page,VaultPageUi.iconRow(this,"check","Whole-Card Expiry Detection","Installed · Signed release"));page.addView(VaultPageUi.toggleRow(this,"↓","Download In App","Show progress in a protected popup",true,null));TextView check=VaultPageUi.primary(this,"Check For Updates");check.setOnClickListener(v->UpdateManager.check(this,true));VaultPageUi.button(page,check);setContentView(VaultPageUi.screen(this,page,"profile"));}
}
