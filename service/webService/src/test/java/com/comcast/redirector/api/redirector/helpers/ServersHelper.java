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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector.helpers;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.Server;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class ServersHelper {

    private static final String SERVERS_SERVICE_PATH = RedirectorConstants.SERVERS_CONTROLLER_PATH;

    public static final String _SERVER = "server";

    public static Server post(String serviceName, String serverName, Server server, String mediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(SERVERS_SERVICE_PATH).path(serviceName).path(serverName);
        return ServiceHelper.post(webTarget, server, mediaType);
    }

    /**
     * simple post - without checking response and specific mediaType
     */
    public static Server post(String serviceName, Server server) {
        return post(serviceName, server.getName(), server, MediaType.APPLICATION_JSON);
    }

    public static Server get(String serviceName, String serverName, String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(SERVERS_SERVICE_PATH).path(serviceName).path(serverName);
        return ServiceHelper.get(webTarget, responseMediaType, Server.class);
    }

    /**
     * simple get - without checking specific mediaType
     */
    public static Server get(String serviceName, String serverName) {
        return get(serviceName, serverName, MediaType.APPLICATION_JSON);
    }

// ************************************* SERVERS PENDING CHANGES  *************************************** //

    public static void approvePendingChanges(WebTarget target, String serviceName, String serverName) throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_SERVER).path(serverName).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.approvePendingChanges(webTarget);
    }

    public static void cancelPendingChanges(WebTarget target, String serviceName, String serverName) throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_SERVER).path(serverName).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.cancelPendingChanges(webTarget);
    }

}
