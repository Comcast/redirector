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
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.redirector.service.IWhiteListStackUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path(RedirectorConstants.WHITELISTED_UPDATES_CONTROLLER_PATH)
public class WhitelistedStackUpdatesController {
    @Autowired
    private IWhiteListStackUpdateService whiteListStackUpdatedService;

    @GET
    @Path("{serviceName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getWhitelistedUpdatedStack(@PathParam("serviceName") String serviceName) {
        WhitelistedStackUpdates updates = whiteListStackUpdatedService.getWhitelistedStacksUpdates(serviceName);
        return Response.ok(updates).build();
    }

    @DELETE
    @Path("{serviceName}")
    public void deleteWhitelistedUpdatedStacks(@PathParam("serviceName") final String serviceName) {
        whiteListStackUpdatedService.saveWhitelistedStacksUpdates(new WhitelistedStackUpdates(), serviceName);
    }

}
