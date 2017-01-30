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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.endpoint.http.logging;

import com.comcast.redirector.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Filter that logs each request. Keep it fast.
 */

public class AccessLoggingFilter extends OncePerRequestFilter {
    private static Logger log = LoggerFactory.getLogger(AccessLoggingFilter.class);
    private static final String START_TIME = "request-start-time";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getAttribute(START_TIME) == null) {
            request.setAttribute(START_TIME, System.currentTimeMillis()); // save request time
        } else {
            log.warn("{} is already set -- possible configuration error", START_TIME);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            log(request, response);
        }
    }

    private void log(HttpServletRequest request, HttpServletResponse response) {
        long duration = 0;
        try {
            final Long requestStartTime = (Long) request.getAttribute(START_TIME);
            if (requestStartTime!=null && requestStartTime > 0) {
                duration = System.currentTimeMillis() - requestStartTime;
            }
        } catch (Exception x) {
            log.warn("Could not get request start time {}", x);
        }

        final StringBuilder httpHeaders = new StringBuilder('[');
        for (final Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
            final String headerName = headerNames.nextElement();
            httpHeaders.append('[').append(headerName).append(": ").append(request.getHeader(headerName)).append("], ");
        }
        httpHeaders.append(']');

        final StringBuffer requestStringBuffer = request.getRequestURL();

        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            requestStringBuffer.append("?").append(request.getQueryString());
        }

        Metrics.reportGatewayRedirectDurationStats(duration);

        log.info("Request: {} {} agent={} status={} duration={} HTTP Headers={}",
                request.getMethod(), requestStringBuffer,
                request.getHeader("User-Agent"),
                response.getStatus(),
                duration != 0 ? duration : "unknown",
                httpHeaders.toString());
    }
}
