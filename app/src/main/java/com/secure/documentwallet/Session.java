package com.secure.documentwallet;
import javax.crypto.SecretKey;import javax.crypto.spec.SecretKeySpec;import javax.crypto.SecretKeyFactory;import javax.crypto.spec.PBEKeySpec;import java.util.Arrays;
final class Session {
 static long authorizedUntil;private static byte[] vaultKey;
 static void unlock(String pin,byte[] salt)throws Exception{PBEKeySpec spec=new PBEKeySpec(pin.toCharArray(),salt,310000,256);vaultKey=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();spec.clearPassword();authorizedUntil=System.currentTimeMillis()+5*60_000L;}
 static boolean valid(){return System.currentTimeMillis()<authorizedUntil&&vaultKey!=null;}
 static SecretKey key(){if(!valid())throw new SecurityException("Vault is locked");return new SecretKeySpec(vaultKey,"AES");}
 static void lock(){authorizedUntil=0;if(vaultKey!=null)Arrays.fill(vaultKey,(byte)0);vaultKey=null;}
}
