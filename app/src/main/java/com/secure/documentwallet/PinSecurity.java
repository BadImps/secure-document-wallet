package com.secure.documentwallet;

import android.content.*;import android.util.Base64;import java.security.*;import javax.crypto.SecretKeyFactory;import javax.crypto.spec.PBEKeySpec;

final class PinSecurity {
 private static final String PREF="security",SALT="pin_salt",HASH="pin_hash",VAULT_SALT="vault_salt",PIN_LENGTH="pin_length";private final SharedPreferences p;
 PinSecurity(Context c){p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);}
 boolean configured(){return p.contains(HASH);}
 void set(String pin)throws Exception{if(!pin.matches("\\d{4}"))throw new IllegalArgumentException("Use a 4-digit PIN");byte[] salt=new byte[24],vaultSalt=new byte[24];SecureRandom random=new SecureRandom();random.nextBytes(salt);random.nextBytes(vaultSalt);byte[] hash=derive(pin,salt);p.edit().putString(SALT,Base64.encodeToString(salt,Base64.NO_WRAP)).putString(HASH,Base64.encodeToString(hash,Base64.NO_WRAP)).putString(VAULT_SALT,Base64.encodeToString(vaultSalt,Base64.NO_WRAP)).putInt(PIN_LENGTH,4).apply();}
 void updateToFourDigits(String pin)throws Exception{if(!pin.matches("\\d{4}"))throw new IllegalArgumentException("Use a 4-digit PIN");byte[] salt=new byte[24];new SecureRandom().nextBytes(salt);byte[] hash=derive(pin,salt);p.edit().putString(SALT,Base64.encodeToString(salt,Base64.NO_WRAP)).putString(HASH,Base64.encodeToString(hash,Base64.NO_WRAP)).putInt(PIN_LENGTH,4).apply();}
 int pinLength(){return p.getInt(PIN_LENGTH,configured()?6:4);}
 boolean verify(String pin){try{byte[] salt=Base64.decode(p.getString(SALT,""),Base64.NO_WRAP),expected=Base64.decode(p.getString(HASH,""),Base64.NO_WRAP);return MessageDigest.isEqual(expected,derive(pin,salt));}catch(Exception e){return false;}}
 byte[] vaultSalt(){return Base64.decode(p.getString(VAULT_SALT,""),Base64.NO_WRAP);}
 private byte[] derive(String pin,byte[] salt)throws Exception{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(pin.toCharArray(),salt,160000,256)).getEncoded();}
}
