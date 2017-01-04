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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;


public class GridCard extends AppCompatActivity {


    private static final String TAG = GridCard.class.getName(); // pri vypisovani chybovych hlasok
    private String GRIDCARD= null; //cesta k suboru gridcard
    final int ACTIVITY_CHOOSE_FILE = 0; //hovori ci je vybraty subor
    final int SETPASS=10; //hovori ci je nastavene heslo
    private boolean MODE_PREF = false;
    private String enc_grid="";
    private SharedPreferences sharedPreferences;

    //tlacidla na zobrazenie a vyber tokena
    Button viewGridCard;
    Button plNumber;
    Button plLetter;
    Button minNumber;
    Button minLetter;
    Button vToken;

    //text view
    TextView viewtoken; // zobrazenie jedneho tokena
    TextView viewnumber;// zobrazenie ciselnej pozicie
    TextView viewletter;// zobrazenie abecednej pozicie
    TextView viewtokens;// zobrazenie vsetkych tokenov


    private FileManager file;


    // oh toto by sa zislo zmenit

    int charValue= 65;// counter pismen
    Integer number=1;// counter cisiel

    int rowsNum=3;  //maximalny pocet riadkov
    static int columnsNum=6;//maximalny pocet stlpcov



    int checkclick = 2; //urcuje preklikavanie buttonu

    private String fileContent; //nacitany obsah grid karty

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //funkcia nacita xml, skusi otvorit subor config.txt ake nexistuje nastavi obsah suboru na hlasku aby uzivatel nastavil subor ak existuje nacita a popyta heslo

        super.onCreate(savedInstanceState);

        String toastmess = "";

        setContentView(R.layout.activity_gridcard);

        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        file=new FileManager(getApplicationContext());


        GRIDCARD = sharedPreferences.getString("grid", "");
        if(GRIDCARD.equals("")){
            fileContent = "Register first!";
            toastmess = "Register first!";
        }
        else{
            MODE_PREF = true;
            enc_grid = GRIDCARD;
            if((sharedPreferences.getString("pwd","").equals(""))){
                fileContent = "Set password";
                toastmess = "Set password";
            }
            else {
                try {
                    file.setFilepass(sharedPreferences.getString("pwd",""));
                    file.decryptFile(enc_grid, sharedPreferences.getString("pwd",""));
                    fileContent = file.getDecryptfile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        if(!toastmess.equals("")) Toast.makeText(getApplicationContext(),toastmess,Toast.LENGTH_SHORT).show();

        viewGridCard = (Button) findViewById(R.id.viewgridcard);
        plNumber = (Button) findViewById(R.id.plusNumber);
        plLetter = (Button) findViewById(R.id.plusLetter);
        minNumber = (Button) findViewById(R.id.minusNumber);
        minLetter = (Button) findViewById(R.id.minusLetter);
        vToken = (Button) findViewById(R.id.viewToken);

        viewtoken = (TextView) findViewById(R.id.textViewToken);
        viewtokens = (TextView) findViewById(R.id.viewTokens);
        viewnumber = (TextView) findViewById(R.id.viewNumber);
        viewletter = (TextView) findViewById(R.id.viewLetter);

        viewGridCard.setOnClickListener(oclviewGridCard);
        plNumber.setOnClickListener(oclplusNumber);
        plLetter.setOnClickListener(oclplusLetter);
        minNumber.setOnClickListener(oclminusNumber);
        minLetter.setOnClickListener(oclminusLetter);
        vToken.setOnClickListener(oclViewToken);

        android.support.v7.app.ActionBar aBar = getSupportActionBar();
        aBar.setTitle("Grid Card");
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2B3742")));
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);



	    return true;
	}

    //ak bol vybraty Set password
    //spusti aktivitu na zadanie hesla
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:
                Intent intent1 = new Intent(GridCard.this,PasswordWrite.class);

                GridCard.this.startActivityForResult(intent1,SETPASS);

        }
        return true;
    }

    //prijme vysledok z aktivit
    //ak nacitanie cesty prebehlo v poriadku tak sa cesta ulozi do GRIDCARD a nacita sa subor
    //nasledne sa cesta ulozi

    //ak bol vybraty set password heslo sa nastavi a subor sa desifruje
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case SETPASS:
                if(resultCode==RESULT_OK)
                {
                    try {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("pwd",data.getStringExtra("password"));
                        editor.commit();

                        file.setFilepass(data.getStringExtra("password"));
                        file.decryptFile(enc_grid,file.getFilepass());
                        fileContent = file.getDecryptfile();
                    } catch (Exception e) {
                        Toast.makeText(this, "Decryption Error",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                break;
        }
    }

        //listenery tlacidiel

        //tlacidlo + cislo
        View.OnClickListener oclplusNumber= new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                number+=1;
                if(number>=columnsNum)number=1;
                viewnumber.setText(number.toString());
            }
        };
        //tlacidlo - cislo
        View.OnClickListener oclminusNumber= new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            number-=1;
            if(number<1)number=columnsNum-1;
            viewnumber.setText(number.toString());
            }
        };

        //tlacidlo View Grid Code ak subor je nacitany a je aj zadane heslo zobrazi prislusnu hodnotu inac chybovu hlasku
        View.OnClickListener oclViewToken= new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                    if (file.getDecryptfile()!=null) {
                        if (file.getFilepass() != null)
                            viewtoken.setText(positionGrid(number, charValue, fileContent));
                        else viewtoken.setText("Set password!");
                    } else viewtoken.setText("Set password!");
            }

        };


        //tlacidlo + pismeno
        View.OnClickListener oclplusLetter= new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(charValue!=(64+rowsNum-1))charValue+=1;
                else charValue=65;


                viewletter.setText(String.valueOf((char)charValue));
            }
        };


        //tlacidlo - pismeno
        View.OnClickListener oclminusLetter= new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(charValue!=65)charValue+=-1;
                else charValue=charValue%25+49+rowsNum-1;
                viewletter.setText(String.valueOf((char)charValue));
            }
        };


        //tlacidlo View ALL GRID CODES zobrazuje vsetky hodnoty ak je subor naictany inac prislusnu hlasku
        View.OnClickListener oclviewGridCard= new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(checkclick%2==0){
                    //	scale.setVisibility(v.VISIBLE);
                    //tuZobraz.setText(possitionGrid(3, "B",readFile()));
                    viewtokens.setText(fileContent);
                    //viewtokens.setText(GRIDCARD);
                    viewGridCard.setText("HIDE GRID CODES");

                   }
                 else
                 {
                    //	scale.setVisibility(v.GONE);
                    //tuZobraz.setGravity(Gravity.CENTER);
                 viewtokens.setText("");
                 viewGridCard.setText("VIEW ALL GRID CODES");


            }
            checkclick+=1;
        }
    };

    //tlacidlo quit na vratenie sa na zaciatok app
    public void quit(View view)
    {
        finish();
    }


    private String positionGrid(int number, int letter,String gridcard)
    //funkcia na zaklade vstupnych premmenych vytiahne zo stringu pozadovany retazec
    {
        String StringGrid="";

        String[] columns = gridcard.split(" ");
        int lett=(letter-65);

        int a=1;

                for (String column : columns) {

                    if ((a %columnsNum) == number){
                        return columns[number+lett*(columnsNum-1)-1];
                    }

                    a++;
                }


        StringGrid="Invalid position";
        return StringGrid;
    }
}
