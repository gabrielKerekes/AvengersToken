package com.avengers.avengerstoken;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by GabrielK on 03-Nov-16.
 */

public class ServiceIp
{
    private static final String IP_SHARED_PREFS = "serviceIp";
    private static final String IP_SHARED_PREFS_KEY = "serviceIpKey";
    private static final String IP_DEFAULT_VALUE = "http://192.168.0.111:8080/testRest/rs/service";

    public static void SaveIp(Context context, String ip)
    {
        SharedPreferences sp = context.getSharedPreferences(IP_SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putString(IP_SHARED_PREFS_KEY, ip);
        spEditor.commit();
    }

    public static String GetIp(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(IP_SHARED_PREFS, Context.MODE_PRIVATE);
        return sp.getString(IP_SHARED_PREFS_KEY, IP_DEFAULT_VALUE);
    }
}
