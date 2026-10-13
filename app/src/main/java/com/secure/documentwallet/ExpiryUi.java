package com.secure.documentwallet;

import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Shared expiry ordering and urgency rules for the home and category pages. */
final class ExpiryUi {
 static final long DAY=86_400_000L,YEAR=365L*DAY;
 private ExpiryUi(){}
 static Date parse(String raw){String value=raw==null?"":raw.trim();for(String pattern:new String[]{"dd/MM/yyyy","MMMM/yyyy","MM/yyyy"})try{SimpleDateFormat f=new SimpleDateFormat(pattern,Locale.US);f.setLenient(false);Date parsed=f.parse(value);if(!pattern.startsWith("dd")){Calendar c=Calendar.getInstance();c.setTime(parsed);c.set(Calendar.DAY_OF_MONTH,c.getActualMaximum(Calendar.DAY_OF_MONTH));parsed=c.getTime();}return parsed;}catch(Exception ignored){}return null;}
 static long when(JSONObject item){Date parsed=parse(item.optString("expiryDate"));return parsed==null?Long.MAX_VALUE:parsed.getTime();}
 static ArrayList<JSONObject> dated(JSONArray source,String group){ArrayList<JSONObject> result=new ArrayList<>();for(int i=0;i<source.length();i++){JSONObject item=source.optJSONObject(i);if(item==null||when(item)==Long.MAX_VALUE)continue;if(group==null||group.equals(DocumentGroups.fromCategory(item.optString("category"))))result.add(item);}result.sort(Comparator.comparingLong(ExpiryUi::when));return result;}
 static boolean withinYear(JSONObject item,long now){long expiry=when(item);return expiry!=Long.MAX_VALUE&&expiry-now<=YEAR;}
 static String dateLine(JSONObject item){long expiry=when(item),now=System.currentTimeMillis();return (expiry<now?"Expired ":"Expires ")+item.optString("expiryDate");}
}
