package com.secure.documentwallet;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentDetailParserTest {
 @Test public void readsDriverPermitLabels()throws Exception{JSONObject result=DocumentDetailParser.identity("DRIVER PERMIT\nIssue Date 4/7/2024\nExpiry Date: 04-07-2029\nLicence # 123 456 789",true);assertEquals("Driver's Permit",result.getString("issuer"));assertEquals("04/07/2024",result.getString("issueDate"));assertEquals("04/07/2029",result.getString("expiryDate"));assertEquals("123456789",result.getString("documentNumber"));}
 @Test public void readsNationalIdBackLabels()throws Exception{JSONObject result=DocumentDetailParser.identity("Date of Issue: 03/02/2025 Registration No. 9988-7766 Expiry Date 03/02/2035",false);assertEquals("National Identification Card",result.getString("issuer"));assertEquals("03/02/2025",result.getString("issueDate"));assertEquals("03/02/2035",result.getString("expiryDate"));assertEquals("99887766",result.getString("documentNumber"));}
 @Test public void normalizesWrittenMonthDates()throws Exception{JSONObject result=DocumentDetailParser.identity("Issued on March 15th, 2024 Licence # 123456 Expires 7 September 2029",true);assertEquals("15/03/2024",result.getString("issueDate"));assertEquals("07/09/2029",result.getString("expiryDate"));}
 @Test public void normalizesIsoAndUnambiguousUsDates()throws Exception{JSONObject result=DocumentDetailParser.identity("Valid From 2024-03-15 Registration No 123456 Valid Until 03/25/2029",false);assertEquals("15/03/2024",result.getString("issueDate"));assertEquals("25/03/2029",result.getString("expiryDate"));}
 @Test public void rejectsImpossibleCalendarDates()throws Exception{JSONObject result=DocumentDetailParser.identity("Issue Date 31/02/2024 Licence # 123456 Expiry Date 2029-02-30",true);assertEquals("",result.getString("issueDate"));assertEquals("",result.getString("expiryDate"));}
 @Test public void convertsBankCardMonthAndYear()throws Exception{JSONObject result=DocumentDetailParser.bank("VALID THRU 09/29","First Citizens");assertEquals("First Citizens",result.getString("issuer"));assertEquals("September/2029",result.getString("expiryDate"));assertNotNull(ExpiryUi.parse(result.getString("expiryDate")));}
 @Test public void findsUnlabelledBankExpiryOnWholeCard()throws Exception{JSONObject result=DocumentDetailParser.bank("FIRST CITIZENS 5412 3456 7890 1234 CARDHOLDER 09/29","First Citizens");assertEquals("September/2029",result.getString("expiryDate"));}
 @Test public void prioritizesStandaloneMonthYearAndLatestFutureValue()throws Exception{JSONObject result=DocumentDetailParser.bank("MEMBER SINCE 05/2018  CARDHOLDER  VALID 11-28","Bank");assertEquals("November/2028",result.getString("expiryDate"));}
 @Test public void fallsBackToWrittenBankExpiry()throws Exception{JSONObject result=DocumentDetailParser.bank("PLATINUM CARD Expires September 2029","Bank");assertEquals("September/2029",result.getString("expiryDate"));}
}
