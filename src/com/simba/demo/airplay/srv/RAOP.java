
package com.simba.demo.airplay.srv;

import java.util.HashMap;
import java.util.Map;

import javax.jmdns.impl.ServiceInfoImpl;

/**
 * @author simba
 */
public class RAOP extends ServiceInfoImpl {
    public static RAOP get(String name, int port) {
        return new RAOP(TYPE_TP, name, port, 0, 0, txtRd);
    }

    public static final String TYPE_TP = "_raop._tcp.local.";

    private static final Map<String, String> txtRd = new HashMap<String, String>();

    private static final int AUDIO_CODECS_PCM = 0;
    /** Apple Lossless */
    private static final int AUDIO_CODECS_ALAC = 1;
    private static final int AUDIO_CODECS_AAC = 2;
    /** AAC Enhanced Low Delay */
    private static final int AUDIO_CODECS_AAC_ELD = 3;
    private static final String SUPPORTED_AUDIO_CODECS = AUDIO_CODECS_PCM + "," + AUDIO_CODECS_ALAC + ","
            + AUDIO_CODECS_AAC
            + "," + AUDIO_CODECS_AAC_ELD;

    private static final int AUDIO_CHANNEL_STEREO = 2;

    /** no encryption */
    private static final int ENCRYPTION_NONE = 0;
    /** RSA (AirPort Express) */
    private static final int ENCRYPTION_RSA = 1;
    private static final int ENCRYPTION_FairPlay = 3;
    /** MFiSAP (3rd-party devices) */
    private static final int ENCRYPTION_MFiSAP = 4;
    private static final int ENCRYPTION_FairPlay_SAPv2_5 = 5;

    private static final String SUPPORTED_ENCRYPTION = ENCRYPTION_NONE + "," + ENCRYPTION_FairPlay + ","
            + ENCRYPTION_FairPlay_SAPv2_5;

    private static final int METADATA_TEXT = 0;
    private static final int METADATA_ARTWORK = 1;
    private static final int METADATA_PROGRESS = 2;
    private static final String SUPPORTED_METADATA = METADATA_TEXT + "," + METADATA_ARTWORK + "," + METADATA_PROGRESS;

    private static String TR_KEY_VERSION = "txtvers";
    private static String TR_KEY_AUDIO_CHANNELS = "ch";
    private static String TR_KEY_AUDIO_CODECS = "cn";
    private static String TR_KEY_ENCRYPTION_TYPES = "et";
    private static String TR_KEY_METADATA_TYPES = "md";
    private static String TR_KEY_PASSWD_REQUIRED = "pw";
    private static String TR_KEY_AUDIO_SAMPLE_RATE = "sr";
    private static String TR_KEY_AUDIO_SAMPLE_SIZE = "ss";
    private static String TR_KEY_TRANSPORT = "tp";
    private static String TR_KEY_SYS_VERSION = "vs";
    private static String TR_KEY_APPLE_DEVICE_MODEL = "am";

    static {
        txtRd.put(TR_KEY_VERSION, "1");// TXT record version 1
        txtRd.put(TR_KEY_AUDIO_CHANNELS, "" + AUDIO_CHANNEL_STEREO);
        txtRd.put(TR_KEY_AUDIO_CODECS, SUPPORTED_AUDIO_CODECS);
        txtRd.put(TR_KEY_ENCRYPTION_TYPES, SUPPORTED_ENCRYPTION);
        txtRd.put(TR_KEY_METADATA_TYPES, SUPPORTED_METADATA);
        txtRd.put(TR_KEY_PASSWD_REQUIRED, "false");// the speaker require a
                                                   // password?
        txtRd.put(TR_KEY_AUDIO_SAMPLE_RATE, "44100");// 44100 Hz
        txtRd.put(TR_KEY_AUDIO_SAMPLE_SIZE, "16");// audio sample size: 16-bit
        txtRd.put(TR_KEY_TRANSPORT, "UDP");// supported transport: TCP or UDP
        txtRd.put(TR_KEY_SYS_VERSION, "130.14");// server version 130.14
        txtRd.put(TR_KEY_APPLE_DEVICE_MODEL, "AppleTV2,1");// device model
    }

    public static Map<String, String> getTxtRd() {
        return txtRd;
    }

    private RAOP(String type, String name, int port, int weight, int priority,
            Map<String, ?> props) {
        super(type, name, "", port, weight, priority, false, props);
    }

}
