
package com.simba.demo.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class SocketDemo extends Thread {
    private static final String tag = "SocketDemo";
    private ServerSocket mSS = null;
    private int port = -1;

    public SocketDemo(int port) {
        this.port = port;
    }

    @Override
    public void run() {

        try {
            mSS = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(tag, "Create ServerSocket failed!!! port: " + port);
            return;
        }
        Log.i(tag, "SocketDemo running, port: " + port);
        while (!Thread.currentThread().isInterrupted()) {

            try {
                Socket sock = mSS.accept();
                SocketProcessor.processSocket(sock);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
