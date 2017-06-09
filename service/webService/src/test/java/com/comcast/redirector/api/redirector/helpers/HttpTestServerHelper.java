/**
 * Copyright 2017 Comcast Cable Communications Management, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.redirector.helpers;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;

public class HttpTestServerHelper {

    private static HttpServer httpServer;
    private static WebTarget webTarget;
    public static final String BASE_URL = "http://localhost:9090/";

    public static void createAndStartHttpServer(ResourceConfig resourceConfig) throws Exception {
        URI baseUri = URI.create(BASE_URL);
        httpServer = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);
        httpServer.start();

        Client client = ClientBuilder.newClient();
        webTarget = client.target(baseUri);
    }

    public static void initWebTarget(String url) {
        Client client = ClientBuilder.newClient();
        URI baseUri = URI.create(url);
        webTarget = client.target(baseUri);
    }

    public static WebTarget target(){
        return webTarget;
    }

    public static  void shutDownHttpServer() throws Exception {
        httpServer.shutdown();
    }
}
