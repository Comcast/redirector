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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.redirectorOffline.controllers;

import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.ruleengine.IDistributionService;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.Snapshot;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path(RedirectorConstants.SERVERS_OFFLINE_CONTROLLER_PATH)
public class ServersControllerOffline {

    @Autowired
    private IServerService serverService;

    @Autowired
    private OperationContextHolder operationContextHolder;

    @POST
    @Path("{appName}/" + RedirectorConstants.DEFAULT_SERVER_NAME)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addServer(@PathParam("appName") final String appName, final Snapshot snapshot){
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);

        OperationResult result = serverService.saveServer(appName, (Server) snapshot.getEntityToSave(), ApplicationStatusMode.OFFLINE);
        return Response.ok(result).build();
    }
}
