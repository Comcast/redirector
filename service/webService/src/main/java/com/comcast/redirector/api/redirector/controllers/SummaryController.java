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

import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.redirector.service.summary.ISummaryService;
import com.comcast.redirector.common.RedirectorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

@Component
@Path(RedirectorConstants.SUMMARY_PATH)
public class SummaryController {

    @Autowired
    private ISummaryService summaryService;

    @GET
    @Path("{serviceName}/{namespacedListNames : [a-zA-Z0-9,%_]+}/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSummary(@PathParam("serviceName") final String serviceName, @PathParam("namespacedListNames") final String namespacedListNames) {
        if(namespacedListNames == null || namespacedListNames.isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Namespaced list names are not correct: " + namespacedListNames)).build());
        }
        return Response.ok(summaryService.getSummary(serviceName, Arrays.asList(namespacedListNames.split(",")))).build();
    }
}
