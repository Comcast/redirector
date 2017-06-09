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

import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.DistributionWithDefaultAndFallbackServers;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.redirector.service.IDistributionsWithDefaultService;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.IDistributionService;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path(RedirectorConstants.DISTRIBUTION_WITH_DEFAULT_CONTROLLER_PATH)
public class DistributionWithDefaultServerController {
    @Autowired
    private IServerService serverService;

    @Autowired
    private IDistributionService distributionService;

    @Autowired
    private IExportFileNameHelper exportFileNameHelper;

    @Autowired
    private IDistributionsWithDefaultService distributionsWithDefaultService;

    @Autowired
    private OperationContextHolder operationContextHolder;

    @GET
    @Path("distribuionswithdefault/{serviceName}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportServer(@PathParam("serviceName") final String serviceName) {
        Server defaultServer = serverService.getServer(serviceName);
        Distribution distribution = distributionService.getDistribution(serviceName);

        DistributionWithDefaultAndFallbackServers distributionWithDefaultAndFallbackServers =
                new DistributionWithDefaultAndFallbackServers(distribution, defaultServer);
        return Response.ok(distributionWithDefaultAndFallbackServers)
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForAll(EntityType.DISTRIBUTION_WITH_DEFAULT_AND_FALLBACK_SERVERS))
                .build();
    }

    @POST
    @Path("distribuionswithdefault/save/{serviceName}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response saveDistributionWithDefault(@PathParam("serviceName") final String serviceName, DistributionWithDefaultAndFallbackServers distributionWithDefaultAndFallbackServers) {
        operationContextHolder.buildContext(serviceName);

        distributionsWithDefaultService.save(serviceName, distributionWithDefaultAndFallbackServers);
        return Response.ok().build();
    }
}
