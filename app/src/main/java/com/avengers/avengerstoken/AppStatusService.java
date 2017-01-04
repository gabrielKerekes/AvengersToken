package com.avengers.avengerstoken;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Robert on 5/18/2016.
 */
public class AppStatusService extends AsyncTask<String,Void,String> {

    private MainActivity caller;
    private String uname;

    public AppStatusService(MainActivity caller, String uname){
        this.caller = caller;
        this.uname = uname;
    }

    @Override
    protected String doInBackground(String... params) {

        Date today = new Date();
        String curr_ts = Long.toString(today.getTime());

        String resp = null;

        //HttpsURLConnection urlConnection = null;
        HttpURLConnection urlConnection = null;
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
            //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            //HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());

            //URL url = new URL("https://147.175.98.16:8443/testRest/rs/service/getTs?user="+uname+"&ts="+curr_ts);
            URL url = new URL(ServiceIp.GetIp(caller) + "/getTs?user="+uname+"&ts="+curr_ts);

            urlConnection = (HttpURLConnection)url.openConnection();
            InputStream in = urlConnection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));

            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            resp = total.toString();

        } catch (Exception e) {
            resp = "err";
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return resp;
    }

    @Override
    protected void onPostExecute(String result) {
        caller.setUp(result);
    }

    public class NullHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            Log.i("RestUtilImpl", "Approving certificate for " + hostname);
            return true;
        }

    }
}
