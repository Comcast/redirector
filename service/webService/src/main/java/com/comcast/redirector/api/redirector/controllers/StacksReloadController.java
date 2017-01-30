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

package com.comcast.redirector.api.redirector.controllers;

import com.comcast.redirector.api.redirector.service.IAppsService;
import com.comcast.redirector.api.redirector.service.IDataChangesNotificationService;
import com.comcast.redirector.common.RedirectorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path(RedirectorConstants.STACKS_RELOAD_PATH)
public class StacksReloadController {

    @Autowired
    private IDataChangesNotificationService dataChangesNotificationService;

    @Autowired
    private IAppsService appsService;

    @POST
    @Path("{serviceName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response reload(@PathParam("serviceName") final String serviceName) {

        if (appsService.getAppNames().getAppNames().contains(serviceName)) {
            dataChangesNotificationService.triggerStacksReload(serviceName);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("getVersion/{serviceName}")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response getVersion(@PathParam("serviceName") final String serviceName) {
        if (appsService.getAppNames().getAppNames().contains(serviceName)) {
            long version = dataChangesNotificationService.getStacksReloadVersion(serviceName);
            return Response.ok(version).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
