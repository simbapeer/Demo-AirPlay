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
package simba.org.apache.http.impl.nio;

import javax.net.ssl.SSLContext;

import simba.org.apache.http.HttpRequestFactory;
import simba.org.apache.http.annotation.Immutable;
import simba.org.apache.http.impl.DefaultHttpRequestFactory;
import simba.org.apache.http.nio.NHttpConnectionFactory;
import simba.org.apache.http.nio.NHttpServerConnection;
import simba.org.apache.http.nio.reactor.IOSession;
import simba.org.apache.http.nio.reactor.ssl.SSLIOSession;
import simba.org.apache.http.nio.reactor.ssl.SSLMode;
import simba.org.apache.http.nio.reactor.ssl.SSLSetupHandler;
import simba.org.apache.http.nio.util.ByteBufferAllocator;
import simba.org.apache.http.nio.util.HeapByteBufferAllocator;
import simba.org.apache.http.params.HttpConnectionParams;
import simba.org.apache.http.params.HttpParams;

/**
 * Factory for SSL encrypted, non-blocking {@link NHttpServerConnection}s.
 * <p>
 * The following parameters can be used to customize the behavior of this
 * class:
 * <ul>
 *  <li>{@link simba.org.apache.http.params.CoreProtocolPNames#HTTP_ELEMENT_CHARSET}</li>
 *  <li>{@link simba.org.apache.http.params.CoreConnectionPNames#SO_TIMEOUT}</li>
 *  <li>{@link simba.org.apache.http.params.CoreConnectionPNames#SOCKET_BUFFER_SIZE}</li>
 *  <li>{@link simba.org.apache.http.params.CoreConnectionPNames#MAX_HEADER_COUNT}</li>
 *  <li>{@link simba.org.apache.http.params.CoreConnectionPNames#MAX_LINE_LENGTH}</li>
 * </ul>
 *
 * @since 4.2
 */
@Immutable
public class SSLNHttpServerConnectionFactory
    implements NHttpConnectionFactory<DefaultNHttpServerConnection> {

    private final HttpRequestFactory requestFactory;
    private final ByteBufferAllocator allocator;
    private final SSLContext sslcontext;
    private final SSLSetupHandler sslHandler;
    private final HttpParams params;

    public SSLNHttpServerConnectionFactory(
            final SSLContext sslcontext,
            final SSLSetupHandler sslHandler,
            final HttpRequestFactory requestFactory,
            final ByteBufferAllocator allocator,
            final HttpParams params) {
        super();
        if (requestFactory == null) {
            throw new IllegalArgumentException("HTTP request factory may not be null");
        }
        if (allocator == null) {
            throw new IllegalArgumentException("Byte buffer allocator may not be null");
        }
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        this.sslcontext = sslcontext;
        this.sslHandler = sslHandler;
        this.requestFactory = requestFactory;
        this.allocator = allocator;
        this.params = params;
    }

    public SSLNHttpServerConnectionFactory(
            final SSLContext sslcontext,
            final SSLSetupHandler sslHandler,
            final HttpParams params) {
        this(sslcontext, sslHandler, new DefaultHttpRequestFactory(), new HeapByteBufferAllocator(), params);
    }

    public SSLNHttpServerConnectionFactory(final HttpParams params) {
        this(null, null, params);
    }

    private SSLContext getDefaultSSLContext() {
        SSLContext sslcontext;
        try {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, null, null);
        } catch (Exception ex) {
            throw new IllegalStateException("Failure initializing default SSL context", ex);
        }
        return sslcontext;
    }

    protected DefaultNHttpServerConnection createConnection(
            final IOSession session,
            final HttpRequestFactory requestFactory,
            final ByteBufferAllocator allocator,
            final HttpParams params) {
        return new DefaultNHttpServerConnection(session, requestFactory, allocator, params);
    }

    public DefaultNHttpServerConnection createConnection(final IOSession session) {
        SSLContext sslcontext = this.sslcontext != null ? this.sslcontext : getDefaultSSLContext();
        SSLIOSession ssliosession = new SSLIOSession(session, SSLMode.SERVER, sslcontext, this.sslHandler);
        session.setAttribute(SSLIOSession.SESSION_KEY, ssliosession);
        DefaultNHttpServerConnection conn =  createConnection(
                ssliosession, this.requestFactory, this.allocator, this.params);
        int timeout = HttpConnectionParams.getSoTimeout(this.params);
        conn.setSocketTimeout(timeout);
        return conn;
    }

}
