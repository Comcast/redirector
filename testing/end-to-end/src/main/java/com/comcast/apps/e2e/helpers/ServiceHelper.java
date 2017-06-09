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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

// TODO: this helper is overcomplicated. Refactor
public class ServiceHelper {
    private static final Logger log = LoggerFactory.getLogger(ServiceHelper.class);

    private final Client client = ClientBuilder.newClient();
    private static final String APP_NAME_PARAM = "appName";

    private String baseUrl;
    private String appName;

    public ServiceHelper(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ServiceHelper(String appName, String baseUrl) {
        this(baseUrl);
        this.appName = appName;

        client.register((ClientResponseFilter) (requestContext, responseContext) ->
                log.info("Response for uri: {}, method: {}, status: {}",
                        requestContext.getUri(), requestContext.getMethod(), responseContext.getStatus()));
    }

    public Invocation.Builder getRequestBuilder(String path, String acceptedResponseTypes) {
        return getWebTarget(path).request(acceptedResponseTypes);
    }

    public WebTarget getWebTarget() {
        return client.target(baseUrl);
    }

    private WebTarget getWebTarget(String path) {
        return (appName == null) ? client.target(baseUrl).path(path) : client.target(baseUrl).path(path).queryParam(APP_NAME_PARAM, appName);
    }

    public <T> void post(String path, T payloadObject, String mediaType) {
        WebTarget webTargetWithPath = getWebTarget(path);
        Response response = webTargetWithPath.request(mediaType).post(createEntity(payloadObject, mediaType));
        if (!isValidStatus(response.getStatus())) {
            throw new WebApplicationException(response.getStatus());
        }
    }

    public <T> T get(String path, Class<T> payloadClass , String mediaType) {
        WebTarget webTargetWithPath = getWebTarget(path);
        Response response = webTargetWithPath.request(mediaType).get();
        return response.readEntity(payloadClass);
    }

    private <T> Entity<T> createEntity(T entityObject, String mediaType) {
        if (entityObject instanceof String) {
            if (MediaType.APPLICATION_JSON.equals(mediaType)) {
                return Entity.json(entityObject);
            } else {
                return Entity.xml(entityObject);
            }
        } else {
            return Entity.entity(entityObject, mediaType);
        }
    }

    boolean isValidStatus(int status) {
        return status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_CREATED;
    }
}
