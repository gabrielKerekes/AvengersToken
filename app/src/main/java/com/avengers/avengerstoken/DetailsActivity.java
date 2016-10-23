package com.avengers.avengerstoken;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Item item = (Item)getIntent().getExtras().getSerializable("item");

        String[] datetime = item.dateTime.split("_");

        TextView text = (TextView)findViewById(R.id.textT);
        text.setText(item.transText);
        TextView date = (TextView)findViewById(R.id.dateT);
        date.setText(datetime[0]);
        TextView time = (TextView)findViewById(R.id.timeT);
        time.setText(datetime[1]);
        TextView from = (TextView)findViewById(R.id.fromT);
        from.setText(item.from);

        android.support.v7.app.ActionBar aBar = getSupportActionBar();
        aBar.setTitle("Transaction details");
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2B3742")));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_details, menu);

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
}
