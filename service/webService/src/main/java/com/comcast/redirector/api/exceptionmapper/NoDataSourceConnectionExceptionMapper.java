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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.exceptionmapper;

import com.comcast.redirector.dataaccess.client.NoConnectionToDataSourceException;
import com.comcast.redirector.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NoDataSourceConnectionExceptionMapper implements ExceptionMapper<NoConnectionToDataSourceException> {
    private static final Logger log = LoggerFactory.getLogger(NoDataSourceConnectionExceptionMapper.class);

    @Override
    public Response toResponse(NoConnectionToDataSourceException exception) {
        log.error("Exception happened in RedirectorWebService", exception);

        Metrics.reportError500(exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
