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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.redirector.controllers;

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import com.comcast.redirector.api.validation.VerifyApplicationExists;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path(RedirectorConstants.SERVERS_CONTROLLER_PATH)
public class ServersController {
    @Autowired
    private IServerService serverService;

    @Autowired
    private IExportFileNameHelper exportFileNameHelper;

    @Autowired
    private OperationContextHolder operationContextHolder;

    @GET
    @Path("{appName}/" + RedirectorConstants.DEFAULT_SERVER_NAME +"/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServer(@VerifyApplicationExists @PathParam("appName") final String appName) {
        return Response.ok(serverService.getServer(appName)).build();
    }

    @GET
    @Path("export/{appName}/" + RedirectorConstants.DEFAULT_SERVER_NAME + "/")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportServer(@VerifyApplicationExists @PathParam("appName") final String appName) {
        return Response.ok(serverService.getServer(appName))
                .header(exportFileNameHelper.getHeader(),
                        exportFileNameHelper.getFileNameForOneEntity(EntityType.SERVER, appName, RedirectorConstants.DEFAULT_SERVER_NAME))
                .build();
    }

    @POST
    @Path("{appName}/" + RedirectorConstants.DEFAULT_SERVER_NAME)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addServer(@PathParam("appName") final String appName, final Server server) {
        operationContextHolder.buildContext(appName);
        serverService.saveServer(appName, server);
        return Response.ok(server).build();
    }
}
