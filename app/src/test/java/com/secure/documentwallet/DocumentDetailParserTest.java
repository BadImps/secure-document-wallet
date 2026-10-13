package com.secure.documentwallet;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentDetailParserTest {
 @Test public void readsDriverPermitLabels()throws Exception{JSONObject result=DocumentDetailParser.identity("DRIVER PERMIT\nIssue Date 4/7/2024\nExpiry Date: 04-07-2029\nLicence # 123 456 789",true);assertEquals("Driver's Permit",result.getString("issuer"));assertEquals("04/07/2024",result.getString("issueDate"));assertEquals("04/07/2029",result.getString("expiryDate"));assertEquals("123456789",result.getString("documentNumber"));}
 @Test public void readsNationalIdBackLabels()throws Exception{JSONObject result=DocumentDetailParser.identity("Date of Issue: 03/02/2025 Registration No. 9988-7766 Expiry Date 03/02/2035",false);assertEquals("National Identification Card",result.getString("issuer"));assertEquals("03/02/2025",result.getString("issueDate"));assertEquals("03/02/2035",result.getString("expiryDate"));assertEquals("99887766",result.getString("documentNumber"));}
 @Test public void convertsBankCardMonthAndYear()throws Exception{JSONObject result=DocumentDetailParser.bank("VALID THRU 09/29","First Citizens");assertEquals("First Citizens",result.getString("issuer"));assertEquals("September/2029",result.getString("expiryDate"));assertNotNull(ExpiryUi.parse(result.getString("expiryDate")));}
}
