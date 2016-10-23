package com.avengers.avengerstoken;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.avengers.Hotp.HotpGenerator;
import com.avengers.Hotp.OCRAhotpGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Robert on 5/4/2016.
 */
public class TransService extends AsyncTask<String,Void,String> {

    public static final int LOGIN = 1;
    public static final int GET_LIST = 2;
    public static final int SEND_T = 3;
    private int action;
    private String data;
    private int responseCode = -1;
    private Context context;
    private String imei;
    private TransactionActivity caller;

    public TransService(TransactionActivity caller,Context context, int action, String data, String imei){
        this.data = data;
        this.action = action;
        this.context = context;
        this.caller = caller;
        this.imei = imei;
    }

    @Override
    protected String doInBackground(String... params) {

        switch (action){
            case LOGIN:
                login();
                break;
            case GET_LIST:

                break;
            case SEND_T:
                send();
                break;
        }

        return null;
    }

    private void login(){
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

            URL url = new URL("https://147.175.98.16:8443/testRest/rs/service/postLoginDev");

            //read data
            urlConnection = (HttpsURLConnection)url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "text/plain");

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(data);
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

    private void send(){
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

            URL url = new URL("https://147.175.98.16:8443/testRest/rs/service/writeTransDev");

            //read data
            urlConnection = (HttpsURLConnection)url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "text/plain");

            OCRAhotpGenerator ocra = new OCRAhotpGenerator();
            String msg = String.format("%040x", new BigInteger(1, data.getBytes(/*YOUR_CHARSET?*/)));
            String ocraMsg = ocra.generateOCRA("OCRA-1:HOTP-SHA1-6:QN08", imei, msg);

            data+="="+ocraMsg;

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(data);
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

    @Override
    protected void onPostExecute(String result) {

        switch (action) {
            case LOGIN:
                switch (responseCode) {
                    case 201:
                        Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show();
                        caller.goToTransaction();
                        break;
                    case 417:
                        Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show();
                        break;
                    case 500:
                        Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case SEND_T:

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
