package com.avengers.avengerstoken;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Robert on 5/4/2016.
 */
public class ListService extends AsyncTask<String,Void,List<Item>> {

    private DeviceActivity caller;
    private int responseCode = -1;
    Context context;

    public ListService(DeviceActivity caller, Context context){
        this.caller = caller;
        this.context = context;
    }

    private String stringBuild(InputStreamReader stream) throws IOException {
        BufferedReader reader = new BufferedReader(stream);
        StringBuilder stringBuilder = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null)
        {
            stringBuilder.append(line + "\n");
        }

        reader.close();

        return stringBuilder.toString();
    }

    @Override
    protected List<Item> doInBackground(String... params) {

        List<Item> items = new ArrayList<Item>();
        Item item;
        String data = "";

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

            SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE);
            String uname = sharedPreferences.getString("uname","");

            //URL url = new URL("https://147.175.98.16:8443/testRest/rs/service/getTrans?user="+uname);
            URL url = new URL(ServiceIp.GetIp(context) + "/getTrans?user="+uname);

            //read data
            urlConnection = (HttpURLConnection)url.openConnection();
            InputStream in = urlConnection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));

            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }

            data = total.toString();

            responseCode = urlConnection.getResponseCode();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        if(!data.equals("err")) {

            String[] trans = data.split("=");

            for (String str :
                    trans) {
                items.add(new Item(str));
            }

            ObjectComparator comparator = new ObjectComparator();
            Collections.sort(items, comparator);
        }

        return items;
    }

    @Override
    protected void onPostExecute(List<Item> items) {
        caller.fillData(items);
    }

    public class NullHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            Log.i("RestUtilImpl", "Approving certificate for " + hostname);
            return true;
        }

    }

    public class ObjectComparator implements Comparator<Item> {

        public int compare(Item obj1, Item obj2) {
            return obj2.dateTime.compareTo(obj1.dateTime);
        }

    }

}
