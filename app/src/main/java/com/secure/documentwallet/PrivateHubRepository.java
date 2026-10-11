package com.secure.documentwallet;

import android.content.*;import android.net.Uri;import org.json.*;import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;

/** Encrypted storage for private text and media that stays outside Android media collections. */
final class PrivateHubRepository {
 private static final String INDEX="private-hub-index";private final Context context;private final CryptoStore crypto;
 PrivateHubRepository(Context context){this.context=context.getApplicationContext();crypto=new CryptoStore(context);}
 synchronized JSONArray info()throws Exception{return cloneArray(index().optJSONArray("info"));}
 synchronized JSONArray media()throws Exception{return cloneArray(index().optJSONArray("media"));}
 synchronized JSONObject addInfo(String title,String body,String source)throws Exception{JSONObject root=index(),entry=new JSONObject().put("id",UUID.randomUUID().toString()).put("title",title).put("body",body).put("source",source).put("createdAt",System.currentTimeMillis());root.getJSONArray("info").put(entry);save(root);return entry;}
 synchronized void updateInfo(String id,String title,String body)throws Exception{JSONObject root=index();JSONArray list=root.getJSONArray("info");for(int i=0;i<list.length();i++){JSONObject item=list.getJSONObject(i);if(id.equals(item.optString("id"))){item.put("title",title).put("body",body).put("updatedAt",System.currentTimeMillis());break;}}save(root);}
 synchronized void deleteInfo(String id)throws Exception{JSONObject root=index();root.put("info",without(root.getJSONArray("info"),id));save(root);}
 synchronized JSONObject addMedia(String title,String mime,Uri uri)throws Exception{String id=UUID.randomUUID().toString(),object=id+"-media";try(InputStream input=context.getContentResolver().openInputStream(uri)){if(input==null)throw new IOException("Could Not Read This Media");crypto.encrypt(input,object);}try{JSONObject root=index(),entry=new JSONObject().put("id",id).put("title",title).put("mime",mime).put("object",object).put("createdAt",System.currentTimeMillis());root.getJSONArray("media").put(entry);save(root);return entry;}catch(Exception error){crypto.delete(object);throw error;}}
 synchronized void deleteMedia(String id)throws Exception{JSONObject root=index();JSONArray list=root.getJSONArray("media");for(int i=0;i<list.length();i++){JSONObject item=list.getJSONObject(i);if(id.equals(item.optString("id")))crypto.delete(item.optString("object"));}root.put("media",without(list,id));save(root);}
 byte[] mediaBytes(JSONObject item)throws Exception{return crypto.decrypt(new File(crypto.vaultDir(),item.getString("object")+".sdw"));}
 File openMedia(JSONObject item,File directory)throws Exception{if(!directory.exists()&&!directory.mkdirs())throw new IOException("Could Not Prepare Secure Preview");new File(directory,".nomedia").createNewFile();String extension=extension(item.optString("mime"));String safe=item.optString("title","Private Media").replaceAll("[^A-Za-z0-9._-]","_");File output=new File(directory,safe+"-"+item.optString("id")+extension);try(OutputStream out=new BufferedOutputStream(new FileOutputStream(output))){crypto.decryptTo(new File(crypto.vaultDir(),item.getString("object")+".sdw"),out);}return output;}
 private JSONObject index()throws Exception{File file=new File(crypto.vaultDir(),INDEX+".sdw");if(!file.exists())return new JSONObject().put("info",new JSONArray()).put("media",new JSONArray());JSONObject root=new JSONObject(new String(crypto.decrypt(file),StandardCharsets.UTF_8));if(root.optJSONArray("info")==null)root.put("info",new JSONArray());if(root.optJSONArray("media")==null)root.put("media",new JSONArray());return root;}
 private void save(JSONObject root)throws Exception{crypto.encryptBytes(root.toString().getBytes(StandardCharsets.UTF_8),INDEX);}
 private JSONArray without(JSONArray source,String id)throws Exception{JSONArray out=new JSONArray();for(int i=0;i<source.length();i++){JSONObject item=source.getJSONObject(i);if(!id.equals(item.optString("id")))out.put(item);}return out;}
 private JSONArray cloneArray(JSONArray source)throws JSONException{return source==null?new JSONArray():new JSONArray(source.toString());}
 private String extension(String mime){if(mime.contains("png"))return ".png";if(mime.contains("jpeg")||mime.contains("jpg"))return ".jpg";if(mime.contains("mp4"))return ".mp4";if(mime.contains("webm"))return ".webm";if(mime.contains("mpeg"))return mime.startsWith("audio")?".mp3":".mpeg";if(mime.contains("wav"))return ".wav";if(mime.contains("ogg"))return ".ogg";return ".bin";}
}
