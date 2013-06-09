
package com.simba.demo.airplay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;

import com.simba.demo.mDNS.JmDNSMng;

public class App extends Application {
    private static App sInstance = null;
    private static final String tag = "AirPlay APP";
    private static Map<String, String> assetFileContent = new HashMap<String, String>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "onCreate!");
        sInstance = this;
        WiFiMng.get(this);
        JmDNSMng.get(this);
    }

    public static String getRespPlist(String assetFileName) {
        if (assetFileContent.containsKey(assetFileName)) {
            return assetFileContent.get(assetFileName);
        } else {
            String content = getAssetFileContent(assetFileName);
            if (content != null) {
                assetFileContent.put(assetFileName, content);
                return content;
            }
        }
        return "";
    }

    public static String getAssetFileContent(String fileName) {
        if (sInstance != null) {
            AssetManager assetManager = sInstance.getResources().getAssets();
            try {
                InputStream is = assetManager.open(fileName);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuffer sb = new StringBuffer("");
                String line = null;
                while (true) {
                    line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                    sb.append("\n");
                }
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
