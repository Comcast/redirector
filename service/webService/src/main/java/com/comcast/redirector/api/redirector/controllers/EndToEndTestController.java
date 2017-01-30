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
import com.comcast.redirector.api.model.testsuite.RedirectorTestCaseList;
import com.comcast.redirector.api.redirector.service.redirectortestsuite.IEndToEndService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//toDo: do we need it? e2e is not making it to open-source.
@Component
@Path(RedirectorConstants.END_TO_END_PATH)
public class EndToEndTestController {

    @Autowired
    private IEndToEndService endToEndService;

    @GET
    @Path("testCases/{serviceName}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTestCases(@PathParam("serviceName") final String serviceName) {
        RedirectorTestCaseList redirectorTestCaseList = endToEndService.getRedirectorTestCases(serviceName);
        return Response.ok(redirectorTestCaseList).build();
    }
}
