
package com.simba.demo.airplay;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * TODO 监视WiFi状态，并向系统内其他模块发出消息
 * 
 * @author simba
 */
public class WiFiMng {
    private static final String tag = "WiFiMng";
    private static WiFiMng si = null;
    private static Object sLock = new Object();
    private Context mContext;
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private IntentFilter mWifiStateFilter;
    private String mWifiSupplicantState = "";
    private String mWifiScanList = "";

    private WiFiMng(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo(); // Mybe null here.
        WifiManager.MulticastLock multicastLock = mWifiManager.createMulticastLock("simba-mDNS");
        multicastLock.acquire();

        mWifiStateFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mWifiStateFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWifiStateFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mWifiStateFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mWifiStateFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mWifiStateFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        mContext.registerReceiver(mWifiStateReceiver, mWifiStateFilter);
    }

    public WifiInfo getWiFiInfo() {
        return mWifiInfo;
    }
    
    public boolean isWiFiEnabled(){
        return mWifiManager.isWifiEnabled();
    }

    private final BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
            {
                handleWifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
            {
                handleNetworkStateChanged((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO));
            } else if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {
                handleScanResultsAvailable();
            } else if (intent.getAction().equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
            {
                /* TODO: handle supplicant connection change later */
            } else if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
            {
                handleSupplicantStateChanged((SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE),
                        intent.hasExtra(WifiManager.EXTRA_SUPPLICANT_ERROR),
                        intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0));
            } else if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION))
            {
                handleWifiSignalChanged(intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0));
            } else if (intent.getAction().equals(WifiManager.NETWORK_IDS_CHANGED_ACTION))
            {
                /* TODO: handle network id change info later */
            } else
            {
                Log.e(tag, "Received an unknown Wifi Intent");
            }
        }
    };

    private void setSupplicantStateText(SupplicantState supplicantState)
    {
        mWifiSupplicantState = supplicantState.name();
    }

    private void handleScanResultsAvailable()
    {
        List<ScanResult> list = mWifiManager.getScanResults();

        StringBuffer scanList = new StringBuffer();
        if (list != null)
        {
            for (int i = list.size() - 1; i >= 0; i--)
            {
                final ScanResult scanResult = list.get(i);

                if (scanResult == null)
                {
                    continue;
                }

                if (TextUtils.isEmpty(scanResult.SSID))
                {
                    continue;
                }

                scanList.append(scanResult.SSID + ";\n");
            }
        }
        mWifiScanList = scanList.toString();
    }

    public String getMAC() {
        if (mWifiInfo != null) {
            return mWifiInfo.getMacAddress();
        }
        return null;
    }

    private void handleNetworkStateChanged(NetworkInfo networkInfo)
    {
        mWifiInfo = mWifiManager.getConnectionInfo();
        if (mWifiManager.isWifiEnabled())
        {
            // TODO
        } else {
            // TODO
        }
    }

    private void handleSupplicantStateChanged(SupplicantState state, boolean hasError, int error)
    {
        if (hasError)
        {
            mWifiSupplicantState = ("ERROR AUTHENTICATING");
        } else
        {
            setSupplicantStateText(state);
        }
    }

    private void handleWifiSignalChanged(int rssi)
    {
        // TODO
    }

    private void handleWifiStateChanged(int wifiState)
    {
        // TODO
        switch (wifiState)
        {
            case WifiManager.WIFI_STATE_DISABLING:
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                break;
            default:
                LOGD("wifi state is bad");
                break;
        }
    }

    public static WiFiMng get(Context context) {
        synchronized (sLock) {
            if (si == null) {
                si = new WiFiMng(context);
            }
            return si;
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
