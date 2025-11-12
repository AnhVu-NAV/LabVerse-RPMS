package com.prm392.g5.labverse.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import androidx.lifecycle.LiveData;

@SuppressLint("MissingPermission")
public class NetworkStatus extends LiveData<Boolean> {

    private final ConnectivityManager cm;
    private final ConnectivityManager.NetworkCallback cb = new ConnectivityManager.NetworkCallback() {
        @Override public void onAvailable(Network network) { postValue(true); }
        @Override public void onLost(Network network) { postValue(hasInternet()); }
    };

    public NetworkStatus(Context ctx) {
        cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        postValue(hasInternet());
    }

    private boolean hasInternet() {
        Network n = cm.getActiveNetwork();
        if (n == null) return false;
        NetworkCapabilities nc = cm.getNetworkCapabilities(n);
        return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    @Override protected void onActive() {
        cm.registerDefaultNetworkCallback(cb);
    }

    @Override protected void onInactive() {
        cm.unregisterNetworkCallback(cb);
    }
}
