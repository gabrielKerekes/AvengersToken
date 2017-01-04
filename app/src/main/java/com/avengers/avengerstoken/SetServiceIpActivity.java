package com.avengers.avengerstoken;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SetServiceIpActivity extends AppCompatActivity
{
    EditText serviceIpEditText;
    Button saveIpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_service_ip);

        serviceIpEditText = (EditText) findViewById(R.id.serviceIpEditText);
        serviceIpEditText.setText(ServiceIp.GetIp(this));

        saveIpButton = (Button) findViewById(R.id.saveIpButton);
    }

    public void saveIp(View view)
    {
        ServiceIp.SaveIp(this, serviceIpEditText.getText().toString());

        finish();
    }
}
