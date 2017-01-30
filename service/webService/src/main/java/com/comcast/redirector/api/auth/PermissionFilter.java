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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.auth;

import com.comcast.redirector.api.auth.model.UserPrincipal;
import com.comcast.redirector.api.model.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class PermissionFilter implements ContainerRequestFilter {
    private static Logger log = LoggerFactory.getLogger(PermissionFilter.class);

    @Autowired
    AuthService authService;

    @Context
    HttpServletRequest servletRequest; // TODO: use ContainerRequestContext instead of HttpServletRequest

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String url = requestContext.getUriInfo().getRequestUri().getPath();
        String method = requestContext.getMethod();

        if (! isAccessGranted(url, method)) {
            logRejectedRequest(url, method);

            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorMessage("Access denied for " + url + " due to invalid permissions")).build());
        }
    }

    private Collection<String> extractPermissions() {
        UserPrincipal authorizedUser = authService.getAuthorizedUser(servletRequest);
        if (authorizedUser != null) {
            return authorizedUser.getPermissions();
        }
        return Collections.emptyList();
    }

    private boolean isAccessGranted(String url, String method) {
        Collection<String> permissions = extractPermissions();
        return getPermissionCheckStrategy().isAccessGranted(url, method, permissions);
    }

    protected void logRejectedRequest(String url, String method) {
        log.info("attempted to do {} on {}, but is not authorized (permissions)", method, url);
    }

    protected PermissionCheckStrategy getPermissionCheckStrategy() {
        return PermissionCheckStrategies.PERMIT_ALL_WITHOUT_CHECK;
    }

    interface PermissionCheckStrategy {
        boolean isAccessGranted(String url, String method, Collection<String> permissions);
    }

    static class PermissionCheckStrategies {
        static PermissionCheckStrategy PERMIT_ALL_WITHOUT_CHECK = (url, method, permissions) -> true;
        static PermissionCheckStrategy CHECK_ALL_PERMISSIONS_AND_AVAILABLE_APPS = PermissionHelper::isAuthorized;
        static PermissionCheckStrategy CHECK_ALL_PERMISSIONS_DISREGARDING_APPS = PermissionHelper::isAuthorizedDisregardingAccessAppPermissions;
    }
}
