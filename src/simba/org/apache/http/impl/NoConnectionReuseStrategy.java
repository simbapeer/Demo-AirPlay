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

package simba.org.apache.http.impl;

import simba.org.apache.http.ConnectionReuseStrategy;
import simba.org.apache.http.HttpResponse;
import simba.org.apache.http.annotation.Immutable;
import simba.org.apache.http.protocol.HttpContext;

/**
 * A strategy that never re-uses a connection.
 *
 * @since 4.0
 */
@Immutable
public class NoConnectionReuseStrategy implements ConnectionReuseStrategy {

    // default constructor


    // non-JavaDoc, see interface ConnectionReuseStrategy
    public boolean keepAlive(final HttpResponse response, final HttpContext context) {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }

        return false;
    }

}