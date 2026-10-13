package com.secure.documentwallet;

import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Shared expiry ordering and urgency rules for the home and category pages. */
final class ExpiryUi {
 static final long DAY=86_400_000L,YEAR=365L*DAY;
 private ExpiryUi(){}
 static long when(JSONObject item){try{SimpleDateFormat f=new SimpleDateFormat("dd/MM/yyyy",Locale.US);f.setLenient(false);return f.parse(item.optString("expiryDate").trim()).getTime();}catch(Exception e){return Long.MAX_VALUE;}}
 static ArrayList<JSONObject> dated(JSONArray source,String group){ArrayList<JSONObject> result=new ArrayList<>();for(int i=0;i<source.length();i++){JSONObject item=source.optJSONObject(i);if(item==null||when(item)==Long.MAX_VALUE)continue;if(group==null||group.equals(DocumentGroups.fromCategory(item.optString("category"))))result.add(item);}result.sort(Comparator.comparingLong(ExpiryUi::when));return result;}
 static boolean withinYear(JSONObject item,long now){long expiry=when(item);return expiry!=Long.MAX_VALUE&&expiry-now<=YEAR;}
 static String dateLine(JSONObject item){long expiry=when(item),now=System.currentTimeMillis();return (expiry<now?"Expired ":"Expires ")+item.optString("expiryDate");}
}
