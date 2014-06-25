
package com.simba.demo.utils;

import java.io.IOException;
import java.math.BigDecimal;

public class Utils {
    private static final char[] _nibbleToHex = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String toHex(byte[] code) {
        StringBuilder result = new StringBuilder(2 * code.length);

        for (int i = 0; i < code.length; i++) {
            int b = code[i] & 0xFF;
            result.append(_nibbleToHex[b / 16]);
            result.append(_nibbleToHex[b % 16]);
        }

        return result.toString();
    }

    public static void main(String[] args) {
        double d = 1.12345678901234567890d;
        String str = doubleLimitPrecision(d, 10);
        System.out.println(str);

        d = -0.12345678901234567890E-5;
        System.out.println(d + "");
        str = doubleLimitPrecision(d, 10);
        System.out.println(str);
        str = doubleLimitPrecision(d, 10);
        System.out.println(str);
        str = doubleLimitPrecision(d, 10);
        System.out.println(str);
        str = doubleLimitPrecision(d, 10);
        System.out.println(str);
    }

    static int
            hexCharToInt(char c) {
        if (c >= '0' && c <= '9')
            return (c - '0');
        if (c >= 'A' && c <= 'F')
            return (c - 'A' + 10);
        if (c >= 'a' && c <= 'f')
            return (c - 'a' + 10);

        throw new RuntimeException("invalid hex char '" + c + "'");
    }

    public static String byteToHex(byte arg) {
        StringBuilder ret = new StringBuilder(2);
        int b;
        b = 0x0f & (arg >> 4);
        ret.append("0123456789abcdef".charAt(b));
        b = 0x0f & arg;
        ret.append("0123456789abcdef".charAt(b));
        return ret.toString();
    }

    /**
     * Converts a hex String to a byte array.
     * 
     * @param s A string of hexadecimal characters, must be an even number of
     *            chars long
     * @return byte array representation
     * @throws RuntimeException on invalid format
     */
    public static byte[] hexStringToBytes(String s) {
        byte[] ret;

        if (s == null)
            return null;

        int sz = s.length();

        ret = new byte[sz / 2];

        for (int i = 0; i < sz; i += 2) {
            ret[i / 2] = (byte) ((hexCharToInt(s.charAt(i)) << 4)
                    | hexCharToInt(s.charAt(i + 1)));
        }

        return ret;
    }

    /**
     * Converts a byte array into a String of hexadecimal characters.
     * 
     * @param bytes an array of bytes
     * @return hex string representation of bytes array
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null)
            return null;

        StringBuilder ret = new StringBuilder(2 * bytes.length);

        for (int i = 0; i < bytes.length; i++) {
            int b;

            b = 0x0f & (bytes[i] >> 4);

            ret.append("0123456789abcdef".charAt(b));

            b = 0x0f & bytes[i];

            ret.append("0123456789abcdef".charAt(b));
        }

        return ret.toString();
    }

    public static String doubleLimitPrecision(double d, int limitedPrecision) {
        String doubleStr = BigDecimal.valueOf(d).toPlainString();
        LOGV("doubleLimitPrecision in: " + doubleStr);
        int len = doubleStr.length();
        int dotIdx = doubleStr.indexOf('.');
        if (dotIdx >= 0) {
            int precision = len - dotIdx - 1;
            if (precision > limitedPrecision) {
                doubleStr = doubleStr.substring(0, dotIdx + limitedPrecision + 1);
            }
        }
        LOGV("doubleLimitPrecision out: " + doubleStr);
        return doubleStr;
    }

    private static void LOGV(String msg) {
        // System.out.println(msg);
    }

    public static String byteArrayToASCIIString(byte[] src) {
        if (src == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < src.length; i++) {
            if (src[i] == 0) {
                break;
            }
            sb.append((char) src[i]);
        }
        return sb.toString();
    }

    public static int executeAndWaitForCMD(String cmd) {
        Runtime runtime = Runtime.getRuntime();
        java.lang.Process proc;
        try {
            proc = runtime.exec(cmd);
            proc.waitFor();
            return proc.exitValue();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e1) {
            e1.printStackTrace();
            return -1;
        }
    }

}
