/**
 * Copyright 2016 Comcast Cable Communications Management, LLC 
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

import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.model.Server;

import javax.ws.rs.core.MediaType;

public class SetUpAndCleanUpHelper {

    public static final int DEFAULT_MIN_HOSTS = 1;
    public static final int DEFAULT_APP_MIN_HOSTS = 1;


    public static void setUpDefaultWhitelistedAndServer(String serviceNameTest) {
        WhitelistedHelper.post(serviceNameTest, WhitelistedHelper.createWhitelisted(WhitelistedHelper.defaultWhiteList),
                MediaType.APPLICATION_JSON);
        PendingChangesHelper.approveAllPendingChanges(HttpTestServerHelper.target(), serviceNameTest);
        Server defaultServer = createDefaultServer();
        ServersHelper.post(serviceNameTest, defaultServer);
        PendingChangesHelper.approveAllPendingChanges(HttpTestServerHelper.target(), serviceNameTest);

    }


    private static Server createDefaultServer() {
        Server server = new Server();
        server.setName("default");
        server.setPath("Zone2");
        server.setUrl("def://defhost");
        server.setDescription("def");
        server.setIsNonWhitelisted("false");
        return server;
    }

    public static RedirectorConfig createDefaultRedirectorConfig() {
        RedirectorConfig redirectorConfig = new RedirectorConfig();
        redirectorConfig.setMinHosts(DEFAULT_MIN_HOSTS);
        redirectorConfig.setAppMinHosts(DEFAULT_APP_MIN_HOSTS);
        return redirectorConfig;
    }
}
