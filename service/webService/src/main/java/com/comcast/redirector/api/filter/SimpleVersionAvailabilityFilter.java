/**
 * Copyright 2015 Comcast Cable Communications Management, LLC
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

package com.comcast.redirector.api.filter;

import com.comcast.redirector.dataaccess.dao.NodeVersionDAO;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;

import static com.comcast.redirector.common.RedirectorConstants.NAMESPACE_CONTROLLER_PATH;

public class SimpleVersionAvailabilityFilter extends LoggingFilter {
    private static Logger log = LoggerFactory.getLogger(SimpleVersionAvailabilityFilter.class);
    private static final String VERSION_STRING = "version";

    @Context
    private HttpServletRequest servletRequest;

    @Autowired
    private NodeVersionDAO nodeVersionDAO;

    @Override
    public void filter(ContainerRequestContext request) {
        Long desiredVersion = null;
        try {
            MultivaluedMap<String, String> queryParametersMap = request.getUriInfo().getQueryParameters(true);
            if (queryParametersMap.containsKey(VERSION_STRING)) {
                desiredVersion = Long.parseLong(request.getUriInfo().getQueryParameters(true).getFirst(VERSION_STRING));
            }
        } catch (Exception e) {
            log.debug("Error parsing version", e);
            throw new WebApplicationException("Version should be numeric", Response.Status.BAD_REQUEST);
        }
        if (desiredVersion != null) {
            String url = request.getUriInfo().getRequestUri().toString();
            log.debug("versionCheckStarted url=" + url);
            Request parsedRequest = new Request(url, request.getMethod());
            Optional<String> applicationName = Request.AppNameExtractionStrategies.stream()
                    .filter(strategy -> strategy.isEligible(parsedRequest.getNormalizedUrl()))
                    .map(parsedRequest::getApplicationFromUrl)
                    .findFirst();

            if (applicationName.isPresent()) {
                Long actualVersion = nodeVersionDAO.getModelChangedVersion(applicationName.get(), true);
                if (!Objects.equals(actualVersion, desiredVersion)) {
                    failWithMessageAndStatus("Version conflict entity=model , url=" + url + " actual=" + actualVersion + " desired=" + desiredVersion, Response.Status.CONFLICT);
                }
            } else {
                if (url.contains(NAMESPACE_CONTROLLER_PATH)) {
                    Long actualVersion = nodeVersionDAO.getNamespacedListsVersion(true);
                    if (!Objects.equals(actualVersion, desiredVersion)) {
                        failWithMessageAndStatus("Version conflict entity=namespaced , url=" + url + " actual=" + actualVersion + " desired=" + desiredVersion, Response.Status.CONFLICT);
                    }
                } else {
                    Long actualVersion = nodeVersionDAO.getNamespacedListsVersion(true);
                    if (!Objects.equals(actualVersion, desiredVersion)) {
                        failWithMessageAndStatus("Version parameter is present, but app name is not found", Response.Status.BAD_REQUEST);
                    }
                }
                log.debug("versionCheckEnded url=" + url);
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {

    }

    public void failWithMessageAndStatus(String message, Response.Status status) {
        Response errorResponse = Response.status(status)
                .entity(message)
                .build();
        log.warn(message);
        throw new WebApplicationException(errorResponse);
    }
}
