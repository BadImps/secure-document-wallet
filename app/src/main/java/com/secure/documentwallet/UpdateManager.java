package com.secure.documentwallet;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class UpdateManager {
    private static final String FEED = "https://badimps.github.io/secure-document-wallet/latest.json";
    private static final String APP_ID = "com.secure.documentwallet";
    private static final long AUTO_CHECK_INTERVAL = 6L * 60L * 60L * 1000L;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    private UpdateManager() {}

    static void check(MainActivity activity, boolean manual) {
        if (!manual) {
            long last = activity.getSharedPreferences("updates", Context.MODE_PRIVATE).getLong("lastCheck", 0L);
            if (System.currentTimeMillis() - last < AUTO_CHECK_INTERVAL) return;
        } else {
            Ui.toast(activity, "Checking For Updates…");
        }
        IO.execute(() -> {
            try {
                JSONObject feed = readFeed();
                if (!APP_ID.equals(feed.optString("applicationId"))) throw new IOException("The update feed is not for My Vault");
                activity.getSharedPreferences("updates", Context.MODE_PRIVATE).edit().putLong("lastCheck", System.currentTimeMillis()).apply();
                int available = feed.getInt("versionCode");
                activity.runOnUiThread(() -> {
                    if (activity.isFinishing()) return;
                    if (available > currentVersionCode(activity)) showUpdate(activity, feed);
                    else if (manual) new AlertDialog.Builder(activity).setTitle("My Vault Is Up To Date").setMessage("Version " + currentVersionName(activity) + " is the newest available version.").setPositiveButton("OK", null).show();
                });
            } catch (Exception error) {
                if (manual) activity.runOnUiThread(() -> Ui.toast(activity, "Could Not Check For Updates: " + safeMessage(error)));
            }
        });
    }

    private static JSONObject readFeed() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(FEED).openConnection();
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(12000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "My-Vault-Android");
        try {
            if (connection.getResponseCode() != 200) throw new IOException("Update service returned " + connection.getResponseCode());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (InputStream input = connection.getInputStream()) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
            }
            return new JSONObject(output.toString("UTF-8"));
        } finally {
            connection.disconnect();
        }
    }

    private static void showUpdate(MainActivity activity, JSONObject feed) {
        String version = feed.optString("versionName", "New Version");
        String notes = feed.optString("notes", "A new My Vault update is ready.");
        new AlertDialog.Builder(activity)
                .setTitle("My Vault " + version + " Is Available")
                .setMessage(notes)
                .setNegativeButton("Later", null)
                .setPositiveButton("Download Update", (dialog, which) -> prepareDownload(activity, feed))
                .show();
    }

    private static void prepareDownload(MainActivity activity, JSONObject feed) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && !activity.getPackageManager().canRequestPackageInstalls()) {
            new AlertDialog.Builder(activity)
                    .setTitle("Allow My Vault Updates")
                    .setMessage("Android needs permission to install updates downloaded by My Vault. Enable ‘Allow From This Source’, return here, then choose Check For Updates again.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Open Settings", (d, w) -> activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + activity.getPackageName()))))
                    .show();
            return;
        }
        try {
            String url = feed.getString("apkUrl");
            String expectedHash = feed.getString("sha256").toLowerCase(Locale.US);
            long expectedSize = feed.optLong("sizeBytes", -1L);
            File folder = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (folder == null) throw new IOException("Update storage is unavailable");
            File apk = new File(folder, "My-Vault-update.apk");
            if (apk.exists() && !apk.delete()) throw new IOException("Could not replace the previous update");
            DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                    .setTitle("My Vault Update")
                    .setDescription("Downloading version " + feed.optString("versionName"))
                    .setMimeType("application/vnd.android.package-archive")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationUri(Uri.fromFile(apk));
            long downloadId = manager.enqueue(request);
            DownloadProgress progress = new DownloadProgress(activity, manager, downloadId, expectedSize, feed.optString("versionName", "Update"));
            progress.show();
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override public void onReceive(Context context, Intent intent) {
                    if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) != downloadId) return;
                    try { activity.unregisterReceiver(this); } catch (Exception ignored) {}
                    progress.verifying();
                    verifyAndInstall(activity, manager, downloadId, apk, expectedHash, expectedSize, progress);
                }
            };
            ContextCompat.registerReceiver(activity, receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_NOT_EXPORTED);
        } catch (Exception error) {
            Ui.toast(activity, "Could Not Download Update: " + safeMessage(error));
        }
    }

    private static void verifyAndInstall(MainActivity activity, DownloadManager manager, long id, File apk, String expectedHash, long expectedSize, DownloadProgress progress) {
        IO.execute(() -> {
            try {
                try (Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id))) {
                    if (!cursor.moveToFirst() || cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) != DownloadManager.STATUS_SUCCESSFUL)
                        throw new IOException("The download did not complete");
                }
                if (!apk.isFile() || (expectedSize >= 0 && apk.length() != expectedSize)) throw new SecurityException("The update file size did not match");
                if (!sha256(apk).equalsIgnoreCase(expectedHash)) throw new SecurityException("The update could not be verified");
                activity.runOnUiThread(() -> { progress.close(); install(activity, apk); });
            } catch (Exception error) {
                if (apk.exists()) apk.delete();
                activity.runOnUiThread(() -> { progress.close(); new AlertDialog.Builder(activity).setTitle("Update Stopped").setMessage(safeMessage(error) + ". My Vault did not open the unverified file.").setPositiveButton("OK", null).show(); });
            }
        });
    }

    private static final class DownloadProgress {
        private final MainActivity activity;private final DownloadManager manager;private final long id,expectedSize;private final String version;private final Handler handler=new Handler(Looper.getMainLooper());private AlertDialog dialog;private ProgressBar bar;private TextView status;private boolean polling=true;
        DownloadProgress(MainActivity activity,DownloadManager manager,long id,long expectedSize,String version){this.activity=activity;this.manager=manager;this.id=id;this.expectedSize=expectedSize;this.version=version;}
        void show(){LinearLayout content=Ui.column(activity);content.setPadding(Ui.dp(activity,22),Ui.dp(activity,8),Ui.dp(activity,22),Ui.dp(activity,8));status=Ui.text(activity,"Starting Download…",14,Ui.INK);status.setGravity(Gravity.CENTER);content.addView(status);bar=new ProgressBar(activity,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(1000);bar.setProgress(0);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,Ui.dp(activity,18));bp.setMargins(0,Ui.dp(activity,18),0,Ui.dp(activity,10));content.addView(bar,bp);TextView note=Ui.text(activity,"You may hide this window. The verified installer will open when the download finishes.",12,Ui.MUTED);note.setGravity(Gravity.CENTER);content.addView(note);dialog=new AlertDialog.Builder(activity).setTitle("Downloading My Vault "+version).setView(content).setNegativeButton("Hide",null).create();dialog.setOnDismissListener(d->{polling=false;handler.removeCallbacksAndMessages(null);});dialog.show();handler.post(this::poll);}
        private void poll(){if(!polling||activity.isFinishing())return;try(Cursor cursor=manager.query(new DownloadManager.Query().setFilterById(id))){if(cursor.moveToFirst()){int state=cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));long downloaded=cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)),reported=cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)),total=reported>0?reported:expectedSize;if(state==DownloadManager.STATUS_RUNNING&&total>0){int value=(int)Math.min(1000,downloaded*1000L/total);bar.setIndeterminate(false);bar.setProgress(value);status.setText((value/10)+"%  ·  "+size(downloaded)+" Of "+size(total));}else if(state==DownloadManager.STATUS_PAUSED){bar.setIndeterminate(true);status.setText("Download Paused · Waiting For Network");}else if(state==DownloadManager.STATUS_PENDING){bar.setIndeterminate(true);status.setText("Preparing Secure Download…");}else if(state==DownloadManager.STATUS_SUCCESSFUL){bar.setIndeterminate(true);status.setText("Download Complete · Verifying…");}else if(state==DownloadManager.STATUS_FAILED){bar.setIndeterminate(false);status.setText("Download Could Not Be Completed");}}}catch(Exception ignored){}if(polling)handler.postDelayed(this::poll,350);}
        void verifying(){handler.post(()->{if(dialog!=null&&dialog.isShowing()){bar.setIndeterminate(true);status.setText("Download Complete · Verifying Signature…");}});}
        void close(){polling=false;handler.removeCallbacksAndMessages(null);if(dialog!=null&&dialog.isShowing())dialog.dismiss();}
        private static String size(long bytes){if(bytes<0)return "Unknown";double mb=bytes/(1024d*1024d);return String.format(Locale.US,"%.1f MB",mb);}
    }

    private static String sha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count);
        }
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) result.append(String.format(Locale.US, "%02x", b));
        return result.toString();
    }

    private static void install(MainActivity activity, File apk) {
        try {
            Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".files", apk);
            Intent install = new Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(install);
        } catch (Exception error) {
            Ui.toast(activity, "Could Not Open The Android Installer: " + safeMessage(error));
        }
    }

    private static String safeMessage(Throwable error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty() ? "Unknown Error" : message;
    }

    @SuppressWarnings("deprecation")
    private static int currentVersionCode(Context context) {
        try { return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode; }
        catch (Exception ignored) { return 0; }
    }

    @SuppressWarnings("deprecation")
    private static String currentVersionName(Context context) {
        try { return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName; }
        catch (Exception ignored) { return "Unknown"; }
    }
}
