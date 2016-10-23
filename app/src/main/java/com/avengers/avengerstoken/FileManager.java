package com.avengers.avengerstoken;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import javax.crypto.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class FileManager {

    private static final String TAG = FileManager.class.getName();

    public String filename; //nazov suboru
    public InputStream file; //subor
   // public Context context;  //kontext na aktivitu

    private String filepass;
    private String encryptfile; //desifrovany text
    private String decryptfile; //sifrovany text
    private byte[]  fileInbytes; //subor v bajtoch

    public FileManager(Context context)
    {
     // this.context=context;
      this.file=null;

    }

    public void setName(String name)
    {
      //nastavi nazov suboru
      this.filename=name;
    }

    public void openFile()
    {
           //otvori subor

           try {
               //if(filename=="gridcard.txt")file=context.getResources().openRawResource(R.raw.gridcard);
               //else
               //{
                   file=new FileInputStream(filename);
               //}

           }
           catch(java.io.FileNotFoundException e)
           {
               Log.e(TAG, "File not found" + e.toString());
           }

    }
    public void readFile()
    {
          //funkcia nacita z otvoreneho suboru data do premennej encryptfile
          //po nacitani zatvori subor


        try {
            if(file!=null) {

                 fileInbytes=new byte[file.available()];
                 file.read(fileInbytes);


            }
           file.close();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Can not read file: " + e.toString());
        }



        //nacitam subor do pola bajtov a ulozim ako string -> kodovanie base64
        setEncryptfile(new String (fileInbytes));

        if(filepass != null) {
            try {
                decryptFile(getEncryptfile(), filepass);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }
        else setDecryptfile("Set your password!!!");


    }



    public void decryptFile(String plain_text,String pass) throws NoSuchAlgorithmException, NoSuchPaddingException {


        //do tohoto sa ulozi rozsifrovany text
        String text = " ";

        //zahashovanie pouziteho kluca "pass" na 256 bitovy has (koli korektnosi kluca pre AES)

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(pass.getBytes()); //zo stringu pass
        byte[] key = md.digest();

        //specifikacia kluca pre sifru AES, tento sa puziva pri sifrovani
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        //nastavenie sifry

        IvParameterSpec ivSpec = new IvParameterSpec("1234567891012345".getBytes());

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        try {
            //inicializacia sifry pre sifrovanie s klucom keySpec
            cipher.init(Cipher.DECRYPT_MODE, keySpec,ivSpec);

        } catch (InvalidKeyException e) {
            e.printStackTrace();

        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        try {
            //sifrovanie
            byte[] decrypted = cipher.doFinal(Base64.decode(plain_text.getBytes(), Base64.DEFAULT));


            //spatne vytvorenie stringy s byte array
            text = new String(decrypted);

        } catch (Exception e) {
            text="";
        }

        Log.d(TAG,text);
        setDecryptfile(text);

    }


    public void setDecryptfile(String file)
    {
        //nastavi desifrovany text
        this.decryptfile=file;
    }
    public String getDecryptfile()
    {
        //vrati desifrovany text
        return decryptfile;
    }
    public void setEncryptfile(String file)
    {
        //nastavi zasifrovany text
        this.encryptfile=file;
    }
    public String getEncryptfile()
    {
        //vrati sifrovany subor
        return encryptfile;
    }

    public void setFilepass(String pass)
    {
        filepass=pass;
    }
    public String getFilepass()
    {
        return filepass;
    }

}
//    public void readFile()
//    {
//        //funkcia nacita z otvoreneho suboru data do premennej decryptfile
//        //po nacitani zatvori subor
//
//        StringBuffer stringBuf = new StringBuffer();
//
//        try {
//            if(file!=null) {
//
//                InputStreamReader tmp=new InputStreamReader(file);
//                BufferedReader reader = new BufferedReader(tmp);
//                String str="";
//
//
//
//                while((str=reader.readLine())!=null)
//                {
//                    stringBuf.append(str+"\n");
//                    //rows++;
//
//                }
//            }
//            file.close();
//        }
//        catch (IOException e)
//        {
//            Log.e(TAG, "Can not read file: " + e.toString());
//        }
//
//        setEncryptfile(stringBuf.toString());
//
//
//    }


//    public void encryptFile(String plain_text,String pass) throws NoSuchAlgorithmException, NoSuchPaddingException {
//
//         plain_text="Zadaj cestu k suboru!!!";
//              //do tohoto sa ulozi zasifrovany text
//        String text = " ";
//        //plain_text="cae25 4d214 07d9a 8f2fb 70784 01848 a0f91 ef243 773b1 468fd";
//        //zahashovanie pouziteho kluca "pass" na 256 bitovy has (koli korektnosi kluca pre AES)
//        MessageDigest md = MessageDigest.getInstance("SHA-256");
//        md.update(pass.getBytes()); //zo stringu pass
//
//        byte[] key = md.digest();
//
//        //specifikacia kluca pre sifru AES, tento sa puziva pri sifrovani
//        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
//
//
//        //inicializacny vektor
//        IvParameterSpec ivSpec = new IvParameterSpec("1234567891012345".getBytes());
//        //nastavenie sifry
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//
//        try {
//            //inicializacia sifry pre sifrovanie s klucom keySpec
//            cipher.init(Cipher.ENCRYPT_MODE, keySpec,ivSpec);
//
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//
//        } catch (InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            //sifrovanie
//            byte[] encrypted = cipher.doFinal(plain_text.getBytes("UTF-8"));
//
//
//            //spatne vytvorenie stringu s byte array
//            text =Base64.encodeToString(encrypted, Base64.DEFAULT);
//
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        setEncryptfile(text);
//
//    }