package com.secure.documentwallet;

import org.json.JSONObject;
import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.regex.*;

/** Extracts only explicitly labelled fields from on-device OCR text. */
final class DocumentDetailParser {
 private DocumentDetailParser(){}
 static JSONObject identity(String text,boolean front){JSONObject out=new JSONObject();try{out.put("issuer",front?"Driver's Permit":"National Identification Card");out.put("issueDate",date(text,front?"ISSUE\\s*DATE":"DATE\\s*OF\\s*ISSUE"));out.put("expiryDate",date(text,"EXPIR(?:Y|ATION)\\s*DATE|DATE\\s*OF\\s*EXPIRY|EXPIRES"));out.put("documentNumber",digitsAfter(text,front?"LICEN[CS]E":"REGISTRATION",front?"(?:#|NO\\.?|NUMBER)?":"(?:NO\\.?|NUMBER|#)?"));}catch(Exception ignored){}return out;}
 static JSONObject bank(String text,String issuer){JSONObject out=new JSONObject();try{out.put("issuer",issuer==null?"":issuer.trim());Matcher m=Pattern.compile("(?i)(?:GOOD|VALID)\\s*THRU\\s*[:#-]?\\s*(0?[1-9]|1[0-2])\\s*[/.-]\\s*(\\d{2}|\\d{4})").matcher(flat(text));String expiry="";if(m.find()){int month=Integer.parseInt(m.group(1)),year=Integer.parseInt(m.group(2));if(year<100)year+=year<50?2000:1900;expiry=new DateFormatSymbols(Locale.US).getMonths()[month-1]+"/"+year;}out.put("expiryDate",expiry);}catch(Exception ignored){}return out;}
 private static String date(String text,String labels){Matcher m=Pattern.compile("(?i)(?:"+labels+")\\s*[:#-]?\\s*(\\d{1,2}\\s*[/.-]\\s*\\d{1,2}\\s*[/.-]\\s*\\d{2,4})").matcher(flat(text));if(!m.find())return "";String[] values=m.group(1).replaceAll("\\s","").split("[/.-]");if(values.length!=3)return "";int day=Integer.parseInt(values[0]),month=Integer.parseInt(values[1]),year=Integer.parseInt(values[2]);if(year<100)year+=year<50?2000:1900;if(day<1||day>31||month<1||month>12)return "";return String.format(Locale.US,"%02d/%02d/%04d",day,month,year);}
 private static String digitsAfter(String text,String label,String suffix){Matcher m=Pattern.compile("(?i)"+label+"\\s*"+suffix+"\\s*[:#-]?\\s*([0-9][0-9\\s-]{2,24})").matcher(flat(text));return m.find()?m.group(1).replaceAll("\\D",""):"";}
 private static String flat(String text){return text==null?"":text.replace('\n',' ').replace('\r',' ').replaceAll("\\s+"," ");}
}
