
package com.simba.demo.airplay.srv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.jmdns.ServiceInfo;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.simba.demo.airplay.WiFiMng;
import com.simba.demo.airplay.raop.RAOPSocket;
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

    private static final String TR_KEY_RHD = "rhd";
    private static final String TR_KEY_DEVICE_ID = "deviceid";
    private static final String TR_KEY_FEATURES = "features";
    private static final String TR_KEY_SRV_VERSION = "srcvers";
    private static final String TR_KEY_APPLE_DEVICE_MODEL = "model";
    /** server is password protected */
    private static final String TR_KEY_PASSWD_PROTECTED = "pw";
    private static final String TR_KEY_VV = "vv";
    private static final String TR_KEY_FLAGS = "flags";
    public static final String VERSION = "160.10";
    public static final String FLAGS = "0x4";

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
    private ServiceInfo mAirPlayServiceInfo = null;
    private ServiceInfo mRAOPServiceInfo = null;
    private static AirPlay sInstance = null;
    private static Object sLock = new Object();

    private String srvName;
    private String mac;
    private int mPort;
    private Map<String, String> mProps;
    private ServerSocket mSS = null;
    private MirroringDaemon mMirroringDaemon;

    /** FIXME 待修正 */
    private static final int SUPPORTED_FEATURES = FEATURE_Video |
            FEATURE_Photo |
            FEATURE_VideoHTTPLiveStreams |
            FEATURE_Slideshow |
            FEATURE_Screen |
            FEATURE_ScreenRotate |
            FEATURE_AudioRedundant |
            FEATURE_PhotoCaching;
    public static final String SUPPORATED_FEATURES_STR = "0x100029FF";
    public static final String APPLE_DEVICE_MODEL = "AppleTV2,1";

    static {
        // txtRd.put(TR_KEY_FEATURES, "0x" +
        // Integer.toHexString(SUPPORTED_FEATURES));
        // txtRd.put(TR_KEY_RHD, "1.9.0");
        // txtRd.put(TR_KEY_PASSWD_PROTECTED, "1"); //FIXME ???
        txtRd.put(TR_KEY_FEATURES, SUPPORATED_FEATURES_STR);
        txtRd.put(TR_KEY_FLAGS, FLAGS);
        txtRd.put(TR_KEY_VV, "1");
        txtRd.put(TR_KEY_SRV_VERSION, "160.10");
        txtRd.put(TR_KEY_APPLE_DEVICE_MODEL, APPLE_DEVICE_MODEL);// device model
        // FIXME 加上Device Model后识别不到了
    }

    public static AirPlay get(Context context) {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new AirPlay(context);
            }
            return sInstance;
        }
    }

    public static Map<String, String> getTxtRd() {
        return txtRd;
    }

    @Override
    public String toString() {
        if (mAirPlayServiceInfo != null) {
            return mAirPlayServiceInfo.toString();
        }
        return super.toString();
    }

    private Context mContext;
    private Worker mWorker = null;

    public void start() {
        new SocketDemo(7000).start();

        new RAOPSocket(RAOP.RAOP_SERVICE_PORT, mac).start();
        registerRAOPService();

        mMirroringDaemon.start();
        registerAirPlayService();
        mWorker.start();
    }

    private AirPlay(Context context) {
        mContext = context;

        try {
            mSS = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mSS == null) {
            LOGE("Create Socket Failed!");
            return;
        }
        this.mac = WiFiMng.get(mContext).getMAC().toUpperCase();
        if (mac != null) {
            txtRd.put(TR_KEY_DEVICE_ID, this.mac);
        }
        this.srvName = getRandomName("airp");
        this.mPort = mSS.getLocalPort();
        mProps = txtRd;
        mAirPlayServiceInfo = ServiceInfo.create(TYPE_TP, srvName, "", mPort, 0, 0, false, mProps);
        LOGW("mServiceInfo Created! : " + mAirPlayServiceInfo);
        LOGD(toString());
        mMirroringDaemon = new MirroringDaemon();
        mWorker = new Worker();
        start();
    }

    private void registerAirPlayService() {
        int tryTimes = 1;
        while (!JmDNSMng.get(mContext).registerService(mAirPlayServiceInfo)) {
            LOGW("Register AirPlay service failed");
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

    private void registerRAOPService() {
        int tryTimes = 1;
        ServiceInfo raopSvc = RAOP.get(getRandomName("raop"));
        mRAOPServiceInfo = raopSvc;
        while (!JmDNSMng.get(mContext).registerService(raopSvc)) {
            LOGW("Register RAOP service failed");
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

    public void destroy() {
        LOGD("Unregister AirPlay and RAOP service! ");
        JmDNSMng.get(mContext).unregisterAllServices();
    }

    private String getRandomName(String pre) {
        Random random = new Random();
        byte[] name = new byte[2];
        random.nextBytes(name);
        String name_prefix = Utils.toHex(name);
        System.out.println("Requesting pairing for " + name_prefix);
        String ret = this.mac + "@" + pre + "_" + name_prefix + "_" + Build.BRAND;
        return ret;
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
                    LOGI(">>>>>>>>>>>>>>>>>>>> AirPlay Service Started on port: " + mPort);
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

    public static void LOGI(String msg) {
        Log.i(tag, msg);
    }
}
