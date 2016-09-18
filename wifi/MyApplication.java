package com.co.wifi;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        if (instance == null)
            instance = this;

    }
    public static Context getInstance() {
        return instance;
    }
}
