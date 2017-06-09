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
 */
package com.comcast.redirector.api.redirector.controllers;

import com.comcast.redirector.api.model.ModelStatesWrapper;
import com.comcast.redirector.api.redirector.service.IModelInitializerService;
import com.comcast.redirector.common.RedirectorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.comcast.redirector.common.RedirectorConstants.EndpointPath.VALID_MODEL_EXISTS;

@Component
@Path(RedirectorConstants.INITIALIZER_CONTROLLER_PATH)
public class RedirectorModelInitializerController {
    @Autowired
    private IModelInitializerService modelInitializerService;

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllExistingApplications() {
        ModelStatesWrapper allServices = modelInitializerService.getAllApplications();
        return Response.ok(allServices).build();
    }

    @POST
    @Path("{appName}/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response activateModelForService(@PathParam("appName") final String appName) {
        return Response.ok(modelInitializerService.activateModelForService(appName)).build();
    }

    @GET
    @Path(VALID_MODEL_EXISTS)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response validModelExists() {
        Boolean returnValue = modelInitializerService.validModelExists();
        return Response.ok(returnValue).build();
    }
    
    @GET
    @Path(VALID_MODEL_EXISTS + "/{appName}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response validModelExists(@PathParam("appName") final String appName) {
        Boolean returnValue = modelInitializerService.isValidModelForAppExists(appName);
        return Response.ok(returnValue).build();
    }
    
    @GET
    @Path("/defaultModelConstructionDetails/{appName}/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response defaultModelConstructionDetails(@PathParam("appName") final String appName) {
        return Response.ok(modelInitializerService.defaultModelConstructionDetails(appName)).build();
    }

    @GET
    @Path("{appName}/validate")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response validate(@PathParam("appName") final String appName) {
        Boolean returnValue = modelInitializerService.validateApplication(appName);
        return Response.ok(returnValue).build();
    }

}
