package com.prm392.g5.labverse.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.prm392.g5.labverse.LabVerse;

public class SharePreferenceManager {
    private static final String PREF_NAME = "labverse_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private SharedPreferences prefs;

    private static SharePreferenceManager sharePreferenceManager;

    private SharePreferenceManager(){
        prefs = LabVerse.getInstance().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SharePreferenceManager getInstance() {
        if (sharePreferenceManager == null) { //first check
            synchronized (SharePreferenceManager.class) { //lock thread, only one can get into this
                if (sharePreferenceManager == null) { //double check
                    sharePreferenceManager = new SharePreferenceManager();
                }
            }
        }
        return sharePreferenceManager;
    }

    public void saveAccessToken(String accessToken) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public void clearAccessToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply();
    }


}
