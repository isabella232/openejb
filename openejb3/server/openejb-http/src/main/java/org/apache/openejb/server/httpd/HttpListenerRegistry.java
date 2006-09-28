/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.httpd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class HttpListenerRegistry implements HttpListener {

    private final HashMap registry = new HashMap();

    public HttpListenerRegistry() {
    }

    public void onMessage(HttpRequest request, HttpResponse response) throws Exception {
        HashMap listeners;

        synchronized (registry) {
            listeners = new HashMap(registry);
        }

        String path = request.getURI().getPath();

        for (Iterator iterator = listeners.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String pattern = (String) entry.getKey();
            HttpListener listener = (HttpListener) entry.getValue();
            if (path.matches(pattern)) {
                listener.onMessage(request, response);
                break;
            }
        }
    }

    public void addHttpListener(HttpListener listener, String regex) {
        synchronized (registry) {
            registry.put(regex, listener);
        }
    }
}
