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
 */
package com.comcast.redirector.api.exceptionmapper;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    private static final Logger log = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(ValidationException exception) {

        if (exception instanceof ConstraintViolationException) {
            final Response.ResponseBuilder response = Response.status(Response.Status.NOT_FOUND);

            return response.entity(getConstraintViolationExceptionMessage((ConstraintViolationException) exception))
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        log.warn("Validation exception raised", exception);
        return Response.serverError().entity(exception.getMessage()).build();
    }

    private String getConstraintViolationExceptionMessage(ConstraintViolationException exception) {
        StringBuilder result = new StringBuilder();
        if (CollectionUtils.isNotEmpty(exception.getConstraintViolations())) {
            for (ConstraintViolation violation : exception.getConstraintViolations()) {
                result.append(" ");
                result.append(violation.getMessage());
                result.append(".");
            }
        }

        return result.toString();
    }
}
