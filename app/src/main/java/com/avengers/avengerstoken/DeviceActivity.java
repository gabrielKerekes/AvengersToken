package com.avengers.avengerstoken;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avengers.Hotp.HotpGenerator;
import com.avengers.Hotp.OCRAhotpGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class DeviceActivity extends AppCompatActivity implements RecyclerViewClickListener{

    private View.OnClickListener callWSListener;
    private ListService service;
    public static List<Item> items;
    private RecyclerView rv;
    private RVAadapter adapter;
    String trans = null;
    private boolean confirmed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

        items = new ArrayList<>();

        rv = (RecyclerView)findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);

        service = new ListService(this,getApplicationContext());

        callWSListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTransaction(v);
            }
        };

        Button okButt = (Button)findViewById(R.id.okButt);
        okButt.setOnClickListener(callWSListener);

        android.support.v7.app.ActionBar aBar = getSupportActionBar();
        aBar.setTitle("Transactions");
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2B3742")));

        service.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device, menu);
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

    public void sendTransaction(View view){

        trans = ((TextView)findViewById(R.id.transText)).getText().toString();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DeviceActivity.this);

        alertDialog.setTitle("Confirm Transaction");

        alertDialog.setMessage(trans);

        alertDialog.setIcon(R.drawable.message);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                confirmed = true;

                sendMessage(trans);
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                confirmed = false;
            }
        });

        alertDialog.show();

    }

    public void sendMessage(String trans){

        String uname = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

        String str = Build.DEVICE + " " + android.os.Build.MODEL;

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei= telephonyManager.getDeviceId();

        if(imei == null) {
            imei = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        uname = sharedPreferences.getString("uname", "");

        String data = uname + "=" + trans + "#" + currentDateandTime + "#" + str + "#" + "1";

        TransService service = new TransService(new TransactionActivity(), this, TransService.SEND_T, data, imei);
        service.execute();

        ListService serviceL = new ListService(this, getApplicationContext());
        serviceL.execute();
    }

    public void fillData(List<Item> items){

        this.items = items;

        adapter = new RVAadapter(items,0, this);
        rv.setAdapter(adapter);
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        /*adapter = new RVAadapter(items, 0, this);
        rv.setAdapter(adapter);*/

        Intent details = new Intent(DeviceActivity.this, DetailsActivity.class);
        details.putExtra("item",items.get(position));
        startActivity(details);
    }
}
