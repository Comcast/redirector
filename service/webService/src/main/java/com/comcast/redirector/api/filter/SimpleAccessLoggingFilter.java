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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.filter;

import com.comcast.redirector.metrics.Metrics;
import org.glassfish.jersey.filter.LoggingFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import java.util.ArrayList;

/**
 * A Jersey (JAX-RS) container filter that logs each request. Keep it fast.
 */
public class SimpleAccessLoggingFilter extends LoggingFilter {
    private static Logger log = LoggerFactory.getLogger("AccessLoggingFilter");
    private static final String START_TIME = "request-start-time";
    private static final String USERNAME = "username";
    static final String NON_AUTHENTICATED_USERNAME = "non-authenticated";
    static final String COOKIE_HEADER = "Cookie";
    protected static final int AUTH_EXPOSURE_TAIL_SIZE = 12;

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext request) {
        MDC.put(USERNAME, getUsername(servletRequest));

        if (request.getProperty(START_TIME) == null) {
            request.setProperty(START_TIME, System.currentTimeMillis());
        } else {
            log.warn("{} is already set -- possible configuration error", START_TIME);
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {

        String duration = "unknown";
        try {
            Long requestStartTime = (Long)request.getProperty(START_TIME);
            if (requestStartTime != null && requestStartTime > 0)
                duration = Long.toString(System.currentTimeMillis() - requestStartTime);
        } catch (Exception x) {
            log.warn("Could not get request start time {}", x);
        }

        ArrayList<String> httpHeaders = getHeadersToLog(request);
        ArrayList<String> cookies = getCookiesToLog(request);

        String agent = request.getHeaderString(HttpHeaders.USER_AGENT);

        log.info("Request: {} {} agent={} status={} duration={} HTTP Headers={},  Cookies={}",
                request.getMethod(), getUrlToLog(request.getUriInfo().getRequestUri().toString()), agent, response.getStatus(), duration, httpHeaders, cookies);

        if (! "unknown".equals(duration)) {
            Metrics.reportWSApiCallDurationStats(agent, request.getUriInfo().getRequestUri().toString(), request.getMethod(), Long.parseLong(duration));
        }

        if (response.getStatus() >= 400) {
            Metrics.reportWSFailedResponseStats(agent, request.getUriInfo().getRequestUri().toString(), request.getMethod());
        }

        MDC.remove(USERNAME);
    }

    public ArrayList<String> getCookiesToLog (ContainerRequestContext request) {
        ArrayList<String> cookies = new ArrayList<>();
        for (String cookieName : request.getCookies().keySet()) {
            cookies.add(cookieName + ": " + request.getCookies().get(cookieName));
        }
        return cookies;
    }

    public ArrayList<String> getHeadersToLog (ContainerRequestContext request) {
        ArrayList<String> httpHeaders = new ArrayList<>();
        for (String name :request.getHeaders().keySet()) {
            String authHeaderValue = null;
            if (name.equals(HttpHeaders.AUTHORIZATION) && StringUtils.isNotBlank(request.getHeaders().get(name).get(0))) {
                authHeaderValue = request.getHeaders().get(name).get(0);
                int start = authHeaderValue.length() - AUTH_EXPOSURE_TAIL_SIZE;
                authHeaderValue = start >= 0 ? authHeaderValue.substring(start, authHeaderValue.length()) : "invalid auth header";
            }
            if (!name.equals(COOKIE_HEADER)) {
                httpHeaders.add(name + ": " + (authHeaderValue == null ? request.getHeaders().get(name) : authHeaderValue));
            }
        }
        return httpHeaders;
    }

    public String getUrlToLog (String url) {
        return url;
    }

    String getUsername(HttpServletRequest servletRequest) {
        return NON_AUTHENTICATED_USERNAME;
    }
}
