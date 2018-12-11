package com.ma.monitoringlibrary;



import android.util.Log;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by abeer on 20-Nov-18.
 */

class Encryption {
    private String encryptedSecretKey = "not generate yet ";

    Encryption() {

    }

    private final String publicKeyString = " MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDHb5Z6UX5YOKlN0/qAni1DLUl8IcnjbLqEi6GJ" +
            "PkUj9zpAGGLHtd5fRk4cBkd988MROwf7mdzBmjS6MZrGa3DY/WeNxnIillNnKd1UBfAzem5f/X6s" +
            "Pk54dEhE0W49yqzxfm4o2Ko5DQ2BAoqlW6dOf/6S3RpubIVPq1zvMXoV5QIDAQAB";


    String Encrypt(String text) {
        try {

            // 1. generate secret key using AES
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128); // AES is currently available in three key sizes: 128, 192 and 256 bits.The design and strength of all key lengths of the AES algorithm are sufficient to protect classified information up to the SECRET level
            SecretKey secretKey = keyGenerator.generateKey();

            String a= Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
            Log.e("SecretKey String",a);

            // 2. get string which needs to be encrypted
            //  String text = "<your_string_which_needs_to_be_encrypted_here>";

            // 3. encrypt string using secret key
            byte[] raw = secretKey.getEncoded();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
            String cipherTextString = Base64.encodeToString(cipher.doFinal(text.getBytes(Charset.forName("UTF-8"))), Base64.DEFAULT);


            // 4. get public key
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(Base64.decode(publicKeyString, Base64.DEFAULT));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(publicSpec);

            // 6. encrypt secret key using public key
            Cipher cipher2 = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher2.init(Cipher.ENCRYPT_MODE, publicKey);
            encryptedSecretKey = Base64.encodeToString(cipher2.doFinal(secretKey.getEncoded()), Base64.DEFAULT);

            // 7. pass cipherTextString (encypted sensitive data) and encryptedSecretKey to your server via your preferred way.
            // Tips:
            // You may use JSON to combine both the strings under 1 object.
            // You may use a volley call to send this data to your server.

            return cipherTextString;
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return text;

    }

    String getEncryptedSecretKey() {
        return encryptedSecretKey;
    }



}
