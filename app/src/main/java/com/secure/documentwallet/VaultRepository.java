package com.secure.documentwallet;

import android.content.*;import android.net.Uri;import org.json.*;import java.io.*;import java.util.*;

final class VaultRepository {
 static final String INDEX="index";private final Context c;private final CryptoStore crypto;
 VaultRepository(Context c){this.c=c.getApplicationContext();crypto=new CryptoStore(c);}
 synchronized JSONArray all()throws Exception{File f=new File(crypto.vaultDir(),INDEX+".sdw");return f.exists()?new JSONArray(new String(crypto.decrypt(f),java.nio.charset.StandardCharsets.UTF_8)):new JSONArray();}
 synchronized JSONObject get(String id)throws Exception{JSONArray a=all();for(int i=0;i<a.length();i++)if(id.equals(a.getJSONObject(i).optString("id")))return a.getJSONObject(i);return null;}
 synchronized JSONObject add(String title,String category,List<Uri> pages)throws Exception{String id=UUID.randomUUID().toString();JSONArray stored=new JSONArray();try{for(int i=0;i<pages.size();i++){String name=id+"-page-"+(i+1);try(InputStream in=c.getContentResolver().openInputStream(pages.get(i))){if(in==null)throw new IOException("Cannot read scanned page");crypto.encrypt(in,name);}stored.put(name);}}catch(Exception e){for(int i=0;i<stored.length();i++)crypto.delete(stored.optString(i));throw e;}JSONObject d=new JSONObject().put("id",id).put("title",title).put("category",category).put("createdAt",System.currentTimeMillis()).put("pages",stored);JSONArray a=all();a.put(d);save(a);return d;}
 synchronized void rename(String id,String title,String category)throws Exception{JSONArray a=all();for(int i=0;i<a.length();i++){JSONObject d=a.getJSONObject(i);if(id.equals(d.optString("id"))){d.put("title",title);d.put("category",category);break;}}save(a);}
 synchronized void delete(String id)throws Exception{JSONArray source=all(),out=new JSONArray();for(int i=0;i<source.length();i++){JSONObject d=source.getJSONObject(i);if(id.equals(d.optString("id"))){JSONArray pages=d.optJSONArray("pages");for(int j=0;pages!=null&&j<pages.length();j++)crypto.delete(pages.optString(j));}else out.put(d);}save(out);}
 byte[] page(JSONObject d,int index)throws Exception{return crypto.decrypt(new File(crypto.vaultDir(),d.getJSONArray("pages").getString(index)+".sdw"));}
 private void save(JSONArray a)throws Exception{crypto.encryptBytes(a.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8),INDEX);}
}
