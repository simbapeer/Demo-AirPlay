
package com.simba.demo.mDNS;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

import android.content.Context;
import android.util.Log;

import com.simba.demo.airplay.WiFiMng;
import com.simba.demo.airplay.srv.AirPlay;
import com.simba.demo.airplay.srv.RAOP;

public class JmDNSMng {

    public static final String tag = "JmDNSMng";
    private static JmDNSMng si = null;
    private static Object sLock = new Object();
    private Context mContext;
    private JmDNS mJmDNS = null;
    private ServiceThread mServiceThread;
    private SampleListener mSampleListener = new SampleListener();

    private JmDNSMng(Context context) {
        mContext = context;
        mServiceThread = new ServiceThread();
        mServiceThread.start();
    }

    public boolean registerService(ServiceInfo info) {
        if (mJmDNS == null) {
            LOGW("X registerService failed: JmDNS is NULL!");
            return false;
        } else {
            try {
                mJmDNS.registerService(info);
            } catch (IOException e) {
                e.printStackTrace();
                LOGW("X registerService failed!");
                return false;
            }
        }
        LOGI("V registerService succeed! ");
        return true;
    }

    public static JmDNSMng get(Context context) {
        synchronized (sLock) {
            if (si == null) {
                si = new JmDNSMng(context);
            }
            return si;
        }
    }

    private class ServiceThread extends Thread {
        @Override
        public void run() {
            try {
                if (WiFiMng.get(mContext).isWiFiEnabled()) {
                    mJmDNS = JmDNS.create();
                    mJmDNS.addServiceTypeListener(mSampleListener);
                    mJmDNS.addServiceListener(AirPlay.TYPE_TP, mSampleListener);
                    mJmDNS.addServiceListener(RAOP.TYPE_TP, mSampleListener);
                    Log.i(tag, ">>>>>>>>>>>>>>>>>> Create JmDNS Succeed!! <<<<<<<<<<<<<<<<<");
                } else {
                    Log.e(tag, "X <<<<<<<<<<<<<<<<< Create JmDNS Failed!! WiFi disabled! >>>>>>>>>>>>>>>>>>>>");
                }

            } catch (IOException e) {
                Log.e(tag, "X <<<<<<<<<<<<<<<<< Create JmDNS Failed!! >>>>>>>>>>>>>>>>>>>>");
                e.printStackTrace();
            }

            super.run();
        }
    }

    private static class SampleListener implements ServiceListener, ServiceTypeListener {
        /**
         * (non-Javadoc)
         * 
         * @see javax.jmdns.ServiceListener#serviceAdded(javax.jmdns.ServiceEvent)
         */
        @Override
        public void serviceAdded(ServiceEvent event) {
            LOGD("ADD: " + event.getDNS().getServiceInfo(event.getType(), event.getName()));
        }

        /**
         * (non-Javadoc)
         * 
         * @see javax.jmdns.ServiceListener#serviceRemoved(javax.jmdns.ServiceEvent)
         */
        @Override
        public void serviceRemoved(ServiceEvent event) {
            LOGD("REMOVE: " + event.getName());
        }

        /**
         * (non-Javadoc)
         * 
         * @see javax.jmdns.ServiceListener#serviceResolved(javax.jmdns.ServiceEvent)
         */
        @Override
        public void serviceResolved(ServiceEvent event) {
            LOGD("RESOLVED: " + event.getInfo());
        }

        /**
         * (non-Javadoc)
         * 
         * @see javax.jmdns.ServiceTypeListener#serviceTypeAdded(javax.jmdns.ServiceEvent
         *      )
         */
        @Override
        public void serviceTypeAdded(ServiceEvent event) {
            LOGD("TYPE: " + event.getType());
        }

        /**
         * (non-Javadoc)
         * 
         * @see javax.jmdns.ServiceTypeListener#subTypeForServiceTypeAdded(javax.
         *      jmdns.ServiceEvent)
         */
        @Override
        public void subTypeForServiceTypeAdded(ServiceEvent event) {
            LOGD("SUBTYPE: " + event.getType());
        }
    }

    private static void LOGW(String msg) {
        Log.w(tag, msg);
    }

    private static void LOGD(String msg) {
        Log.d(tag, msg);
    }

    public static void LOGI(String msg) {
        Log.i(tag, msg);
    }

    public static void LOGE(String msg) {
        Log.e(tag, msg);
    }
}
