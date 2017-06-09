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

import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.IDistributionService;
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

@Component
@Path(RedirectorConstants.DISTRIBUTION_CONTROLLER_PATH)
public class DistributionController {
    @Autowired
    private IDistributionService distributionService;

    @Autowired
    private IExportFileNameHelper exportFileNameHelper;

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private OperationContextHolder operationContextHolder;

    @GET
    @Path("{serviceName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDistribution(@VerifyApplicationExists @PathParam("serviceName") String serviceName) {
        return Response.ok(distributionService.getDistribution(serviceName)).build();
    }

    @GET
    @Path("export/{serviceName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exporttDistribution(@PathParam("serviceName") String serviceName) {
        return Response.ok(distributionService.getDistribution(serviceName))
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForAll(EntityType.DISTRIBUTION, serviceName))
                .build();
    }

    @POST
    @Path("{serviceName}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveDistribution(Distribution distribution, @Context UriInfo ui, @PathParam("serviceName") String serviceName) {
        operationContextHolder.buildContext(serviceName);

        distributionService.saveDistribution(distribution, serviceName);
        return Response.created(ui.getRequestUri()).entity(distribution).build();
    }
}
