
package com.simba.demo.airplay;

import com.simba.demo.mDNS.JmDNSMng;

import android.app.Application;
import android.util.Log;

public class App extends Application {
    private static final String tag = "AirPlay APP";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "onCreate!");
        WiFiMng.get(this);
        JmDNSMng.get(this);
    }
}
