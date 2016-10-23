package com.avengers.avengerstoken;

import android.content.Context;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class RegActivity extends AppCompatActivity {

    private View.OnClickListener callWSListener;
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    GoogleCloudMessaging gcm;
    String regid = null;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        gcm = GoogleCloudMessaging.getInstance(this);
        context = getApplicationContext();

        callWSListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //parseAndSend(v);
                registerInBackground();
            }
        };

        Button okButt = (Button)findViewById(R.id.okbutt);
        okButt.setOnClickListener(callWSListener);

        android.support.v7.app.ActionBar aBar = getSupportActionBar();
        aBar.setTitle("Registration");
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2B3742")));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_reg, menu);

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

    public void parseAndSend(){

        String uname = ((TextView)findViewById(R.id.unameTttttt)).getText().toString();
        String pwd = ((TextView)findViewById(R.id.pwdT)).getText().toString();
        String rep_pwd = ((TextView)findViewById(R.id.rep_pwdT)).getText().toString();
        String f_name = ((TextView)findViewById(R.id.f_nameT)).getText().toString();
        String l_name = ((TextView)findViewById(R.id.l_nameT)).getText().toString();
        String pin = ((TextView)findViewById(R.id.pinT)).getText().toString();

        if(uname.isEmpty()){
            uname = "*";
        }
        if(pwd.isEmpty()){
            pwd = "*";
        }
        if(rep_pwd.isEmpty()){
            rep_pwd = "*";
        }
        if(f_name.isEmpty()){
            f_name = "*";
        }
        if(l_name.isEmpty()){
            l_name = "*";
        }
        if(pin.isEmpty()){
            pin = "*";
        }

        TelephonyManager telman = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telman.getDeviceId();

        if(imei == null) {
            imei = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        if(regid == null){
            Toast.makeText(context, "GCM Error", Toast.LENGTH_LONG).show();
        }else {
            String data = uname + ":" + pwd + ":" + rep_pwd + ":" + f_name + ":" + l_name + ":" + pin + ":" + imei + ":" + regid;

            Service service = new Service(data, this, pin, uname, regid);
            service.execute();
        }
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
                parseAndSend();
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

}
