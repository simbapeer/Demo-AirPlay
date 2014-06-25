
package com.simba.demo.airplay.raop;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.simba.demo.utils.Utils;

import android.util.Log;

public class RAOPSocket extends Thread {
    private static final String tag = "RAOPSocket";
    private ServerSocket mSS = null;
    private int port = -1;
    private String mac = null;

    public RAOPSocket(int port, String mac) {
        this.port = port;
        this.mac = mac.replace(":", "");
    }

    @Override
    public void run() {

        try {
            mSS = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(tag, "Create RAOPSocket failed!!! port: " + port);
            return;
        }
        Log.w(tag, "RAOPSocket running, port: " + port);
        while (!Thread.currentThread().isInterrupted()) {

            try {
                Socket sock = mSS.accept();
                new RTSPResponder(Utils.hexStringToBytes(mac), sock).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
