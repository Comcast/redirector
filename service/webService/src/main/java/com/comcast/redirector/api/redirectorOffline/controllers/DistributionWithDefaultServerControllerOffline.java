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

package com.comcast.redirector.api.redirectorOffline.controllers;

import com.comcast.redirector.api.model.DistributionWithDefaultAndFallbackServers;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.Snapshot;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IDistributionsWithDefaultService;
import com.comcast.redirector.common.RedirectorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path(RedirectorConstants.DISTRIBUTION_WITH_DEFAULT_CONTROLLER_PATH_OFFLINE)
public class DistributionWithDefaultServerControllerOffline {

    @Autowired
    private IDistributionsWithDefaultService distributionsWithDefaultService;

    @Autowired
    private OperationContextHolder operationContextHolder;

    @POST
    @Path("distribuionswithdefault/save/{serviceName}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response saveDistributionWithDefault(@PathParam("serviceName") final String serviceName, Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(serviceName, snapshot);

        DistributionWithDefaultAndFallbackServers distributionWithDefaultAndFallbackServers = new DistributionWithDefaultAndFallbackServers();
        distributionWithDefaultAndFallbackServers.setDefaultServer((Server) snapshot.getEntitiesToSave().get(1));
        distributionWithDefaultAndFallbackServers.setDistribution((Distribution) snapshot.getEntitiesToSave().get(0));

        OperationResult operationResult = distributionsWithDefaultService.saveDistribution(serviceName, distributionWithDefaultAndFallbackServers);
        return Response.ok(operationResult).build();
    }
}
