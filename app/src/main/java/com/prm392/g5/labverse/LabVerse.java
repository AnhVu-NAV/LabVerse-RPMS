package com.prm392.g5.labverse;

import android.app.Application;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

public class LabVerse extends Application {
    //this class would be created before Activity/Service
    //used to keep global variance, singleton, init library
    private static LabVerse instance; // to get context in class in which the context is not available
    //cho cái SharePreferenceManager chẳng hạn

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this; //the Application, aka application context
        PDFBoxResourceLoader.init(this);
    }

    public static LabVerse getInstance(){
        return instance;
    }
}
