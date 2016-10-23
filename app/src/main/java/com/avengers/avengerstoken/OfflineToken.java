package com.avengers.avengerstoken;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.avengers.Hotp.HotpGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static java.security.AccessController.getContext;


public class OfflineToken extends AppCompatActivity {

    TextView imeiText; //zobrazuje imei
    TextView showToken; //zobrazuje token

    final int SETPASS=10; //menu

    private String existFile=null; //meno suboru pre ulozenie countra


    private byte[] secret=null;   //secret
    private String imei; // secret

    private HotpGenerator generator; //hotp generator

    //funkcia spusti aktivitu nacita Imei, vytvori generator, nacita counter
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_token);

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei= telephonyManager.getDeviceId();

        if(imei == null) {
            imei = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        generator=new HotpGenerator();

         existFile=loadCounter();

        if(existFile==null || existFile.length()<=0){
            generator.setCounter(0L);
        }
        else {
            generator.setCounter(Long.parseLong(loadCounter()));
            generator.incrementCounter();
        }

        showToken = (TextView) findViewById(R.id.showToken);

        android.support.v7.app.ActionBar aBar = getSupportActionBar();
        aBar.setTitle("One-Time Password");
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2B3742")));
    }


    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_offline_token, menu);

        return true;
    }
    //menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Toast.makeText(getApplicationContext(), item.getTitle() + " menu selected", Toast.LENGTH_SHORT).show();

        switch (item.getItemId()) {
            case R.id.menu1:
                Intent intent1 = new Intent(OfflineToken.this,PasswordWrite.class);

                OfflineToken.this.startActivityForResult(intent1,SETPASS);

        }
        return true;
    }
    //ak je heslo zadane zmeni heslo do hexa= hexPass
    //vytvori secret pomocou imei a hesla

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case SETPASS:
                if(resultCode==RESULT_OK)
                {

                    try {
                        String tmp = data.getStringExtra("password");
                        String hexPass = String.format("%040x", new BigInteger(1, tmp.getBytes(/*YOUR_CHARSET?*/)));
                        imei = String.format("%040x", new BigInteger(1, imei.getBytes(/*YOUR_CHARSET?*/)));
                        Log.e("tu je imei ",imei);

                        SharedPreferences sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("pin", tmp);
                        editor.commit();

                        secret=generator.hmac_sha2(imei.getBytes(),hexPass.getBytes());

                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(this,"You can generate token !!!",
                            Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    //zobrazi imai
/*    public void showImei(View view)
    {

        imeiText.setText(imei);
        imeiText.setGravity(Gravity.CENTER);

    }*/

    //zobrazi token
    // vygeneruje token kde ak nie je zadane heslo pyta secret.
    public void showToken(View view) throws InvalidKeyException, NoSuchAlgorithmException {

        generator.setSecret(secret);
        String b= null;

        if(secret==null)
        {
         b= "Set PIN";
        } else {
            b = generator.generateOTP(generator.getSecret(), generator.getCounter(), 6, false, 65535);
        }

        showToken.setText(b);
        showToken.setGravity(Gravity.CENTER);
        generator.incrementCounter();
        //ulozenie countera
        saveCounter(generator.getCounter().toString());


    }

    public void backButton(View view )
    {
            finish();
    }

    //ulozenie countera
    private boolean saveCounter(String data) {

        try
        {

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("config1.txt", Context.MODE_PRIVATE));
            outputStreamWriter.flush();
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Log.e("Counter", "write was successful");
            return true;
        }
        catch (IOException e) {
            Log.e("OfflineToken", "File write failed: " + e.toString());
            return false;
        }

    }

    //nacitanie cesty z config.txt
    private String loadCounter() {

        String ret = null;
        InputStream inputStream = null;
        try {
            inputStream = openFileInput("config1.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("Offline token", "File not found: " + e.toString());

        } catch (IOException e) {
            Log.e("Offline token", "Can not read file: " + e.toString());

        }finally {
             try {
                if (inputStream != null)
                    inputStream.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }
}
