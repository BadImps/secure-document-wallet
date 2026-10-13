package com.secure.documentwallet;

import android.content.*;import android.security.keystore.*;import android.util.Base64;import java.io.File;import java.security.*;import javax.crypto.*;import javax.crypto.spec.*;

/** Keeps the random vault key wrapped by Android Keystore and migrates legacy PIN-encrypted vaults once. */
final class VaultKeyManager {
 private static final String PREF="vault-master-key",WRAPPED="wrapped",IV="iv",ALIAS="my-vault-master-wrap-v1";
 static void unlock(Context context,String pin,byte[] pinSalt)throws Exception{
  Context c=context.getApplicationContext();SharedPreferences p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
  if(p.contains(WRAPPED)){Session.use(unwrap(p));return;}
  byte[] fresh=new byte[32];new SecureRandom().nextBytes(fresh);File[] existing=new CryptoStore(c).vaultDir().listFiles((d,n)->n.endsWith(".sdw"));
  if(existing!=null&&existing.length>0){Session.unlock(pin,pinSalt);SecretKey old=Session.key();new CryptoStore(c).rekey(old,new SecretKeySpec(fresh,"AES"));}
  install(c,fresh);Session.use(fresh);java.util.Arrays.fill(fresh,(byte)0);
 }
 static boolean protectedByKeystore(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE).contains(WRAPPED);}
 static void install(Context context,byte[] master)throws Exception{Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,wrappingKey());byte[] wrapped=cipher.doFinal(master);context.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString(WRAPPED,Base64.encodeToString(wrapped,Base64.NO_WRAP)).putString(IV,Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP)).commit();}
 private static byte[] unwrap(SharedPreferences p)throws Exception{Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,wrappingKey(),new GCMParameterSpec(128,Base64.decode(p.getString(IV,""),Base64.NO_WRAP)));return cipher.doFinal(Base64.decode(p.getString(WRAPPED,""),Base64.NO_WRAP));}
 private static SecretKey wrappingKey()throws Exception{KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);if(store.containsAlias(ALIAS))return (SecretKey)store.getKey(ALIAS,null);KeyGenerator generator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");generator.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build());return generator.generateKey();}
}
