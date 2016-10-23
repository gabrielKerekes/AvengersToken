package com.avengers.avengerstoken;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;

/**
 * Created by Robert on 4/11/2016.
 */
public class Service extends AsyncTask<String,Void,String> {

    private String data;
    private String err_mess;
    private String pin;
    private String uname;
    private String ret_data;
    private String regid;
    private Context context;
    private int responseCode = -1;

    @Override
    protected String doInBackground(String... params) {
    //    callTest();
        sendReg();
        return "sup";
    }

    public Service(String data, Context context, String pin, String uname, String regid){
        this.data = data;
        this.pin = pin;
        this.context = context;
        this.uname = uname;
        this.regid = regid;
    }

    private void sendReg(){
        HttpsURLConnection urlConnection = null;

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

            URL url = new URL("https://147.175.98.16:8443/testRest/rs/service/regDevice?data="+data);


            urlConnection = (HttpsURLConnection)url.openConnection();
            InputStream in = urlConnection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));

            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            String message = total.toString();

            JSONObject json = new JSONObject(message);
            String result = json.getString("response");

            switch (result){
                case "suc":
                    responseCode = 201;
                    String grid = json.getString("data");

                    SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("grid",grid);
                    editor.putString("pin",pin);
                    editor.putString("uname",uname);
                    editor.commit();

                    SharedPreferences gcm_pref = context.getSharedPreferences(Globals.PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor gcm_edit = gcm_pref.edit();
                    gcm_edit.putString(Globals.PREFS_PROPERTY_REG_ID, regid);
                    gcm_edit.commit();
                    break;
                case "fail":
                    responseCode = 417;
                    break;
                case "err":
                    responseCode = 409;
                    err_mess = json.getString("data");
                    break;
                case "exc":
                    responseCode = 500;
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        evaluateResponse(responseCode);
    }

    private void evaluateResponse(int resp){

        switch (resp) {
            case -1:
                Toast.makeText(context, "Nespecifikovana chyba", Toast.LENGTH_LONG).show();
                break;
            case 201:
                Toast.makeText(context, "Registracia uspesna", Toast.LENGTH_LONG).show();
                break;
            case 417:
                Toast.makeText(context, "Pouzivatelkse meno uz existuje", Toast.LENGTH_LONG).show();
                break;
            case 409:
                err_mess = err_mess.replace("-","\n");
                Toast.makeText(context, err_mess, Toast.LENGTH_LONG).show();
                break;
            case 500:
                Toast.makeText(context, "Chyba servera", Toast.LENGTH_LONG).show();
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
