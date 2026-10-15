package com.secure.documentwallet;

import android.os.Bundle;import android.view.*;import android.widget.*;import androidx.fragment.app.FragmentActivity;

public class AppUpdatesActivity extends FragmentActivity {
 @Override public void onCreate(Bundle b){super.onCreate(b);Ui.safeScreen(this);Ui.lightBars(this);if(!Session.valid()){finish();return;}LinearLayout page=VaultPageUi.page(this);page.addView(VaultPageUi.header(this,"App Updates","Check trusted release availability"));page.addView(VaultPageUi.notice(this,"✓","Update Checks Enabled","Downloads are hash-checked before Android opens its installer",0xffe4f6ef,0xff168365));page.addView(VaultPageUi.sectionHead(this,"Before Installing","Android protection"));VaultPageUi.addRow(page,VaultPageUi.iconRow(this,"check","App Signature Required","Android accepts an update only when it matches this app's signing identity"));TextView check=VaultPageUi.primary(this,"Check For Updates");check.setOnClickListener(v->UpdateManager.check(this,true));VaultPageUi.button(page,check);setContentView(VaultPageUi.screen(this,page,"profile"));}
}
