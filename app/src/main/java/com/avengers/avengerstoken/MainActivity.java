package com.avengers.avengerstoken;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

    String initString= null;
    SharedPreferences sharedPreferences;
    Button devSynchButt;
    Button regButt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        devSynchButt = (Button) findViewById(R.id.onlineToken);
        regButt = (Button) findViewById(R.id.buttDevOnly);

        sharedPreferences = getApplicationContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        String reg_usr = sharedPreferences.getString("uname",null);

        if(reg_usr!=null){
            devSynchButt.setVisibility(View.INVISIBLE);
            regButt.setVisibility(View.INVISIBLE);
        }else {

            SharedPreferences settings = getApplicationContext().getSharedPreferences("qrresult", 0);
            initString = settings.getString("qrresult", null);

            if (initString != null){
                devSynchButt.setVisibility(View.INVISIBLE);
                regButt.setVisibility(View.INVISIBLE);
            }
        }

        new AppStatusService(this,reg_usr).execute();

        android.support.v7.app.ActionBar aBar = getSupportActionBar();
        aBar.setTitle("Token");
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2B3742")));
    }

    @Override
    protected void onResume(){
        super.onResume();

        String reg_usr = sharedPreferences.getString("uname", null);

        if(reg_usr!=null){
            devSynchButt.setVisibility(View.INVISIBLE);
            regButt.setVisibility(View.INVISIBLE);
        }else {

            SharedPreferences settings = getApplicationContext().getSharedPreferences("qrresult", 0);
            initString = settings.getString("qrresult", null);

            if (initString != null){
                devSynchButt.setVisibility(View.INVISIBLE);
                regButt.setVisibility(View.INVISIBLE);
            }
        }
    }

    //button pomocou ktoreho sa spusti grig karta
    public void startGridCardActivity(View view)
    {
            Intent gridcardintent = new Intent(MainActivity.this,GridCard.class);
            MainActivity.this.startActivity(gridcardintent);
    }

    //button pomocou ktoreho sa spusti oath
    public void startOfflineTokenActivity(View view)
    {
            Intent offlineToken = new Intent(MainActivity.this,OfflineToken.class);
            MainActivity.this.startActivity(offlineToken);
    }

    public void startOnlineTokenActivity(View view)
    {
        if(isOnline()) {
            Intent onlineToken = new Intent(MainActivity.this, SynchronizeActivity.class);
            onlineToken.putExtra("initString",initString);
            MainActivity.this.startActivity(onlineToken);
        }else{
            Toast.makeText(getApplicationContext(), "Device is Offline !", Toast.LENGTH_LONG).show();
        }
    }

    public void startDeviceActivity(View view)
    {
        if(isOnline()) {
            Intent deviceActivity = new Intent(MainActivity.this, RegActivity.class);
            MainActivity.this.startActivity(deviceActivity);
        }else{
            Toast.makeText(getApplicationContext(), "Device is Offline !", Toast.LENGTH_LONG).show();
        }
    }

    public void startTransactionActivity(View view)
    {
        if(isOnline()) {
            Intent deviceActivity = new Intent(MainActivity.this, TransactionActivity.class);
            MainActivity.this.startActivity(deviceActivity);
        }else{
            Toast.makeText(getApplicationContext(), "Device is Offline !", Toast.LENGTH_LONG).show();
        }

    }

    public void setUp(String res){

        String reg_usr = sharedPreferences.getString("uname",null);

        if(res.equals("out")){
            Toast.makeText(getApplicationContext(), "Connection Error!", Toast.LENGTH_LONG).show();
            devSynchButt.setVisibility(View.VISIBLE);
        }else if(res.equals("err")){
            Toast.makeText(getApplicationContext(), "Not registered application!", Toast.LENGTH_LONG).show();
        }

    }

    //button na ukoncenie programu
    public void exit(View view)
    {
        finish();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

	
}
