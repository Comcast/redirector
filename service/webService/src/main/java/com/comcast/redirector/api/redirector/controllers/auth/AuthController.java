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

package com.comcast.redirector.api.redirector.controllers.auth;

import com.comcast.redirector.api.auth.AuthService;
import com.comcast.redirector.api.auth.DevProfilePredicate;
import com.comcast.redirector.api.auth.model.UserPrincipal;
import com.comcast.redirector.common.RedirectorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path(RedirectorConstants.AUTH_CONTROLLER_PATH)
public class AuthController {
    @Autowired
    private ApplicationContext context;

    @Context
    private HttpServletRequest request;

    @Autowired(required = false)
    private AuthService authService;

    private DevProfilePredicate devProfilePredicate = new DevProfilePredicate();

    @GET
    @Path("authinfo")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAuthInfo() {
        String [] profilesToTest = context.getEnvironment().getActiveProfiles();
        if (profilesToTest == null || profilesToTest.length == 0) {
            profilesToTest = context.getEnvironment().getDefaultProfiles();
        }

        if (devProfilePredicate.test(profilesToTest)) {
            return Response.ok(UserPrincipal.devProfileUser()).build();
        }

        UserPrincipal authResponse = null;
        if (authService != null) {
            authResponse = authService.getAuthorizedUser(request);
        }

        return (authResponse != null) ? Response.ok(authResponse).build() : Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
