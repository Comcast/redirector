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

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.distribution.Distribution;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class DistributionHelper {

    private static final String DISTRIBUTION_SERVICE_PATH = RedirectorConstants.DISTRIBUTION_CONTROLLER_PATH;

    private static final String _PREVIEW = "preview";
    private static final String _DISTRIBUTION = "distribution";

    public static Distribution post(String serviceName, Distribution payloadObject, String mediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(DISTRIBUTION_SERVICE_PATH).path(serviceName);
        return ServiceHelper.post(webTarget, payloadObject, mediaType, Distribution.class, true);
    }

    /**
     * simple post - without checking response and specific mediaType
     */
    public static Distribution post(String serviceName, Distribution distribution) {
        return post(serviceName, distribution, MediaType.APPLICATION_JSON);
    }

    public static Distribution get(String serviceName, String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(DISTRIBUTION_SERVICE_PATH).path(serviceName);
        return ServiceHelper.get(webTarget, responseMediaType, Distribution.class);
    }

    /**
     * simple get - without checking specific mediaType
     */
    public static Distribution get(String serviceName) {
        return get(serviceName, MediaType.APPLICATION_JSON);
    }

    public static void makeDistributionWithUpdatedStatus(WebTarget target, String serviceName, Distribution distribution, Distribution updatedDistribution)
            throws AssertionError {
        // post our distribution
        post(serviceName, distribution);

        // approve distribution
        approvePendingChanges(target, serviceName);

        // post updated distribution
        post(serviceName, updatedDistribution);
    }

    public static void deleteAll(WebTarget target, String serviceName) {
        WebTarget webTarget = target.path(DISTRIBUTION_SERVICE_PATH).path(serviceName);
        Distribution distribution = new Distribution();
        // post empty distribution
        ServiceHelper.post(webTarget, distribution, MediaType.APPLICATION_JSON);
    }

 // ************************************* DISTRIBUTION PENDING CHANGES  *************************************** //

    public static Distribution getPendingPreview(WebTarget target, String serviceName,
                                                     String responseMediaType) {
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_PREVIEW).path(_DISTRIBUTION);
        return ServiceHelper.get(webTarget, responseMediaType, Distribution.class);
    }

    public static void approvePendingChanges(WebTarget target, String serviceName) throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_DISTRIBUTION).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.approvePendingChanges(webTarget);
    }

    public static void cancelPendingChanges(WebTarget target, String serviceName) throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_DISTRIBUTION).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.cancelPendingChanges(webTarget);
    }

}
