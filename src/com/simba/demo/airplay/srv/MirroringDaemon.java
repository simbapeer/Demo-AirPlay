
package com.simba.demo.airplay.srv;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import simba.org.apache.http.HttpEntity;
import simba.org.apache.http.HttpEntityEnclosingRequest;
import simba.org.apache.http.HttpRequest;
import simba.org.apache.http.HttpResponse;
import simba.org.apache.http.HttpStatus;
import simba.org.apache.http.entity.ContentType;
import simba.org.apache.http.nio.NHttpConnection;
import simba.org.apache.http.nio.entity.NStringEntity;
import simba.org.apache.http.protocol.ExecutionContext;
import simba.org.apache.http.protocol.HttpContext;
import android.os.Debug;
import android.util.Log;

import com.simba.demo.airplay.App;
import com.simba.demo.airplay.lab.SimbaHttpServer;
import com.simba.demo.airplay.lab.SimbaHttpServer.RequestCallback;
import com.simba.demo.utils.Utils;

public class MirroringDaemon {
    private static final String tag = "MirroringDaemon";
    private SimbaHttpServer mHttpServer = null;

    private static final String GET_SERVER_CAPABILITES = "/stream.xml";
    private static final String POST_STREAM = "/stream";
    private static final String POST_FP_SETUP = "/fp-setup";
    private static final String RESP_SERVER_CAPABILITIES = "stream.plist";

    private static final int SCREEN_HIGHT = 720;// FIXME to be modified.
    private static final int SCREEN_WIDTH = 1280;// FIXME to be modified.
    private static final boolean OVER_SCANNED = false;
    private static final String REFRESH_RATE = "0.016666666666666666";
    private static final String VERSION = AirPlay.VERSION;
    private static final int MD_PORT = 7100;

    private SimbaHttpServer.RequestCallback mHttpRC = new RequestCallback() {

        @Override
        public void onPOST(HttpRequest request, HttpResponse response, HttpContext context) {
            String target = request.getRequestLine().getUri();
            LOGI("onPOST: " + target);
            String path = "";
            try {
                path = URLDecoder.decode(target, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (path == null) {
                return;
            }
            if (path.equals(POST_FP_SETUP)) {
                respFPSetup(request, response, context);
            } else if (path.equals(POST_STREAM)) {
                respFPStream(request, response, context);
            } else {
                resp404(request, response, context, path);
            }
            return;

        }

        @Override
        public void onHEAD(HttpRequest request, HttpResponse response, HttpContext context) {
            // FIXME do nothing
        }

        @Override
        public void onGET(HttpRequest request, HttpResponse response, HttpContext context) {
            String target = request.getRequestLine().getUri();
            LOGI("onGET: " + target);
            String path = "";
            try {
                path = URLDecoder.decode(target, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (path == null) {
                return;
            }
            if (path.equals(GET_SERVER_CAPABILITES)) {
                respServerCapability(request, response, context);
            } else if (path.equals("")) {
                // TODO business
            } else {
                resp404(request, response, context, path);
            }
            return;
        }
    };

    private static void respServerCapability(HttpRequest request, HttpResponse response, HttpContext context) {
        NHttpConnection conn = (NHttpConnection) context.getAttribute(
                ExecutionContext.HTTP_CONNECTION);
        LOGD("conn: " + conn);
        response.setStatusCode(HttpStatus.SC_OK);
        String resp = String.format(App.getRespPlist(RESP_SERVER_CAPABILITIES), SCREEN_HIGHT, SCREEN_WIDTH,
                OVER_SCANNED,
                REFRESH_RATE, VERSION);
        LOGV(">>>>>>>>>>>>>> RESP <<<<<<<<<<<<<< \n" + resp);
        NStringEntity entity = new NStringEntity(resp, ContentType.create("text/x-apple-plist+xml", "UTF-8"));
        response.setEntity(entity);
    }

    private static void respFPSetup(HttpRequest request, HttpResponse response, HttpContext context) {
        LOGD("TOD respFPSetup: " + request);
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest req = (HttpEntityEnclosingRequest) request;
            req.getEntity();
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            try {
                InputStream is = entity.getContent();
                final int step = 512;
                byte[] buffer = new byte[step];
                int readCount = 0;
                for (;;) {
                    int read = is.read(buffer, readCount, (int) (buffer.length - readCount));
                    if (read < 0) {
                        break;
                    } else {
                        readCount += read;
                        if (readCount >= buffer.length) {
                            byte buffer2[] = new byte[buffer.length + step];
                            System.arraycopy(buffer, 0, buffer2, 0, buffer.length);
                            buffer = buffer2;
                        }
                    }
                }
                String s = new String(buffer, 0, readCount);
                LOGW("buffer hex: " + Utils.toHex(buffer));
                LOGW("buffer str: " + s);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Incoming entity content (bytes): " + entity.getContentLength());
        } else {
            LOGW("request is NOT instanceof HttpEntityEnclosingRequest");
        }
        response.setStatusCode(HttpStatus.SC_OK);
    }

    private static void respFPStream(HttpRequest request, HttpResponse response, HttpContext context) {
        LOGD("TOD respFPStream: " + request);
        response.setStatusCode(HttpStatus.SC_OK);
    }

    private static void resp404(HttpRequest request, HttpResponse response, HttpContext context, String path) {
        LOGE("File \"" + path + "\" not found");
        Debug.waitForDebugger();
        NHttpConnection conn = (NHttpConnection) context.getAttribute(
                ExecutionContext.HTTP_CONNECTION);
        LOGD("conn: " + conn);
        response.setStatusCode(HttpStatus.SC_NOT_FOUND);
        String resp =
                "<html><body><h1>File \"" + path +
                        "\n not found</h1></body></html>";
        LOGV(">>>>>>>>>>>>>> RESP <<<<<<<<<<<<<< \n" + resp);
        NStringEntity entity = new NStringEntity(resp, ContentType.create("text/html", "UTF-8"));
        response.setEntity(entity);
    }

    public MirroringDaemon() {
        mHttpServer = new SimbaHttpServer(MD_PORT, mHttpRC);
    }

    public void start() {
        mHttpServer.start();
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

    public static void LOGV(String msg) {
        Log.v(tag, msg);
    }

    public static void LOGI(String msg) {
        Log.i(tag, msg);
    }

}
