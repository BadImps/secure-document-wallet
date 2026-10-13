package com.secure.documentwallet;

import android.Manifest;import android.app.*;import android.content.*;import android.content.pm.PackageManager;import android.os.Build;import androidx.core.app.*;import org.json.*;import java.text.*;import java.util.*;

final class ExpiryScheduler {
 static final String CHANNEL="vault_expiry";private static final int[] LEADS={90,30,7,1,0};
 static void scheduleAll(Context c,JSONArray docs){channel(c);for(int i=0;i<docs.length();i++)schedule(c,docs.optJSONObject(i));}
 static void schedule(Context c,JSONObject doc){if(doc==null)return;String raw=doc.optString("expiryDate").trim();if(raw.isEmpty())return;try{Date expiry=ExpiryUi.parse(raw);if(expiry==null)return;Calendar now=Calendar.getInstance();for(int lead:LEADS){Calendar at=Calendar.getInstance();at.setTime(expiry);at.add(Calendar.DAY_OF_YEAR,-lead);at.set(Calendar.HOUR_OF_DAY,9);at.set(Calendar.MINUTE,0);at.set(Calendar.SECOND,0);if(at.after(now)){Intent intent=new Intent(c,ExpiryReceiver.class).putExtra("id",doc.optString("id")).putExtra("title",doc.optString("title","Document")).putExtra("days",lead);PendingIntent pending=PendingIntent.getBroadcast(c,request(doc.optString("id"),lead),intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at.getTimeInMillis(),pending);}}}catch(Exception ignored){}}
 static void channel(Context c){if(Build.VERSION.SDK_INT>=26)((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(new NotificationChannel(CHANNEL,"Document Expiry Reminders",NotificationManager.IMPORTANCE_HIGH));}
 private static int request(String id,int lead){return 31*id.hashCode()+lead;}
}
