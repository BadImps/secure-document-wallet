package com.secure.documentwallet;

import org.json.JSONObject;
import java.text.DateFormatSymbols;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.*;

/** Extracts only explicitly labelled fields from on-device OCR text. */
final class DocumentDetailParser {
 private DocumentDetailParser(){}
 static JSONObject identity(String text,boolean front){JSONObject out=new JSONObject();try{out.put("issuer",front?"Driver's Permit":"National Identification Card");out.put("issueDate",date(text,"ISSUE\\s*DATE|DATE\\s*OF\\s*ISSUE|ISSUED(?:\\s*ON)?|VALID\\s*FROM"));out.put("expiryDate",date(text,"EXPIR(?:Y|ATION)\\s*DATE|DATE\\s*OF\\s*EXPIRY|EXPIRES|EXP\\.?\\s*DATE|VALID\\s*(?:UNTIL|TO)"));out.put("documentNumber",digitsAfter(text,front?"LICEN[CS]E":"REGISTRATION",front?"(?:#|NO\\.?|NUMBER)?":"(?:NO\\.?|NUMBER|#)?"));}catch(Exception ignored){}return out;}
 static JSONObject bank(String text,String issuer){JSONObject out=new JSONObject();try{out.put("issuer",issuer==null?"":issuer.trim());Matcher m=Pattern.compile("(?i)(?:GOOD|VALID)\\s*THRU\\s*[:#-]?\\s*(0?[1-9]|1[0-2])\\s*[/.-]\\s*(\\d{2}|\\d{4})").matcher(flat(text));String expiry="";if(m.find()){int month=Integer.parseInt(m.group(1)),year=Integer.parseInt(m.group(2));if(year<100)year+=year<50?2000:1900;expiry=new DateFormatSymbols(Locale.US).getMonths()[month-1]+"/"+year;}out.put("expiryDate",expiry);}catch(Exception ignored){}return out;}
 private static String date(String text,String labels){String value=flat(text);Matcher label=Pattern.compile("(?i)(?:"+labels+")\\s*[:#-]?\\s*").matcher(value);if(!label.find())return "";String candidate=value.substring(label.end(),Math.min(value.length(),label.end()+48)).replaceAll("(?i)(\\d{1,2})(?:st|nd|rd|th)","$1");Matcher iso=Pattern.compile("\\b(\\d{4})\\s*[/.-]\\s*(\\d{1,2})\\s*[/.-]\\s*(\\d{1,2})\\b").matcher(candidate);if(iso.find())return normalized(number(iso,3),number(iso,2),number(iso,1));Matcher numeric=Pattern.compile("\\b(\\d{1,2})\\s*[/.-]\\s*(\\d{1,2})\\s*[/.-]\\s*(\\d{2,4})\\b").matcher(candidate);if(numeric.find()){int first=number(numeric,1),second=number(numeric,2);return first<=12&&second>12?normalized(second,first,number(numeric,3)):normalized(first,second,number(numeric,3));}String monthNames="JAN(?:UARY)?|FEB(?:RUARY)?|MAR(?:CH)?|APR(?:IL)?|MAY|JUN(?:E)?|JUL(?:Y)?|AUG(?:UST)?|SEP(?:TEMBER)?|SEPT|OCT(?:OBER)?|NOV(?:EMBER)?|DEC(?:EMBER)?";Matcher dayFirst=Pattern.compile("(?i)\\b(\\d{1,2})[\\s,./-]+("+monthNames+")[\\s,./-]+(\\d{2,4})\\b").matcher(candidate);if(dayFirst.find())return normalized(number(dayFirst,1),month(dayFirst.group(2)),number(dayFirst,3));Matcher monthFirst=Pattern.compile("(?i)\\b("+monthNames+")[\\s,./-]+(\\d{1,2})[\\s,./-]+(\\d{2,4})\\b").matcher(candidate);if(monthFirst.find())return normalized(number(monthFirst,2),month(monthFirst.group(1)),number(monthFirst,3));Matcher yearFirst=Pattern.compile("(?i)\\b(\\d{4})[\\s,./-]+("+monthNames+")[\\s,./-]+(\\d{1,2})\\b").matcher(candidate);if(yearFirst.find())return normalized(number(yearFirst,3),month(yearFirst.group(2)),number(yearFirst,1));Matcher compact=Pattern.compile("\\b(\\d{4})(\\d{2})(\\d{2})\\b").matcher(candidate);return compact.find()?normalized(number(compact,3),number(compact,2),number(compact,1)):"";}
 private static int number(Matcher matcher,int group){return Integer.parseInt(matcher.group(group));}
 private static String normalized(int day,int month,int year){if(year<100)year+=year<50?2000:1900;try{LocalDate date=LocalDate.of(year,month,day);return String.format(Locale.US,"%02d/%02d/%04d",date.getDayOfMonth(),date.getMonthValue(),date.getYear());}catch(DateTimeException ignored){return "";}}
 private static int month(String value){String key=value.toUpperCase(Locale.US).replace("SEPT","SEP");for(int i=0;i<MONTHS.length;i++)if(key.startsWith(MONTHS[i]))return i+1;return 0;}
 private static final String[] MONTHS={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
 private static String digitsAfter(String text,String label,String suffix){Matcher m=Pattern.compile("(?i)"+label+"\\s*"+suffix+"\\s*[:#-]?\\s*([0-9][0-9\\s-]{2,24})").matcher(flat(text));return m.find()?m.group(1).replaceAll("\\D",""):"";}
 private static String flat(String text){return text==null?"":text.replace('\n',' ').replace('\r',' ').replaceAll("\\s+"," ");}
}
