package com.secure.documentwallet;

import org.json.*;
import org.junit.Test;
import java.text.SimpleDateFormat;
import java.util.*;
import static org.junit.Assert.*;

public class ExpiryUiTest {
 @Test public void expiryItemsAreFilteredAndOrderedByNearestDate()throws Exception{JSONArray docs=new JSONArray().put(item("Later","31/12/2032",DocumentGroups.ID)).put(item("Nearest","02/01/2030",DocumentGroups.ID)).put(item("Other Group","01/01/2029",DocumentGroups.CARDS)).put(item("No Date","",DocumentGroups.ID));ArrayList<JSONObject> result=ExpiryUi.dated(docs,DocumentGroups.ID);assertEquals(2,result.size());assertEquals("Nearest",result.get(0).getString("title"));assertEquals("Later",result.get(1).getString("title"));}
 @Test public void datesWithinOneYearAreUrgent()throws Exception{SimpleDateFormat f=new SimpleDateFormat("dd/MM/yyyy",Locale.US);long now=f.parse("01/01/2030").getTime();assertTrue(ExpiryUi.withinYear(item("Soon","31/12/2030",DocumentGroups.DOCUMENTS),now));assertFalse(ExpiryUi.withinYear(item("Later","02/01/2031",DocumentGroups.DOCUMENTS),now));assertTrue(ExpiryUi.withinYear(item("Expired","31/12/2029",DocumentGroups.DOCUMENTS),now));}
 private JSONObject item(String title,String expiry,String category)throws Exception{return new JSONObject().put("title",title).put("expiryDate",expiry).put("category",category);}
}
