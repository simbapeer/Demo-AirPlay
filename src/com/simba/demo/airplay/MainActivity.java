
package com.simba.demo.airplay;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Random;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.simba.demo.airplay.srv.AirPlay;
import com.simba.demo.utils.Utils;

public class MainActivity extends Activity {

    private static final String tag = null;
    private JmDNS mJmDNS = null;
    private WifiInfo mWifiInfo;
    private Random random = new Random();
    private final HashMap<String, String> values = new HashMap<String, String>();
    private AirPlay ap;
    private TestThread mTestThread;

    private class TestThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            testAirPort();
            super.run();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTestThread = new TestThread();
        mTestThread.start();
    }

    private void test1()
    {
        values.put("DvNm", "Android-" + random.nextInt(100000));
        values.put("RemV", "10000");
        values.put("DvTy", "iPod");
        values.put("RemN", "Remote");
        values.put("txtvers", "1");
        byte[] pair = new byte[8];
        random.nextBytes(pair);
        values.put("Pair", Utils.toHex(pair));
    }

    private void testAirPort1() {
        String protocol = "airplay";
        String tProtocol = "tcp";
        String domain = "local.";

        String REMOTE_TYPE = "_" + protocol + "." + "_" + tProtocol + "." + domain;

        byte[] name = new byte[5];
        random.nextBytes(name);
        String name_subfix = Utils.toHex(name);
        System.out.println("Requesting pairing for " + name_subfix);
        try {
            ServerSocket ss = new ServerSocket(0);
            int port = ss.getLocalPort();
            values.clear();
            values.put("deviceid", mWifiInfo.getMacAddress());
            values.put("features", "0x7");

            ServiceInfo airPlayService = ServiceInfo.create(REMOTE_TYPE, name_subfix + "@Pateo Simba's", port, 0, 0,
                    values);
            mJmDNS.registerService(airPlayService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testAirPort() {
        ap = AirPlay.get(mJmDNS, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private static void LOGE(String msg) {
        Log.e(tag, msg);
    }
}
