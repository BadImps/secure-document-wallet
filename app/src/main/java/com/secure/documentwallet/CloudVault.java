package com.secure.documentwallet;

import android.content.Context;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Minimal Supabase client. Vault backup is committed as an immutable, hash-verified snapshot. */
final class CloudVault {
 static final String BASE="https://dicabrqcyogwfizepkyp.supabase.co",KEY="sb_publishable_Y8PtxDf8SHaS6XBa3Q0lVA_AVZuNKZV";
 private static final String BUCKET="secure-document-wallet",LATEST="latest-snapshot.json",KIT="recovery-kit.json";
 final Context c;String access,userId;
 CloudVault(Context c){this.c=c.getApplicationContext();}

 void sendCode(String email)throws Exception{request("POST","/auth/v1/otp?redirect_to="+enc("https://badimps.github.io/secure-document-wallet/"),null,"application/json",new JSONObject().put("email",email).put("create_user",true).toString().getBytes(StandardCharsets.UTF_8));}
 void verify(String email,String credential)throws Exception{JSONObject body=new JSONObject();if(credential.startsWith("http://")||credential.startsWith("https://")){android.net.Uri link=android.net.Uri.parse(credential);String token=link.getQueryParameter("token");if(token==null)token=link.getQueryParameter("token_hash");if(token==null)throw new IllegalArgumentException("This verification link is incomplete");body.put("token_hash",token).put("type","magiclink");}else body.put("email",email).put("token",credential).put("type","email");JSONObject result=new JSONObject(new String(request("POST","/auth/v1/verify",null,"application/json",body.toString().getBytes(StandardCharsets.UTF_8)),StandardCharsets.UTF_8));access=result.getString("access_token");userId=result.getJSONObject("user").getString("id");c.getSharedPreferences("backup-account",0).edit().putString("email",email).apply();}
 String accountEmail(){return c.getSharedPreferences("backup-account",0).getString("email","");}

 /** Upload every object first, then publish one pointer only after the snapshot is complete. */
 int backup(byte[] salt)throws Exception{
  requireAuth();if(!RecoveryKit.ready(c))throw new IllegalStateException("Create and safely store a recovery code before cloud backup");
  String encoded=Base64.encodeToString(salt,Base64.NO_WRAP);JSONArray profile=new JSONArray().put(new JSONObject().put("user_id",userId).put("vault_salt",encoded).put("updated_at",java.time.Instant.now().toString()));
  request("POST","/rest/v1/secure_wallet_profiles?on_conflict=user_id","resolution=merge-duplicates,return=minimal","application/json",profile.toString().getBytes(StandardCharsets.UTF_8));
  File[] files=new CryptoStore(c).vaultDir().listFiles((d,n)->n.endsWith(".sdw"));if(files==null||files.length==0)throw new IOException("There is no encrypted vault data to back up");
  Arrays.sort(files,Comparator.comparing(File::getName));String id="snapshot-"+System.currentTimeMillis()+"-"+UUID.randomUUID().toString().substring(0,8),base=userId+"/snapshots/"+id;JSONArray objects=new JSONArray();
  for(File file:files){byte[] data=read(file);request("PUT",object(base+"/"+file.getName()),"","application/octet-stream",data);objects.put(new JSONObject().put("name",file.getName()).put("size",data.length).put("sha256",sha256(data)));}
  byte[] kit=RecoveryKit.bytes(c);request("PUT",object(base+"/"+KIT),"","application/json",kit);JSONObject manifest=new JSONObject().put("version",2).put("id",id).put("createdAt",java.time.Instant.now().toString()).put("objects",objects).put("recoveryKitSha256",sha256(kit));byte[] manifestBytes=manifest.toString().getBytes(StandardCharsets.UTF_8);
  request("PUT",object(base+"/manifest.json"),"","application/json",manifestBytes);
  // The pointer is the commit. A failure before it leaves the previous snapshot usable.
  JSONObject pointer=new JSONObject().put("version",2).put("id",id).put("manifest",base+"/manifest.json").put("manifestSha256",sha256(manifestBytes)).put("createdAt",manifest.getString("createdAt"));
  request("PUT",object(userId+"/"+LATEST),"","application/json",pointer.toString().getBytes(StandardCharsets.UTF_8));
  c.getSharedPreferences("vault-health",0).edit().putLong("last_backup",System.currentTimeMillis()).putInt("last_backup_files",objects.length()).putString("last_snapshot",id).apply();return objects.length();
 }

 /** Kept for compatible same-PIN restores; current restores use recovery-code snapshots. */
 int restore(byte[] localSalt)throws Exception{
  requireAuth();byte[] profile=request("GET","/rest/v1/secure_wallet_profiles?user_id=eq."+enc(userId)+"&select=vault_salt",null,null,null);JSONArray rows=new JSONArray(new String(profile,StandardCharsets.UTF_8));if(rows.length()==0)throw new IllegalStateException("No cloud vault is available for this account");byte[] cloudSalt=Base64.decode(rows.getJSONObject(0).getString("vault_salt"),Base64.NO_WRAP);if(!MessageDigest.isEqual(localSalt,cloudSalt))throw new SecurityException("This cloud vault uses a different PIN encryption key");Snapshot snapshot=latestSnapshot();if(snapshot==null)throw new IllegalStateException("This is a legacy backup. Restore it on a replacement phone with its recovery code.");File dir=new CryptoStore(c).vaultDir();for(int i=0;i<snapshot.objects.length();i++){JSONObject item=snapshot.objects.getJSONObject(i);write(new File(dir,item.getString("name")),verifiedObject(snapshot,item));}return snapshot.objects.length();
 }

 int restoreWithRecovery(String recoveryCode)throws Exception{
  requireAuth();File dir=new CryptoStore(c).vaultDir();File[] present=dir.listFiles((d,n)->n.endsWith(".sdw"));if(present!=null&&present.length>0)throw new IllegalStateException("For safety, cloud restore is available only when this phone's vault is empty");Snapshot snapshot=latestSnapshot();byte[] kit=snapshot==null?request("GET",object(userId+"/"+KIT),null,null,null):request("GET",object(snapshot.base+"/"+KIT),null,null,null);if(snapshot!=null&&!sha256(kit).equals(snapshot.recoveryKitSha256))throw new SecurityException("The cloud recovery package does not match the backup");
  byte[] prior=Session.copyKey(),master;try{master=RecoveryKit.unwrap(kit,recoveryCode);}catch(Exception error){throw new SecurityException("The recovery code is incorrect");}
  File stage=new File(c.getCacheDir(),"vault-restore");deleteTree(stage);if(!stage.mkdirs())throw new IOException("Could not prepare secure restore");
  try{int count;if(snapshot==null){count=downloadLegacy(stage);}else{for(int i=0;i<snapshot.objects.length();i++){JSONObject item=snapshot.objects.getJSONObject(i);write(new File(stage,item.getString("name")),verifiedObject(snapshot,item));}count=snapshot.objects.length();}
   if(count==0)throw new IOException("The cloud backup contains no vault files");VaultKeyManager.install(c,master);Session.use(master);for(File source:Objects.requireNonNull(stage.listFiles()))java.nio.file.Files.move(source.toPath(),new File(dir,source.getName()).toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);new VaultRepository(c).verifyAll();RecoveryKit.save(c,kit);c.getSharedPreferences("vault-health",0).edit().putLong("last_restore",System.currentTimeMillis()).apply();return count;
  }catch(Exception error){File[] created=dir.listFiles((d,n)->n.endsWith(".sdw"));if(created!=null)for(File file:created)file.delete();VaultKeyManager.install(c,prior);Session.use(prior);throw error;
  }finally{Arrays.fill(master,(byte)0);Arrays.fill(prior,(byte)0);deleteTree(stage);}
 }

 /** Verify the published snapshot, including every encrypted object's length and hash. */
 int verifyBackup()throws Exception{requireAuth();Snapshot snapshot=latestSnapshot();if(snapshot==null)throw new IOException("This older backup cannot be fully verified. Make a fresh backup first.");byte[] kit=request("GET",object(snapshot.base+"/"+KIT),null,null,null);if(!sha256(kit).equals(snapshot.recoveryKitSha256))throw new SecurityException("The cloud recovery package does not match the backup");for(int i=0;i<snapshot.objects.length();i++)verifiedObject(snapshot,snapshot.objects.getJSONObject(i));c.getSharedPreferences("vault-health",0).edit().putLong("last_verified",System.currentTimeMillis()).apply();return snapshot.objects.length();}

 private Snapshot latestSnapshot()throws Exception{byte[] raw;try{raw=request("GET",object(userId+"/"+LATEST),null,null,null);}catch(IOException missing){if(isMissing(missing))return null;throw missing;}JSONObject pointer=new JSONObject(new String(raw,StandardCharsets.UTF_8));String manifestPath=pointer.getString("manifest");byte[] manifestBytes=request("GET",object(manifestPath),null,null,null);if(!sha256(manifestBytes).equals(pointer.getString("manifestSha256")))throw new SecurityException("The cloud backup manifest could not be verified");JSONObject manifest=new JSONObject(new String(manifestBytes,StandardCharsets.UTF_8));if(manifest.optInt("version")!=2||manifest.optJSONArray("objects")==null)throw new IOException("Unsupported cloud backup format");return new Snapshot(manifestPath.substring(0,manifestPath.lastIndexOf('/')),manifest.getJSONArray("objects"),manifest.getString("recoveryKitSha256"));}
 private static boolean isMissing(IOException error){String message=error.getMessage();return message!=null&&(message.contains("404")||message.toLowerCase(Locale.US).contains("not found"));}
 private byte[] verifiedObject(Snapshot snapshot,JSONObject item)throws Exception{String name=item.getString("name");if(name.contains("/")||name.contains("\\"))throw new SecurityException("Invalid backup object name");byte[] data=request("GET",object(snapshot.base+"/"+name),null,null,null);if(data.length!=item.getLong("size")||!sha256(data).equals(item.getString("sha256")))throw new SecurityException("Cloud backup verification failed for "+name);return data;}
 private int downloadLegacy(File stage)throws Exception{JSONArray files=list(userId);int count=0;for(int i=0;i<files.length();i++){String name=files.getJSONObject(i).optString("name");if(name.isEmpty()||!name.endsWith(".sdw")||name.contains("/")||name.contains("\\"))continue;write(new File(stage,name),request("GET",object(userId+"/"+name),null,null,null));count++;}return count;}
 private JSONArray list(String prefix)throws Exception{JSONObject body=new JSONObject().put("prefix",prefix).put("limit",1000).put("offset",0).put("sortBy",new JSONObject().put("column","name").put("order","asc"));return new JSONArray(new String(request("POST","/storage/v1/object/list/"+BUCKET,null,"application/json",body.toString().getBytes(StandardCharsets.UTF_8)),StandardCharsets.UTF_8));}
 private static final class Snapshot{final String base,recoveryKitSha256;final JSONArray objects;Snapshot(String base,JSONArray objects,String recoveryKitSha256){this.base=base;this.objects=objects;this.recoveryKitSha256=recoveryKitSha256;}}
 private static void deleteTree(File file){if(file==null||!file.exists())return;if(file.isDirectory()){File[] children=file.listFiles();if(children!=null)for(File child:children)deleteTree(child);}file.delete();}
 private byte[] request(String method,String path,String prefer,String type,byte[] body)throws Exception{HttpURLConnection h=(HttpURLConnection)new URL(BASE+path).openConnection();h.setRequestMethod(method);h.setConnectTimeout(20000);h.setReadTimeout(60000);h.setRequestProperty("apikey",KEY);if(access!=null)h.setRequestProperty("Authorization","Bearer "+access);if(prefer!=null&&!prefer.isEmpty())h.setRequestProperty("Prefer",prefer);if("PUT".equals(method)&&path.startsWith("/storage/v1/object/"))h.setRequestProperty("x-upsert","true");if(type!=null)h.setRequestProperty("Content-Type",type);if(body!=null){h.setDoOutput(true);try(OutputStream out=h.getOutputStream()){out.write(body);}}int status=h.getResponseCode();InputStream stream=status>=200&&status<300?h.getInputStream():h.getErrorStream();byte[] response=stream==null?new byte[0]:read(stream);if(status<200||status>=300)throw new IOException(error(response,status));return response;}
 private String object(String name){return "/storage/v1/object/"+BUCKET+"/"+enc(name);}
 private String error(byte[] response,int status){try{JSONObject j=new JSONObject(new String(response,StandardCharsets.UTF_8));return j.optString("msg",j.optString("message","Cloud request failed ("+status+")"));}catch(Exception e){return "Cloud request failed ("+status+")";}}
 private void requireAuth(){if(access==null||userId==null)throw new SecurityException("Email verification required");}
 private static String enc(String s){try{return URLEncoder.encode(s,"UTF-8").replace("+","%20").replace("%2F","/");}catch(Exception e){throw new IllegalArgumentException(e);}}
 private static void write(File file,byte[] bytes)throws IOException{try(OutputStream out=new FileOutputStream(file)){out.write(bytes);}}
 private static byte[] read(File f)throws IOException{try(InputStream in=new FileInputStream(f)){return read(in);}}
 private static byte[] read(InputStream in)throws IOException{try(ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[64*1024];for(int n;(n=in.read(b))!=-1;)out.write(b,0,n);return out.toByteArray();}}
 private static String sha256(byte[] bytes)throws Exception{byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder out=new StringBuilder(64);for(byte b:digest)out.append(String.format(Locale.US,"%02x",b));return out.toString();}
}
