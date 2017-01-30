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
 * @author Sergey Lavrenyuk (slavrenyuk@productengine.com)
 */

package com.comcast.redirector.api.exceptionmapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DataServiceExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private static final Logger log = LoggerFactory.getLogger(DataServiceExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException exception) {
        log.error("Exception happened in RedirectorWebService", exception);

        int exceptionStatusCode = exception.getResponse().getStatus();
        String exceptionMessage = exception.getMessage();
        if (exceptionMessage == null) {
            Response.Status exceptionStatus = Response.Status.fromStatusCode(exceptionStatusCode);
            exceptionMessage = (exceptionStatus != null)
                    ? exceptionStatus.getReasonPhrase()
                    : String.format("Exception has been thrown with Status Code : %s", exceptionStatusCode);
        }

        return Response.status(exceptionStatusCode)
                .entity(exceptionMessage)
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
