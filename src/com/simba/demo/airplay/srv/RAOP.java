
package com.simba.demo.airplay.srv;

import java.util.HashMap;
import java.util.Map;

import javax.jmdns.impl.ServiceInfoImpl;

/**
 * @author simba
 */
public class RAOP extends ServiceInfoImpl {
    public static final int RAOP_SERVICE_PORT = 5000;

    public static RAOP get(String name, int port) {
        return new RAOP(TYPE_TP, name, port, 0, 0, txtRd);
    }

    public static RAOP get(String name) {
        return get(name, RAOP_SERVICE_PORT);
    }

    public static final String TYPE_TP = "_raop._tcp.local.";

    private static final Map<String, String> txtRd = new HashMap<String, String>();

    private static final int AUDIO_CODECS_PCM = 0;
    /** Apple Lossless */
    private static final int AUDIO_CODECS_ALAC = 1;
    private static final int AUDIO_CODECS_AAC = 2;
    /** AAC Enhanced Low Delay */
    private static final int AUDIO_CODECS_AAC_ELD = 3;
    private static final String SUPPORTED_AUDIO_CODECS = AUDIO_CODECS_PCM + "," + AUDIO_CODECS_ALAC;

    private static final int AUDIO_CHANNEL_STEREO = 2;

    /** no encryption */
    private static final int ENCRYPTION_NONE = 0;
    /** RSA (AirPort Express) */
    private static final int ENCRYPTION_RSA = 1;
    private static final int ENCRYPTION_FairPlay = 3;
    /** MFiSAP (3rd-party devices) */
    private static final int ENCRYPTION_MFiSAP = 4;
    private static final int ENCRYPTION_FairPlay_SAPv2_5 = 5;

    private static final String SUPPORTED_ENCRYPTION = ENCRYPTION_NONE + "," + ENCRYPTION_RSA;
    private static final String SUPPORTED_ENCRYPTION2 = ENCRYPTION_NONE + "," + ENCRYPTION_RSA;

    private static final int METADATA_TEXT = 0;
    private static final int METADATA_ARTWORK = 1;
    private static final int METADATA_PROGRESS = 2;
    private static final String SUPPORTED_METADATA = METADATA_TEXT + "," + METADATA_ARTWORK + "," + METADATA_PROGRESS;

    private static final String TR_KEY_VERSION = "txtvers";
    /** audio channels */
    private static final String TR_KEY_AUDIO_CHANNELS = "ch";
    /** audio codecs */
    private static final String TR_KEY_AUDIO_CODECS = "cn";
    /** supported encryption types */
    private static final String TR_KEY_ENCRYPTION_TYPES = "et";
    /** supported metadata types */
    private static final String TR_KEY_METADATA_TYPES = "md";
    /** does the speaker require a password? */
    private static final String TR_KEY_PASSWD_REQUIRED = "pw";
    private static final String TR_KEY_AUDIO_SAMPLE_RATE = "sr";
    private static final String TR_KEY_AUDIO_SAMPLE_SIZE = "ss";
    /** supported transport: TCP or UDP */
    private static final String TR_KEY_TRANSPORT = "tp";
    private static final String TR_KEY_SYS_VERSION = "vs";
    private static final String TR_KEY_APPLE_DEVICE_MODEL = "am";
    private static final String TR_KEY_da = "da";
    private static final String TR_KEY_sv = "sv";
    private static final String TR_KEY_vv = "vv";
    private static final String TR_KEY_vn = "vn";
    private static final String TR_KEY_ft = "ft";
    private static final String TR_KEY_sm = "sm";

    static {
        txtRd.put(TR_KEY_AUDIO_SAMPLE_RATE, "44100");// 44100 Hz
        txtRd.put(TR_KEY_AUDIO_CHANNELS, "" + AUDIO_CHANNEL_STEREO);    // 2
        txtRd.put(TR_KEY_vn, "3");
        txtRd.put(TR_KEY_VERSION, "1");// TXT record version 1
        txtRd.put(TR_KEY_sm, "false");
        txtRd.put(TR_KEY_AUDIO_CODECS, SUPPORTED_AUDIO_CODECS); // 0,1
        txtRd.put(TR_KEY_TRANSPORT, "UDP");//
        txtRd.put(TR_KEY_ENCRYPTION_TYPES, SUPPORTED_ENCRYPTION);   //0, 1
        txtRd.put(TR_KEY_PASSWD_REQUIRED, "false");
        txtRd.put(TR_KEY_sv, "false");
        txtRd.put(TR_KEY_AUDIO_SAMPLE_SIZE, "16");// audio sample size: 16-bit
        
//        txtRd.put(TR_KEY_da, "true"); // da
//        txtRd.put(TR_KEY_SYS_VERSION, AirPlay.VERSION);// server version 130.14
//        txtRd.put(TR_KEY_ft, AirPlay.SUPPORATED_FEATURES_STR);//
//        txtRd.put(TR_KEY_APPLE_DEVICE_MODEL, "AppleTV2,1");// device model
//        txtRd.put(TR_KEY_vv, "1");
//        txtRd.put("sf", "0x4");
//        txtRd.put(TR_KEY_METADATA_TYPES, SUPPORTED_METADATA);
//        txtRd.put(TR_KEY_APPLE_DEVICE_MODEL, AirPlay.APPLE_DEVICE_MODEL);
    }

    public static Map<String, String> getTxtRd() {
        return txtRd;
    }

    private RAOP(String type, String name, int port, int weight, int priority,
            Map<String, ?> props) {
        super(type, name, "", port, weight, priority, false, props);
    }

}
