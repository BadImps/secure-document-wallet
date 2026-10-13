package com.secure.documentwallet;

import android.os.Bundle;import android.view.*;import android.widget.*;import androidx.fragment.app.FragmentActivity;

public class AppUpdatesActivity extends FragmentActivity {
 @Override public void onCreate(Bundle b){super.onCreate(b);Ui.safeScreen(this);Ui.lightBars(this);if(!Session.valid()){finish();return;}LinearLayout page=VaultPageUi.page(this);page.addView(VaultPageUi.header(this,"App Updates","My Vault 3.2"));page.addView(VaultPageUi.notice(this,"✓","Automatic Update Checks","Secure GitHub release feed is enabled",Ui.SOFT,Ui.TEAL));page.addView(VaultPageUi.section(this,"Current Release"));VaultPageUi.addRow(page,VaultPageUi.row(this,"▣","V8 Search Command Centre","Installed design system · Signed build"));page.addView(VaultPageUi.toggleRow(this,"↓","Download In App","Show download and verification progress",true,null));TextView check=VaultPageUi.primary(this,"Check For Updates");check.setOnClickListener(v->UpdateManager.check(this,true));VaultPageUi.button(page,check);setContentView(VaultPageUi.scroll(this,page));}
}
