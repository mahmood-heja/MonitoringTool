package com.ma.monitoringlibrary;


import android.util.Log;

import org.kobjects.base64.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by abeer on 20-Nov-16.
 */

public class Security {
    private String key;

    public  Security(String key)
    {
        this.key = key;
    }

    public  String Decrypt(String text) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //this parameters should not be changed
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length)
            len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] results = new byte[text.length()];
        try {
            results = cipher.doFinal(Base64.decode(text));//decodeBuffer(text)
        } catch (Exception e) {
            Log.i("Erron in Decryption", e.toString());
        }
        Log.i("Data", new String(results, "UTF-8"));
        return new String(results, "UTF-8"); // it returns the result as a String
    }

    public  String Encrypt(String text) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length)
            len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));

        return Base64.encode(results); // it returns the result as a String
    }
}
