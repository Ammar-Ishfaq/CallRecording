package com.ammar.callrecording;

import android.app.Application;

import com.koushikdutta.ion.Ion;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Ion.getDefault(this).getConscryptMiddleware().enable(false);


    }
}
