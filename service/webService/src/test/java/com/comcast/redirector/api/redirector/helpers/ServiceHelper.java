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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector.helpers;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ServiceHelper {

    public static <T> T post(WebTarget webTargetWithPath, T payloadObject, String mediaType) {
        return post(webTargetWithPath, payloadObject, mediaType, payloadObject.getClass());
    }

    public static <T, R> R post(WebTarget webTargetWithPath, T payloadObject, String mediaType, Class<?> responseType) {
        return post(webTargetWithPath, payloadObject, mediaType, responseType, false);
    }

    public static <T, R> R post(WebTarget webTargetWithPath, T payloadObject, String mediaType, Class<?> responseType, boolean suppressWebApplicationException) {
        Response response = webTargetWithPath.request(mediaType).post(createEntity(payloadObject, mediaType));
        if (!suppressWebApplicationException) {
            if (response.getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR ||
                    response.getStatusInfo().getFamily() == Response.Status.Family.SERVER_ERROR) {
                String message = response.readEntity(String.class);
                throw new WebApplicationException(message, response);
            }
        }
        return (R) response.readEntity(responseType);
    }

    public static <T> Response postAndGetRawResponse(WebTarget webTargetWithPath, T payloadObject, String mediaType) {
        return webTargetWithPath.request(mediaType).post(createEntity(payloadObject, mediaType));
    }

    public static <T> T put(WebTarget webTargetWithPath, T payloadObject, String mediaType) {
        return put(webTargetWithPath, payloadObject, mediaType, false);
    }
    public static <T> T put(WebTarget webTargetWithPath, T payloadObject, String mediaType, boolean suppressWebApplicationException) {
        Response response = webTargetWithPath.request(mediaType).put(createEntity(payloadObject, mediaType));
        if (!suppressWebApplicationException) {
            if (response.getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR ||
                    response.getStatusInfo().getFamily() == Response.Status.Family.SERVER_ERROR) {
                String message = response.readEntity(String.class);
                throw new WebApplicationException(message, response);
            }
        }
        return (T) response.readEntity(payloadObject.getClass());
    }

    public static <T> Entity<T> createEntity(T entityObject, String mediaType) {
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

    public static <T> T get(WebTarget webTargetWithPath, String responseMediaType, Class<T> responseClassType) {
        return get(webTargetWithPath, responseMediaType, responseClassType, false);
    }
    public static <T> T get(WebTarget webTargetWithPath, String responseMediaType, Class<T> responseClassType, boolean suppressWebApplicationException) {
        Response response = webTargetWithPath.request(responseMediaType).get();
        if (!suppressWebApplicationException) {
            if (response.getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR ||
                    response.getStatusInfo().getFamily() == Response.Status.Family.SERVER_ERROR) {
                String message = response.readEntity(String.class);
                throw new WebApplicationException(message, response);
            }
        }
        return (T) response.readEntity(responseClassType);
    }

    public static void delete(WebTarget webTargetWithPath) {
        webTargetWithPath.request().delete();
    }

    public static int deleteWithResponce (WebTarget webTargetWithPath) {
       return webTargetWithPath.request().delete().getStatus();
    }

    public static <R> R  delete(WebTarget webTargetWithPath, Class<?> responseType) {
        return delete(webTargetWithPath, responseType, false);
    }

    public static <R> R  delete(WebTarget webTargetWithPath, Class<?> responseType, boolean suppressWebApplicationException) {
        Response response = webTargetWithPath.request().delete();
        if (!suppressWebApplicationException) {
            if (response.getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR ||
                    response.getStatusInfo().getFamily() == Response.Status.Family.SERVER_ERROR) {
                String message = response.readEntity(String.class);
                throw new WebApplicationException(message, response);
            }
        }
        return (R) response.readEntity(responseType);
    }

}
