package com.secure.documentwallet;

import android.content.*;import org.json.*;

final class VaultActivityLog {
 private static final String PREF="vault-activity",KEY="events";
 private VaultActivityLog(){}
 static synchronized void add(Context c,String title,String detail){try{JSONArray source=new JSONArray(c.getSharedPreferences(PREF,0).getString(KEY,"[]")),out=new JSONArray();out.put(new JSONObject().put("title",title).put("detail",detail).put("time",System.currentTimeMillis()));for(int i=0;i<source.length()&&i<39;i++)out.put(source.getJSONObject(i));c.getSharedPreferences(PREF,0).edit().putString(KEY,out.toString()).apply();}catch(Exception ignored){}}
 static JSONArray all(Context c){try{return new JSONArray(c.getSharedPreferences(PREF,0).getString(KEY,"[]"));}catch(Exception e){return new JSONArray();}}
}
