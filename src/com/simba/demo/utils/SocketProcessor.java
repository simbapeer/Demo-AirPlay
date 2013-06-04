package com.simba.demo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.LinkedList;

import android.util.Log;

public class SocketProcessor extends Thread {
    private static LinkedList<SocketProcessor> pool = new LinkedList<SocketProcessor>();
    private static final int capacity = 10;
    private static final String tag = "SocketProcessor";

    private Object mSockLock = new Object();
    private Socket mSock;
    private boolean running = false;

    public static boolean processSocket(Socket sock) {
        LOGD("Process Socket: " + sock);
        synchronized (pool) {

            if (pool.isEmpty()) {
                pool.addLast(new SocketProcessor(sock));
                return true;
            }
            SocketProcessor last = pool.getLast();

            if (last.isIdle()) {
                last = pool.removeLast();
                last.setSocket(sock);
                pool.addFirst(last);
                return true;
            }

            if (pool.size() >= capacity) {
                LOGE("BOOMB!!!!");
                return false;
            }
            pool.addLast(new SocketProcessor(sock));
            return true;
        }
    }

    public synchronized void start() {
        running = true;
        super.start();
    };

    private SocketProcessor(Socket sock) {
        mSock = sock;
        start();
    }

    public void setSocket(Socket sock) {
        synchronized (mSockLock) {
            mSock = sock;
            mSockLock.notify();
            // TODO 重设了一个Socket，在这个Socket上开展行动
        }
    }

    public boolean isIdle() {
        synchronized (mSockLock) {
            return mSock == null;
        }
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {

            synchronized (mSockLock) {
                if (mSock == null) {
                    LOGW("mSock == null, wait");
                    try {
                        mSockLock.wait();
                        LOGD("Work came, WTF...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        mSock.getInputStream()));
                int port = mSock.getLocalPort();
                while (!Thread.currentThread().isInterrupted() && !mSock.isClosed()) {
                    String msg = input.readLine();
                    if (msg != null) {
                        LOGD(port + " readMsg: [" + msg + "]");
                        // TODO
                    } else {
                        mSock.close();
                        LOGW("READ NULL, CLOSE SOCKET");
                        break;
                    }
                }
                if (Thread.currentThread().isInterrupted()) {
                    break;
                } else if (mSock.isClosed()) {
                    synchronized (mSockLock) {
                        mSock = null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void LOGW(String msg) {
        Log.w(tag, msg);
    }

    private static void LOGD(String msg) {
        Log.d(tag, msg);
    }

    public static void LOGE(String msg) {
        Log.e(tag, msg);
    }
}
