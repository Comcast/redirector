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

package com.comcast.redirector.api.exceptionmapper;

import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.NoConnectionToDataSourceException;
import com.comcast.redirector.dataaccess.client.NoNodeInZookeeperException;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.api.model.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RedirectorDataSourceExceptionMapper implements ExceptionMapper<RedirectorDataSourceException> {
    private static final Logger log = LoggerFactory.getLogger(RedirectorDataSourceExceptionMapper.class);

    @Override
    public Response toResponse(RedirectorDataSourceException exception) {
        log.error("Exception happened in RedirectorWebService", exception);

        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
            .entity(new ErrorMessage(getHumanReadableMessage(exception)))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private String getHumanReadableMessage (RedirectorDataSourceException exception) {
        if (exception.getCause() != null) {
            Throwable cause = exception.getCause();
            if (NoConnectionToDataSourceException.class.isAssignableFrom(cause.getClass()) ||
                    NoNodeInZookeeperException.class.isAssignableFrom(cause.getClass()) ||
                    DataSourceConnectorException.class.isAssignableFrom(cause.getClass())) {
                return cause.getMessage();
            }
        }

        return exception.getMessage();
    }

}
