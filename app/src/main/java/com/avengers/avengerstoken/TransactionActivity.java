package com.avengers.avengerstoken;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avengers.Hotp.HotpGenerator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;

public class TransactionActivity extends AppCompatActivity {

    private View.OnClickListener callWSListener;
    private HotpGenerator generator;
    private String existFile=null; //meno suboru pre ulozenie countra
    private byte[] secret=null;   //secret
    private String imei; // secret

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        ((TextView)findViewById(R.id.unameTttttt)).setText("");
        ((TextView)findViewById(R.id.pwdT)).setText("");
        ((TextView)findViewById(R.id.pinT)).setText("");

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
        }

        callWSListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(v);
            }
        };

        Button loginButt = (Button)findViewById(R.id.loginButt);
        loginButt.setOnClickListener(callWSListener);

        android.support.v7.app.ActionBar aBar = getSupportActionBar();
        aBar.setTitle("Transaction");
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2B3742")));
    }

    @Override
    protected void onResume(){
        super.onResume();
        ((TextView)findViewById(R.id.unameTttttt)).setText("");
        ((TextView)findViewById(R.id.pwdT)).setText("");
        ((TextView)findViewById(R.id.pinT)).setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_transaction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        return super.onOptionsItemSelected(item);
    }

    public void login(View view){

        String uname = ((TextView)findViewById(R.id.unameTttttt)).getText().toString();
        String pwd = ((TextView)findViewById(R.id.pwdT)).getText().toString();
        String pin = ((TextView)findViewById(R.id.pinT)).getText().toString();
        String otp = null;

        String tmp = pin;
        if(tmp.equals("")){
            Toast.makeText(getApplicationContext(), "Set PIN !", Toast.LENGTH_LONG).show();
        }else {
            String hexPass = String.format("%040x", new BigInteger(1, tmp.getBytes(/*YOUR_CHARSET?*/)));
            imei = String.format("%040x", new BigInteger(1, imei.getBytes(/*YOUR_CHARSET?*/)));
            Log.e("tu je imei ", imei);

            try {
                secret = generator.hmac_sha2(imei.getBytes(), hexPass.getBytes());
                generator.setSecret(secret);
                String b = null;

                if (secret == null) {
                    otp = "Set PIN";
                } else {
                    generator.incrementCounter();
                    saveCounter(generator.getCounter().toString());
                    otp = generator.generateOTP(generator.getSecret(), generator.getCounter(), 6, false, 65535);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            String data = uname + "-" + pwd + "-" + otp;

            TransService service = new TransService(this,this,TransService.LOGIN, data, imei);
            service.execute();
        }
    }

    public void goToTransaction(){
        Intent gridcardintent = new Intent(TransactionActivity.this,DeviceActivity.class);
        startActivity(gridcardintent);
    }

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
}
