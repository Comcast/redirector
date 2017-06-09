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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector.helpers;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

public class WhitelistedHelper {

    private static final String WHITELISTED_SERVICE_PATH = RedirectorConstants.WHITELISTED_CONTROLLER_PATH;

    public static final String _PREVIEW = "preview";
    public static final String _STACKMANAGEMENT = "stackmanagement";

    public static final Whitelisted WHITELISTED;

    public static final String DC1_REGION1 = "/DataCenter1/Region1";
    public static final String DC2_REGION1 = "/DataCenter2/Region1";
    public static final String DC1_REGION2 = "/DataCenter1/Region2";
    public static final String DC2_REGION2 = "/DataCenter2/Region2";
    public static final String DC2_ZONE2 = "/DataCenter2/Zone2";
    public static final String[] defaultWhiteList = new String[] {DC1_REGION1, DC2_REGION1, DC1_REGION2, DC2_REGION2, DC2_ZONE2};
    static {
        WHITELISTED = createDefaultWhitelisted();
    }

    private static final String _ADD_STACKS = "addStacks";

    public static Whitelisted post(String serviceName, Whitelisted whitelisted, String mediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(WHITELISTED_SERVICE_PATH).path(serviceName);
        return ServiceHelper.post(webTarget, whitelisted, mediaType, Whitelisted.class, true);
    }

    /**
     * simple post - without checking response and specific mediaType
     */
    public static Whitelisted post(String serviceName, Whitelisted whitelisted) {
        return post(serviceName, whitelisted, MediaType.APPLICATION_JSON);
    }

    public static void postAndApprove(WebTarget target, String serviceName, Whitelisted whitelisted) {
        post(serviceName, whitelisted);
        approvePendingChanges(serviceName);
    }

    public static Whitelisted get(String serviceName, String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(WHITELISTED_SERVICE_PATH).path(serviceName);
        return ServiceHelper.get(webTarget, responseMediaType, Whitelisted.class);
    }

    /**
     * simple get - without checking specific mediaType
     */
    public static Whitelisted get(WebTarget target, String serviceName) {
        return get(serviceName, MediaType.APPLICATION_JSON);
    }

    public static <T> T addStacks(WebTarget target, String serviceName, T whitelistedStacks, String mediaType) {
        WebTarget webTarget = target.path(WHITELISTED_SERVICE_PATH).path(serviceName).path(_ADD_STACKS);
        return ServiceHelper.put(webTarget, whitelistedStacks, mediaType);
    }

    public static void deleteStacks(WebTarget target, String serviceName, String whitelistedStacks) {
        WebTarget webTarget = target.path(WHITELISTED_SERVICE_PATH).path(serviceName).path(whitelistedStacks);
        ServiceHelper.delete(webTarget);
    }

    public static void makeWhitelistedWithUpdatedStatus(String serviceName, Whitelisted whitelisted,
                                                        Whitelisted updatedWhitelisted) throws AssertionError {
        // post our whitelisted
        post(serviceName, whitelisted);

        // approve whitelisted
        approvePendingChanges(serviceName);

        // post updated whitelisted
        post(serviceName, updatedWhitelisted);
    }

    /**
     * @return Whitelisted object that needed for post and comparing with all responses: <p>
     * <pre>
     *  {@code
     *    <whitelisted>
     *      <paths>/DC0/Stack0</paths>
     *      <paths>/DC1/Stack1</paths>
     *    </whitelisted>
     *  }
     * </pre>
     */
    public static Whitelisted createDefaultWhitelisted() {
        return createWhitelisted(
                "/DC0/Stack0",
                "/DC1/Stack1");
    }

    @Deprecated // use WebServiceModelBuilders.createWhitelisted instead
    public static Whitelisted createWhitelisted(String... paths) {
        Whitelisted whitelisted = new Whitelisted();
        whitelisted.setPaths(Arrays.asList(paths));
        return whitelisted;
    }

// ************************************* WHITELISTED PENDING CHANGES  *************************************** //

    public static <T> T getPendingPreview(String serviceName, String responseMediaType, Class<T> responseClassType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_PREVIEW).path(_STACKMANAGEMENT);
        return ServiceHelper.get(webTarget, responseMediaType, responseClassType);
    }

    public static void approvePendingChanges(String serviceName) throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = HttpTestServerHelper.target().path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_STACKMANAGEMENT).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.approvePendingChanges(webTarget);
    }

    public static void cancelPendingChanges(String serviceName) throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = HttpTestServerHelper.target().path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_STACKMANAGEMENT).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.cancelPendingChanges(webTarget);
    }

}
