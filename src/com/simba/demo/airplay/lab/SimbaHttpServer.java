/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.simba.demo.airplay.lab;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.CodingErrorAction;
import java.util.Locale;

import simba.org.apache.http.HttpException;
import simba.org.apache.http.HttpRequest;
import simba.org.apache.http.HttpResponse;
import simba.org.apache.http.HttpResponseInterceptor;
import simba.org.apache.http.MethodNotSupportedException;
import simba.org.apache.http.impl.DefaultConnectionReuseStrategy;
import simba.org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import simba.org.apache.http.impl.nio.DefaultNHttpServerConnection;
import simba.org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import simba.org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import simba.org.apache.http.nio.NHttpConnectionFactory;
import simba.org.apache.http.nio.NHttpServerConnection;
import simba.org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import simba.org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import simba.org.apache.http.nio.protocol.HttpAsyncExchange;
import simba.org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import simba.org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import simba.org.apache.http.nio.protocol.HttpAsyncRequestHandlerRegistry;
import simba.org.apache.http.nio.protocol.HttpAsyncService;
import simba.org.apache.http.nio.reactor.IOEventDispatch;
import simba.org.apache.http.nio.reactor.ListeningIOReactor;
import simba.org.apache.http.params.BasicHttpParams;
import simba.org.apache.http.params.CoreConnectionPNames;
import simba.org.apache.http.params.CoreProtocolPNames;
import simba.org.apache.http.params.HttpParams;
import simba.org.apache.http.protocol.HttpContext;
import simba.org.apache.http.protocol.HttpProcessor;
import simba.org.apache.http.protocol.ImmutableHttpProcessor;
import simba.org.apache.http.protocol.ResponseConnControl;
import simba.org.apache.http.protocol.ResponseContent;
import simba.org.apache.http.protocol.ResponseDate;
import simba.org.apache.http.protocol.ResponseServer;
import android.util.Log;

/**
 * HTTP/1.1 file server based on the non-blocking I/O model and capable of
 * direct channel (zero copy) data transfer.
 */
public class SimbaHttpServer extends Thread {
    private static final String tag = "SimbaHttpServer";
    private int mPort = -1;
    private RequestCallback mRC;

    public interface RequestCallback {
        public void onPOST(HttpRequest request, HttpResponse response, HttpContext context);

        public void onGET(HttpRequest request, HttpResponse response, HttpContext context);

        public void onHEAD(HttpRequest request, HttpResponse response, HttpContext context);
    }

    public SimbaHttpServer(int port) {
        mPort = port;
    }

    public SimbaHttpServer(int port, RequestCallback rc) {
        this(port);
        mRC = rc;
    }

    @Override
    public void run() {
        // HTTP parameters for the server
        HttpParams params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
        params.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024);
        params.setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpTest/1.1");
        params.setParameter(CoreProtocolPNames.HTTP_MALFORMED_INPUT_ACTION, CodingErrorAction.IGNORE);
        // Create HTTP protocol processing chain
        HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
                // Use standard server-side protocol interceptors
                new ResponseDate(),
                new ResponseServer(),
                new ResponseContent(),
                new ResponseConnControl()
        });
        // Create request handler registry
        HttpAsyncRequestHandlerRegistry reqistry = new HttpAsyncRequestHandlerRegistry();
        // Register the default handler for all URIs
        reqistry.register("*", new SimbaHttpHandler());
        // Create server-side HTTP protocol handler
        HttpAsyncService protocolHandler = new HttpAsyncService(
                httpproc, new DefaultConnectionReuseStrategy(), reqistry, params) {

            @Override
            public void connected(final NHttpServerConnection conn) {
                LOGD(conn + ": connection open");
                super.connected(conn);
            }

            @Override
            public void closed(final NHttpServerConnection conn) {
                LOGD(conn + ": connection closed");
                super.closed(conn);
            }

        };
        // Create HTTP connection factory
        NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory = new DefaultNHttpServerConnectionFactory(
                params);

        // Create server-side I/O event dispatch
        IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFactory);
        // Create server-side I/O reactor
        try {
            ListeningIOReactor ioReactor = new DefaultListeningIOReactor();
            // Listen of the given port
            InetSocketAddress isa = new InetSocketAddress(mPort);
            ioReactor.listen(isa);
            // Ready to go!
            ioReactor.execute(ioEventDispatch);
        } catch (InterruptedIOException ex) {
            LOGE("Interrupted");
        } catch (IOException e) {
            LOGE("I/O error: " + e.getMessage());
        }
        LOGD("Shutdown");
    }

    private class SimbaHttpHandler implements HttpAsyncRequestHandler<HttpRequest> {

        public SimbaHttpHandler() {
            super();
        }

        public HttpAsyncRequestConsumer<HttpRequest> processRequest(
                final HttpRequest request,
                final HttpContext context) {
            // Buffer request content in memory for simplicity
            return new BasicAsyncRequestConsumer();
        }

        public void handle(
                final HttpRequest request,
                final HttpAsyncExchange httpexchange,
                final HttpContext context) throws HttpException, IOException {
            HttpResponse response = httpexchange.getResponse();
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (method.equals("GET")) {
                if (mRC != null) {
                    mRC.onGET(request, response, context);
                }
            } else if (method.equals("POST")) {
                if (mRC != null) {
                    mRC.onPOST(request, response, context);
                }
            } else if (method.equals("HEAD")) {
                if (mRC != null) {
                    mRC.onHEAD(request, response, context);
                }
            } else {
                throw new MethodNotSupportedException(method + " method not supported");
            }

            // response
            httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
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
