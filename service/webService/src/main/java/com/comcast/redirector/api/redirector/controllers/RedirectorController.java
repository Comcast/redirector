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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.redirector.controllers;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.api.model.ServiceInstances;
import com.comcast.redirector.api.redirector.service.DefaultNodesService;
import com.comcast.redirector.api.redirector.service.IAppsService;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Component
@Path(RedirectorConstants.REDIRECTOR_CONTROLLER_PATH)
public class RedirectorController {
    private static Logger log = LoggerFactory.getLogger(RedirectorController.class);

    @Autowired
    private IDataSourceConnector connector;

    @Autowired
    private DefaultNodesService nodesService;

    @Autowired
    private IAppsService appsService;

    @GET
    @Path("{serviceName}/instances/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRedirectorInstances(@PathParam("serviceName") final String serviceName) {
        List<String> instances = new ArrayList<>();
        try {
            String path = PathHelper.getPathHelper(EntityType.INSTANCES, connector.getBasePath()).getPathByService(serviceName);
            instances = connector.getChildren(path);
        } catch (Exception e) {
            log.error("failed to get list of active XRE Redirector instances", e);
        }

        return Response.ok(new ServiceInstances(instances)).build();
    }

    @GET
    @Path(RedirectorConstants.EndpointPath.APPLICATION_NAMES)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRedirectorAppNames () {
        return Response.ok(appsService.getAppNames()).build();
    }
    
    @GET
    @Path(RedirectorConstants.EndpointPath.GET_ALL_REGISTERED_APPS)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllAppNamesRegisteredInStacks () {
        return Response.ok(appsService.getAllRegisteredApps()).build();
    }
    
    @GET
    @Path("createNodesIfNotPresent/{applicationName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createNodesIfNotPresent (@PathParam("applicationName") String applicationName) {
        if (!nodesService.areNodesPresentForService(applicationName)) {
             nodesService.createNodesForService(applicationName);
        }
        return Response.ok().build();
    }
}
