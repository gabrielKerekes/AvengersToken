package com.avengers.avengerstoken;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class PasswordWrite extends AppCompatActivity {

    EditText pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pass_write);

       // toGridCard = (Button) findViewById(R.id.button);
        //toGridCard.setOnClickListener(ocltoGridCard);

        android.support.v7.app.ActionBar aBar = getSupportActionBar();
        aBar.setTitle("Set secret");
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2B3742")));

        pass = (EditText) findViewById(R.id.editText);
    }

    public  void setPassword(View view)
    {

        Intent data =new Intent();
        String password= String.valueOf(pass.getText());
        data.putExtra("password",password);
        setResult(RESULT_OK, data);
        finish();
    }


}
