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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.redirector.controllers;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.traffic.TrafficInputParams;
import com.comcast.redirector.api.redirector.service.traffic.ITrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Component
@Path(RedirectorConstants.TRAFFIC_PATH)
public class TrafficController {

    @Autowired
    private ITrafficService trafficService;

    @GET
    @Path("current/{serviceName}/{totalNumberConnections}/{connectionThreshold}/{isActive}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCurrentTraffic(@PathParam("serviceName") final String serviceName,
                                      @PathParam("totalNumberConnections") final long totalNumberConnections,
                                      @PathParam("connectionThreshold") final long connectionThreshold,
                                      @PathParam("isActive") final boolean isActive) {
        TrafficInputParams trafficInputParams = new TrafficInputParams();
        trafficInputParams.setTotalNumberConnections(totalNumberConnections);
        trafficInputParams.setConnectionThreshold(connectionThreshold);
        trafficInputParams.setDistributionMode(TrafficInputParams.DistributionMode.CURRENT);
        trafficInputParams.setHostsMode(isActive ? TrafficInputParams.HostsMode.ONLY_ACTIVE_WHITELISTED : TrafficInputParams.HostsMode.ALL_WHITELISTED);

        return Response.ok(trafficService.getTraffic(serviceName, trafficInputParams)).build();
    }

    @GET
    @Path("next/{serviceName}/{totalNumberConnections}/{connectionThreshold}/{isActive}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getNextTraffic(@PathParam("serviceName") final String serviceName,
                                   @PathParam("totalNumberConnections") final long totalNumberConnections,
                                   @PathParam("connectionThreshold") final long connectionThreshold,
                                   @PathParam("isActive") final boolean isActive) {
        TrafficInputParams trafficInputParams = new TrafficInputParams();
        trafficInputParams.setTotalNumberConnections(totalNumberConnections);
        trafficInputParams.setConnectionThreshold(connectionThreshold);
        trafficInputParams.setDistributionMode(TrafficInputParams.DistributionMode.NEXT);
        trafficInputParams.setHostsMode(isActive ? TrafficInputParams.HostsMode.ONLY_ACTIVE_WHITELISTED : TrafficInputParams.HostsMode.ALL_WHITELISTED);

        return Response.ok(trafficService.getTraffic(serviceName, trafficInputParams)).build();
    }

    @POST
    @Path("{serviceName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTraffic(@PathParam("serviceName") String serviceName, final TrafficInputParams inputParams) {
        return Response.ok(trafficService.getTraffic(serviceName, inputParams)).build();
    }
}
