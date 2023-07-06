package com.samtechs.thehinjuapp.apputils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnectivity {

    private final boolean isConnected;
    private final boolean isMetered;

    public InternetConnectivity(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        isMetered = cm.isActiveNetworkMetered();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isMetered() { return isMetered; }

}
