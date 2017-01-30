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

import com.comcast.redirector.api.model.StackComment;
import com.comcast.redirector.api.redirector.service.IStackCommentsService;
import com.comcast.redirector.common.RedirectorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Component
@Path(RedirectorConstants.STACK_COMMENTS_CONTROLLER_PATH)
public class StackCommentsController {
    @Autowired
    private IStackCommentsService stackCommentsService;

    @GET
    @Path("getOne/{serviceName}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getStackComment(@PathParam("serviceName") String serviceName, @QueryParam("path") String path) {
        return Response.ok(stackCommentsService.getComment(path, serviceName)).build();
    }

    @POST
    @Path("post/{serviceName}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response saveStackMetadata(StackComment comment,
                                            @Context UriInfo ui,
                                            @PathParam("serviceName") String serviceName,
                                            @QueryParam("path") String path) {
        stackCommentsService.saveComment(comment, path, serviceName);
        return Response.created(ui.getRequestUri()).entity(comment).build();
    }
}
