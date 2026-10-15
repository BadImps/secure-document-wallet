package com.secure.documentwallet;
import android.app.*;import android.content.*;import android.os.Build;import java.io.File;
final class SecureCache {
 private static final String[] PRIVATE={"exports","imported_pages","edited_pages","normalized_scans","card_import","vault-restore"};
 static int clear(Context c){int total=0;for(String name:PRIVATE)total+=deleteChildren(new File(c.getCacheDir(),name),Long.MAX_VALUE);return total;}
 static int clearExpired(Context c,long ageMs){int total=0;long before=System.currentTimeMillis()-ageMs;for(String name:PRIVATE)total+=deleteChildren(new File(c.getCacheDir(),name),before);return total;}
 static int count(Context c){int total=0;for(String name:PRIVATE)total+=countFiles(new File(c.getCacheDir(),name));return total;}
 static void scheduleCleanup(Context c,long delayMs){AlarmManager alarms=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(alarms==null)return;Intent intent=new Intent(c,SecureCacheCleanupReceiver.class);PendingIntent pending=PendingIntent.getBroadcast(c,713,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);long when=System.currentTimeMillis()+Math.max(delayMs,60_000L);if(Build.VERSION.SDK_INT>=23)alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pending);else alarms.set(AlarmManager.RTC_WAKEUP,when,pending);}
 private static int deleteChildren(File file,long before){if(!file.exists())return 0;int count=0;if(file.isDirectory()){File[] children=file.listFiles();if(children!=null)for(File child:children)count+=deleteChildren(child,before);if(file.list()==null||file.list().length==0)file.delete();return count;}if(before==Long.MAX_VALUE||file.lastModified()<before){if(file.delete())return 1;}return 0;}
 private static int countFiles(File file){if(!file.exists())return 0;if(file.isFile())return 1;int count=0;File[] children=file.listFiles();if(children!=null)for(File child:children)count+=countFiles(child);return count;}
}
