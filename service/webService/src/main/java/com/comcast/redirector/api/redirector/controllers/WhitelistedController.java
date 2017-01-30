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

import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.IWhiteListService;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import com.comcast.redirector.api.validation.VerifyApplicationExists;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Path(RedirectorConstants.WHITELISTED_CONTROLLER_PATH)
public class WhitelistedController {
    @Autowired
    private IWhiteListService whiteListService;

    @Autowired
    private IExportFileNameHelper fileNameExportHelper;

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private OperationContextHolder operationContextHolder;

    @GET
    @Path("{serviceName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getWhitelistedStack(@VerifyApplicationExists @PathParam("serviceName") String serviceName) {
        Whitelisted whitelisted = whiteListService.getWhitelistedStacks(serviceName);
        return Response.ok(whitelisted).build();
    }

    @GET
    @Path("export/{serviceName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportWhitelistedStack(@PathParam("serviceName") String serviceName) {
        Whitelisted whitelisted = whiteListService.getWhitelistedStacks(serviceName);
        return Response.ok(whitelisted)
                .header(fileNameExportHelper.getHeader(), fileNameExportHelper.getFileNameForAll(EntityType.WHITELIST, serviceName))
                .build();
    }

    @POST
    @Path("{serviceName}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveWhitelistedStacks(Whitelisted whitelisted, @Context UriInfo ui, @PathParam("serviceName") String serviceName) {
        operationContextHolder.buildContext(serviceName);
        whiteListService.saveWhitelistedStacks(whitelisted, serviceName);
        return Response.created(ui.getRequestUri()).entity(whitelisted).build();
    }

    @PUT
    @Path("{serviceName}/addStacks")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addWhitelistedStacks(Whitelisted whitelistedStacks, @Context UriInfo ui, @VerifyApplicationExists @PathParam("serviceName") String serviceName) {
        operationContextHolder.buildContext(serviceName);
        return Response.created(ui.getRequestUri()).entity(whiteListService.addWhitelistedStacks(serviceName, whitelistedStacks)).build();
    }

    @DELETE
    @Path("{serviceName}/{values: (\\S+)}/")
    public void deleteWhitelistedStacks(@PathParam("serviceName") final String serviceName, @PathParam("values") final String values) {
        operationContextHolder.buildContext(serviceName);
        whiteListService.deleteWhitelistedStacks(serviceName, values);
    }

}
