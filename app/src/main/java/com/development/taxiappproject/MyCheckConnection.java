package com.development.taxiappproject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class MyCheckConnection {
    static boolean connected = false;

    public static boolean mCheckConnectivity(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
            Toast.makeText(context, "connection", Toast.LENGTH_SHORT).show();
        } else {
            connected = false;
            Toast.makeText(context, "No connection!", Toast.LENGTH_SHORT).show();
        }
        return connected;
    }
}
