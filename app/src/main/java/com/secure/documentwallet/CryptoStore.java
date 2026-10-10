package com.secure.documentwallet;

import android.content.Context;import java.io.*;import java.security.*;import javax.crypto.*;import javax.crypto.spec.*;

/** Encrypts every vault object independently with AES-256-GCM backed by Android Keystore. */
final class CryptoStore {
 private static final byte[] MAGIC={'S','D','W','1'};
 private final Context context;
 CryptoStore(Context c){context=c.getApplicationContext();}
 private SecretKey key(){return Session.key();}
 File vaultDir(){File d=new File(context.getFilesDir(),"vault");if(!d.exists()&&!d.mkdirs())throw new IllegalStateException("Cannot create encrypted vault");return d;}
 File encrypt(InputStream in,String name)throws Exception{File out=new File(vaultDir(),name+".sdw");Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());try(DataOutputStream data=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out)))){data.write(MAGIC);byte[] iv=c.getIV();data.writeByte(iv.length);data.write(iv);try(CipherOutputStream encrypted=new CipherOutputStream(data,c)){copy(in,encrypted);}}return out;}
 byte[] decrypt(File file)throws Exception{try(DataInputStream data=new DataInputStream(new BufferedInputStream(new FileInputStream(file)))){byte[] magic=new byte[4];data.readFully(magic);if(!MessageDigest.isEqual(MAGIC,magic))throw new SecurityException("Invalid vault file");byte[] iv=new byte[data.readUnsignedByte()];data.readFully(iv);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,iv));try(CipherInputStream clear=new CipherInputStream(data,c);ByteArrayOutputStream out=new ByteArrayOutputStream()){copy(clear,out);return out.toByteArray();}}}
 void encryptBytes(byte[] bytes,String name)throws Exception{try(ByteArrayInputStream in=new ByteArrayInputStream(bytes)){encrypt(in,name);}}
 void delete(String name){File f=new File(vaultDir(),name+".sdw");if(f.exists())f.delete();}
 private static void copy(InputStream in,OutputStream out)throws IOException{byte[] b=new byte[64*1024];for(int n;(n=in.read(b))!=-1;)out.write(b,0,n);}
}
