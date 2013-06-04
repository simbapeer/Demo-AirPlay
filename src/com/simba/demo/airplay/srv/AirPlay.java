
package com.simba.demo.airplay.srv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.util.Log;

import com.simba.demo.airplay.WiFiMng;
import com.simba.demo.airplay.lab.SimbaHttpServer;
import com.simba.demo.mDNS.JmDNSMng;
import com.simba.demo.utils.SocketDemo;
import com.simba.demo.utils.SocketProcessor;
import com.simba.demo.utils.Utils;

/**
 * @author simba
 */
public class AirPlay {

    private static final String tag = "AirPlaySV";
    public static final String TYPE_TP = "_airplay._tcp.local.";

    private static final Map<String, String> txtRd = new HashMap<String, String>();

    private static String TR_KEY_DEVICE_ID = "deviceid";
    private static String TR_KEY_FEATURES = "features";
    private static String TR_KEY_SRV_VERSION = "srcvers";
    private static String TR_KEY_APPLE_DEVICE_MODEL = "model";
    /** server is password protected */
    private static String TR_KEY_PASSWD_PROTECTED = "pw";

    /** video supported */
    private static final int FEATURE_Video = 1;
    /** photo supported */
    private static final int FEATURE_Photo = 1 << 1;
    /** video protected with FairPlay DRM */
    private static final int FEATURE_VideoFairPlay = 1 << 2;
    /** volume control supported for videos */
    private static final int FEATURE_VideoVolumeControl = 1 << 3;
    /** http live streaming supported */
    private static final int FEATURE_VideoHTTPLiveStreams = 1 << 4;
    /** slideshow supported */
    private static final int FEATURE_Slideshow = 1 << 5;
    private static final int FEATURE_BYTE6 = 1 << 6; // FIXME Unknown....
    /** mirroring supported */
    private static final int FEATURE_Screen = 1 << 7;
    /** screen rotation supported */
    private static final int FEATURE_ScreenRotate = 1 << 8;
    /** audio supported */
    private static final int FEATURE_Audio = 1 << 9;
    /** audio packet redundancy supported */
    private static final int FEATURE_AudioRedundant = 1 << 11;
    /** FairPlay secure auth supported */
    private static final int FEATURE_FPSAPv2pt5_AES_GCM = 1 << 12;
    private static final int FEATURE_PhotoCaching = 1 << 13;
    private ServiceInfo mServiceInfo = null;
    private static AirPlay sInstance = null;
    private static Object sLock = new Object();
    private WifiInfo mWifiInfo; // FIXME get Wifi Info

    private String srvName;
    private String mac;
    private int mPort;
    private Map<String, String> mProps;
    private ServerSocket mSS = null;

    /** FIXME 待修正 */
    private static final int SUPPORTED_FEATURES = FEATURE_Video |
            FEATURE_Photo |
            FEATURE_VideoFairPlay |
            FEATURE_VideoHTTPLiveStreams |
            FEATURE_Slideshow |
            FEATURE_BYTE6 |
            FEATURE_Screen |
            FEATURE_ScreenRotate |
            FEATURE_AudioRedundant |
            FEATURE_FPSAPv2pt5_AES_GCM |
            FEATURE_PhotoCaching;

    static {
        txtRd.put(TR_KEY_FEATURES, "0x" + Integer.toHexString(SUPPORTED_FEATURES));
        // txtRd.put(TR_KEY_PASSWD_PROTECTED, "1"); //FIXME ???
        txtRd.put(TR_KEY_SRV_VERSION, "130.14");
        // txtRd.put(TR_KEY_APPLE_DEVICE_MODEL, "AppleTV2,1");// device model
        // FIXME 加上Device Model后识别不到了
    }

    public static AirPlay get(JmDNS mDNSService, Context context) {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new AirPlay(mDNSService, context);
            }
            return sInstance;
        }
    }

    public static Map<String, String> getTxtRd() {
        return txtRd;
    }

    @Override
    public String toString() {
        if (mServiceInfo != null) {
            return mServiceInfo.toString();
        }
        return super.toString();
    }

    private Context mContext;
    private Worker mWorker = null;

    private AirPlay(JmDNS mDNSService, Context context) {
        mContext = context;
        Random random = new Random();
        byte[] name = new byte[5];
        random.nextBytes(name);
        String name_prefix = Utils.toHex(name);
        System.out.println("Requesting pairing for " + name_prefix);
        this.srvName = name_prefix + "@AS21 Simba's";

        try {
            mSS = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mSS == null) {
            LOGE("Create Socket Failed!");
            return;
        }
        this.mac = WiFiMng.get(mContext).getMAC();
        if (mac != null) {
            txtRd.put(TR_KEY_DEVICE_ID, this.mac);
        }
        this.mPort = mSS.getLocalPort();
        mProps = txtRd;
        mServiceInfo = ServiceInfo.create(TYPE_TP, srvName, "", mPort, 0, 0, false, mProps);
        LOGW("mDNSService Created!");
        LOGD(toString());

        new SimbaHttpServer(7000).start();
        new SimbaHttpServer(7100).start();
//        new SocketDemo(7000).start();
//        new SocketDemo(7100).start();
        registerService();
        mWorker = new Worker();
        mWorker.start();
    }

    private void registerService() {
        int tryTimes = 1;
        while (!JmDNSMng.get(mContext).registerService(mServiceInfo)) {
            LOGW("Register service failed");
            if (tryTimes >= 10) {
                LOGW("Maximum times tried... Won't Try Again!");
                return;
            }
            try {
                LOGD("Sleep 1000ms");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            LOGD("Try Again...");
            tryTimes++;
        }
    }

    private class Worker extends Thread {
        private boolean mRunning = false;

        @Override
        public synchronized void start() {
            mRunning = true;
            super.start();
        }

        @Override
        public void run() {
            while (mRunning && !Thread.currentThread().isInterrupted()) {

                try {
                    Socket sock = mSS.accept();
                    SocketProcessor.processSocket(sock);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
