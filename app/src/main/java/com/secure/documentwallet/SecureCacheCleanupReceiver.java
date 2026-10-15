package com.secure.documentwallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Removes cleartext previews after the short export window even if the app is no longer open. */
public final class SecureCacheCleanupReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){SecureCache.clearExpired(context,14L*60_000L);}
}
