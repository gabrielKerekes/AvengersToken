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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SynchronizeActivity extends AppCompatActivity {

    Button syncButt;
    String pwd="";
    String uname="";
    private View.OnClickListener callWSListener;
    private static final String PROPERTY_APP_VERSION = "appVersion";
    GoogleCloudMessaging gcm;
    String regid = null;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronize);

        gcm = GoogleCloudMessaging.getInstance(this);
        context = getApplicationContext();

        callWSListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //parseAndSend(v);
                synchronizeDevice();
            }
        };

        syncButt = (Button)findViewById(R.id.syncButt);
        syncButt.setOnClickListener(callWSListener);

        android.support.v7.app.ActionBar aBar = getSupportActionBar();
        aBar.setTitle("Device Synchronization");
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2B3742")));
    }

    private void synchronizeDevice(){

        uname = ((TextView)findViewById(R.id.unameT)).getText().toString();
        pwd = ((TextView)findViewById(R.id.passwdT)).getText().toString();

        if(!uname.equals("")&&!pwd.equals("")) {

            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... params) {
                    String msg = "";

                    if (gcm == null){
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }

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
                        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());

                        regid = gcm.register(Globals.GCM_SENDER_ID);
                        storeRegistrationId(context, regid);

                        TelephonyManager telman = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                        String imei = telman.getDeviceId();

                        if(imei == null) {
                            imei = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                        }

                        String[] regidSplit = regid.split(":");
                        if (regidSplit.length > 1)
                            regid = regidSplit[1];

                        String data = uname+":"+pwd+":"+regid+":"+imei;

                        //URL url = new URL("https://147.175.98.16:8443/testRest/rs/service/synchronizeDev?data="+data);
                        URL url = new URL(ServiceIp.GetIp(context) + "/synchronizeDev?data="+data);

                        // uncomment for tls
                        //HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
                        // comment for tls
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        InputStream in = urlConnection.getInputStream();
                        BufferedReader r = new BufferedReader(new InputStreamReader(in));

                        StringBuilder total = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) {
                            total.append(line);
                        }

                        String grid_enc = total.toString();

                        if(!grid_enc.equals("err")) {

                            SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putString("grid", grid_enc);
                            editor.putString("uname", uname);
                            editor.commit();

                            SharedPreferences gcm_pref = context.getSharedPreferences(Globals.PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor gcm_edit = gcm_pref.edit();
                            gcm_edit.putString(Globals.PREFS_PROPERTY_REG_ID, regid);
                            gcm_edit.commit();

                            msg = "suc";
                        }else msg = "err";

                    } catch (Exception e) {
                        msg="err";
                    }

                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    if(msg.equals("suc")){
                        Toast.makeText(context, "Device synchronized", Toast.LENGTH_LONG).show();
                        Intent nextactivity = new Intent(SynchronizeActivity.this, MainActivity.class);
                        startActivity(nextactivity);
                    }else{
                        Toast.makeText(context, "Synchronization refused", Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }
    }

    private void storeRegistrationId(Context context, String regId){
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(Globals.TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Globals.PREFS_PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private SharedPreferences getGcmPreferences(Context context){
        return getSharedPreferences(Globals.PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_synch, menu);
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

    public class NullHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            Log.i("RestUtilImpl", "Approving certificate for " + hostname);
            return true;
        }

    }
}
