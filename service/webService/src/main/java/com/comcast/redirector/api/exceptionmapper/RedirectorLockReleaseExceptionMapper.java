/*
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
 * @author Alexander Ievstratiev (oievstratiev@productengine.com)
 */
package com.comcast.redirector.api.exceptionmapper;

import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.dataaccess.client.RedirectorLockReleaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RedirectorLockReleaseExceptionMapper implements ExceptionMapper<RedirectorLockReleaseException> {
    private static final Logger log = LoggerFactory.getLogger(RedirectorDataSourceExceptionMapper.class);
    
    @Override
    public Response toResponse(RedirectorLockReleaseException exception) {
        log.error("FATAL: LOCK RELEASE FAILED", exception);
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessage("FATAL: LOCK RELEASE FAILED"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
