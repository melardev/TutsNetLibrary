package com.melardev.tutsnetlibrary;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by melardev on 6/20/2017.
 */

public class AppTutsNetLibrary extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
