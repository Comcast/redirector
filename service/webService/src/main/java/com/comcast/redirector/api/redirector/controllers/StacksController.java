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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.redirector.controllers;

import com.comcast.redirector.api.redirector.service.IDataChangesNotificationService;
import com.comcast.redirector.api.validation.VerifyApplicationExists;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.xrestack.HostIPsListWrapper;
import com.comcast.redirector.api.model.xrestack.Paths;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path(RedirectorConstants.STACKS_CONTROLLER_PATH)
public class StacksController {
    @Autowired
    private IStacksService stacksService;

    @Autowired
    private IExportFileNameHelper exportFileNameHelper;

    @Autowired
    private IDataChangesNotificationService dataChangesNotificationService;

    @GET
    @Path("{serviceName}/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getStacksForService(@VerifyApplicationExists @PathParam("serviceName") final String serviceName) {
        return Response.ok(stacksService.getStacksForService(serviceName)).build();
    }

    @GET
    @Path("{serviceName}/export")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getExportPaths(@PathParam("serviceName") final String serviceName) {
        return Response.ok(stacksService.getStacksForService(serviceName))
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForAll(EntityType.STACK, serviceName))
                .build();
    }

    @GET
    @Path("")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllStacks() {
        return Response.ok(stacksService.getStacksForAllServices()).build();
    }

    @GET
    @Path("{serviceName}/addresses")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAddressByStackOrFlavor(@VerifyApplicationExists @PathParam("serviceName") final String serviceName, @QueryParam("stackName") String stackName, @QueryParam("flavorName") String flavorName) {
        try {
            return Response.ok(stacksService.getAddressByStackOrFlavor(serviceName, stackName, flavorName)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{serviceName}/addresses/random")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRandomAddressByStackOrFlavor(@VerifyApplicationExists @PathParam("serviceName") final String serviceName, @QueryParam("stackName") String stackName, @QueryParam("flavorName") String flavorName) {
        try {
            HostIPsListWrapper addresses = stacksService.getRandomAddressByStackOrFlavor(serviceName, stackName, flavorName);
            if (addresses == null || addresses.getHostIPsList().size() == 0) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(addresses).build();

        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("deleteStacks")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteStacks(Paths paths) {
        stacksService.deleteStacks(paths);
        return Response.ok().build();
    }

    @GET
    @Path("getVersion/")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response getVersion() {
        long version = dataChangesNotificationService.getStacksVersion();
        return Response.ok(version).build();
    }

}
