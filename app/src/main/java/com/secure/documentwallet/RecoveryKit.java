package com.secure.documentwallet;

import android.content.Context;import android.util.Base64;import org.json.JSONObject;import java.io.*;import java.nio.charset.StandardCharsets;import java.security.SecureRandom;import javax.crypto.*;import javax.crypto.spec.*;

/** Recovery package for moving the random vault key to a replacement phone. The recovery code is never stored. */
final class RecoveryKit {
 private static final String NAME="vault-recovery-kit.json",ALPHABET="ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
 static File file(Context c){return new File(c.getNoBackupFilesDir(),NAME);}
 static boolean ready(Context c){return file(c).isFile();}
 static String create(Context c)throws Exception{String code=randomCode();byte[] salt=new byte[24],iv=new byte[12];SecureRandom random=new SecureRandom();random.nextBytes(salt);random.nextBytes(iv);SecretKey key=derive(code,salt);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key,new GCMParameterSpec(128,iv));byte[] wrapped=cipher.doFinal(Session.copyKey());JSONObject kit=new JSONObject().put("version",1).put("salt",b64(salt)).put("iv",b64(iv)).put("wrappedKey",b64(wrapped));write(file(c),kit.toString().getBytes(StandardCharsets.UTF_8));return format(code);}
 static byte[] bytes(Context c)throws IOException{return java.nio.file.Files.readAllBytes(file(c).toPath());}
 static byte[] unwrap(byte[] packageBytes,String code)throws Exception{JSONObject kit=new JSONObject(new String(packageBytes,StandardCharsets.UTF_8));byte[] salt=decode(kit.getString("salt")),iv=decode(kit.getString("iv")),wrapped=decode(kit.getString("wrappedKey"));Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,derive(normalize(code),salt),new GCMParameterSpec(128,iv));return cipher.doFinal(wrapped);}
 static void save(Context c,byte[] packageBytes)throws IOException{write(file(c),packageBytes);}
 private static SecretKey derive(String code,byte[] salt)throws Exception{PBEKeySpec spec=new PBEKeySpec(normalize(code).toCharArray(),salt,420000,256);try{return new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(),"AES");}finally{spec.clearPassword();}}
 private static String randomCode(){SecureRandom random=new SecureRandom();StringBuilder out=new StringBuilder(24);for(int i=0;i<24;i++)out.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));return out.toString();}
 private static String normalize(String code){return code==null?"":code.toUpperCase(java.util.Locale.US).replaceAll("[^A-Z2-9]","");}
 private static String format(String raw){StringBuilder out=new StringBuilder();for(int i=0;i<raw.length();i++){if(i>0&&i%4==0)out.append('-');out.append(raw.charAt(i));}return out.toString();}
 private static String b64(byte[] value){return Base64.encodeToString(value,Base64.NO_WRAP);}
 private static byte[] decode(String value){return Base64.decode(value,Base64.NO_WRAP);}
 private static void write(File file,byte[] bytes)throws IOException{try(FileOutputStream out=new FileOutputStream(file)){out.write(bytes);out.getFD().sync();}}
}
