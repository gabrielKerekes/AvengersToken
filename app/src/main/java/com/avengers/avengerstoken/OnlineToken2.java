package com.avengers.avengerstoken;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avengers.Hotp.OCRAhotpGenerator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class OnlineToken2 extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "message";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public final static int REQUEST_CODE_QR_END = 1;

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    String regid = null;
    Context context;
    String imei;
    String messageType;
    String initString = null;
    SharedPreferences settings;
    private int responseCode = -1;

    Button ano;
    Button nie;
    TextView msgView;
    String msg = null;
    String counter = null;

    int amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_token);

        ano = (Button) findViewById(R.id.ano);
        nie = (Button) findViewById(R.id.nie);
        msgView = (TextView) findViewById(R.id.message_textView);

        gcm = GoogleCloudMessaging.getInstance(this);
        context = getApplicationContext();
        regid = getRegistrationId(context);
        settings = getApplicationContext().getSharedPreferences("qrresult",0);

        TelephonyManager telman = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei = telman.getDeviceId();

        try
        {
            // todo: GABO - extra keys to string
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            msg = extras.getString("msg");
            counter = extras.getString("mCounter");
            amount = extras.getInt("amount");

            msgView.setText(msg + " - " + amount);

        }catch (Exception e){ }

        if(counter.equals(" ")){
            Intent mainIntent = new Intent(OnlineToken2.this,MainActivity.class);
            startActivity(mainIntent);
        }

        msgView.setGravity(Gravity.CENTER);

        ano.setVisibility(View.VISIBLE);
        nie.setVisibility(View.VISIBLE);

        android.support.v7.app.ActionBar aBar = getSupportActionBar();
        aBar.setTitle("Confirm action");
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2B3742")));

    }

    public void onClick(View v){
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei= telephonyManager.getDeviceId();

        if(imei == null) {
            imei = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        String or_mess = null;
        String answ = null;
        String uname = null;

        SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        uname = sharedPreferences.getString("uname", "");
        or_mess = msg;

        messageType = "SEND";

        OCRAhotpGenerator ocra = new OCRAhotpGenerator();
        String ocraMsg = null;

        switch (v.getId()) {
            case R.id.ano:
                Log.e("TAG",imei);
                answ = "ano";
                msg = "ano:" + msg;
                msg = String.format("%040x", new BigInteger(1, msg.getBytes(/*YOUR_CHARSET?*/)));
                break;

            case R.id.nie:
                answ = "nie";
                msg = "nie:" + msg;
                msg = String.format("%040x", new BigInteger(1, msg.getBytes(/*YOUR_CHARSET?*/)));
                Log.e("TAG", imei);
                break;
        }

        ocraMsg = ocra.generateOCRA("OCRA-1:HOTP-SHA1-6:QN08", imei, msg);

        if (ocraMsg != null || ocraMsg.length() > 0) {
            sendMessage(uname, answ, or_mess, ocraMsg, amount);
            Log.e("TAG", "ocra send " + ocraMsg);
        } else Log.e("TAG", "ocra string je nulovy");
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_online_token2, menu);
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

    private String getRegistrationId(Context context)
    {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(Globals.PREFS_PROPERTY_REG_ID, "");
        if (registrationId == null || registrationId.equals(""))
        {
            Log.i(Globals.TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion)
        {
            Log.i(Globals.TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGcmPreferences(Context context)
    {
        // This sample app persists the registration ID in shared preferences,
        // but how you store the regID in your app is up to you.
        return getSharedPreferences(Globals.PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context)
    {
        try
        {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            }
            else
            {
                Log.i(Globals.TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void sendMessage(String uname, String answer, String or_msg, String ocra, int amount)
    {
        if(regid == null || regid.equals(""))
        {
            Toast.makeText(this, "You must register first", Toast.LENGTH_LONG).show();
            return;
        }

        new AsyncTask<String, Void, String>()
        {
            @Override
            protected String doInBackground(String... params)
            {
                String msg = "";
                try
                {
                    String uname = params[0];
                    String answer = params[1];
                    String message = params[2];
                    String ocra = params[3];
                    String amount = params[4];

                    JSONObject data = new JSONObject();

                    data.put("uname", uname);
                    data.put("msg", message);
                    data.put("answer", answer);
                    data.put("ocra", ocra);
                    data.put("counter", counter);
                    data.put("amount", amount);

                    //HttpsURLConnection urlConnection = null;
                    HttpURLConnection urlConnection = null;

                    try {
                        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            @Override
                            public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
                                // Not implemented
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
                                // Not implemented
                            }
                        } };

                        SSLContext sc = SSLContext.getInstance("TLS");
                        sc.init(null, trustAllCerts, new java.security.SecureRandom());
                        //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                        //HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());

                        //URL url = new URL("https://147.175.98.16:8443/testRest/rs/service/confTrans");
                        URL url = new URL(ServiceIp.GetIp(context) + "/confTrans");

                        //read data
                        //urlConnection = (HttpsURLConnection)url.openConnection();
                        urlConnection = (HttpURLConnection)url.openConnection();
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setRequestProperty("Content-Type", "application/json");

                        OutputStream os = urlConnection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        writer.write(data.toString());
                        writer.flush();
                        writer.close();
                        os.close();

                        responseCode = urlConnection.getResponseCode();


                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    }
                }
                catch (Exception ex)
                {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg)
            {
                evaluateResponse(responseCode);
            }
        }.execute(uname,answer,or_msg,ocra, amount + "");
    }

    private void evaluateResponse(int resp){

        switch (resp) {
            case -1:
                Toast.makeText(context, "Chyba pri odoslani spravy", Toast.LENGTH_LONG).show();
                break;
            case 201:
                Toast.makeText(context, "Sprava uspesne odoslana", Toast.LENGTH_LONG).show();
                break;
            case 500:
                Toast.makeText(context, "Chyba servera", Toast.LENGTH_LONG).show();
                break;
        }

        Intent mainActivity = new Intent(OnlineToken2.this, MainActivity.class);
        startActivity(mainActivity);

    }


    private void registerInBackground()
    {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                String msg = "";
                try
                {
                    if (gcm == null)
                    {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(Globals.GCM_SENDER_ID);

                    // You should send the registration ID to your server over
                    // HTTP, so it can use GCM/HTTP or CCS to send messages to your app.
                     sendRegistrationIdToBackend();

                    // For this demo: we use upstream GCM messages to send the
                    // registration ID to the 3rd party server

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                }
                catch (IOException ex)
                {
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg)
            {
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend()
    {
        Log.d(Globals.TAG, "REGISTER USERID: " + regid);
        new AsyncTask<String, Void, String>()
        {
            @Override
            protected String doInBackground(String... params)
            {
                String msg = "";

                try
                {
                    Bundle data = new Bundle();
                    data.putString("message", regid + ":" + imei + ":" + initString);
                    data.putString("action", messageType);
                    String id = Integer.toString(msgId.incrementAndGet());
                    gcm.send(Globals.GCM_SENDER_ID + "@gcm.googleapis.com", id, Globals.GCM_TIME_TO_LIVE, data);
                    msg = "Sent registration";
                }
                catch (IOException ex)
                {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg)
            {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId)
    {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(Globals.TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Globals.PREFS_PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data)
    {


        switch (requestCode)
        {
            case REQUEST_CODE_QR_END:
                Log.e(Globals.TAG,"qr skoncila mozem registrovat");

                if (checkPlayServices()) {
                    messageType = "REGISTER";
                    initString = settings.getString("qrresult", null);

                    regid = getRegistrationId(context);
                    registerInBackground();

                } else {
                    Log.i(Globals.TAG, "No valid Google Play Services APK found.");
                }

                break;

        }
    }

    public class NullHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            Log.i("RestUtilImpl", "Approving certificate for " + hostname);
            return true;
        }

    }
}
