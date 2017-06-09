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

import com.comcast.redirector.api.model.RuleIdsWrapper;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import org.junit.Assert;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

/**
 *
 * PendingChangesHelper contains common calls for our objects from PendingChangesController(distribution, rules, urlRules,...) <p>
 * Pending changes calls for concrete objects located in the other helpers
 *
 */
public class PendingChangesHelper {

    public static final String PENDING_SERVICE_PATH = RedirectorConstants.PENDING_CONTROLLER_PATH;

    public static final String _APPROVE = "approveWithoutValidation";
    public static final String _CANCEL = "cancel";

    public static void approveAllPendingChanges(WebTarget target, String serviceName) throws AssertionError {
        Integer currentChangeVersion = getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PENDING_SERVICE_PATH).path(serviceName)
                .path(_APPROVE).path(String.valueOf(currentChangeVersion));
        Response response = webTarget.request().post(null);
        // expect HTTP/1.1 200
        String message = response.readEntity(String.class);
        Assert.assertEquals("Response status code assertion error [" + message + "] during Approve  pending change:",
                HttpURLConnection.HTTP_OK, response.getStatus());
    }

    public static void cancelAllPendingChanges(WebTarget target, String serviceName) throws AssertionError {
        Integer currentChangeVersion = getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PENDING_SERVICE_PATH).path(serviceName)
                .path(_CANCEL).path(String.valueOf(currentChangeVersion));
        Response response = webTarget.request().post(null);
        // expect HTTP/1.1 200
        String message = response.readEntity(String.class);
        Assert.assertEquals("Response status code assertion error [" + message + "] during Cancel All pending changes:",
                HttpURLConnection.HTTP_OK, response.getStatus());
    }
    public static PendingChangesStatus getAllPendingChanges(String serviceName, String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(PENDING_SERVICE_PATH).path(serviceName);
        return ServiceHelper.get(webTarget, responseMediaType, PendingChangesStatus.class);
    }

    public static Integer getCurrentChangeVersion(String serviceName) throws AssertionError {
        WebTarget webTarget = HttpTestServerHelper.target().path(PENDING_SERVICE_PATH).path(serviceName);
        Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
        // expect HTTP/1.1 200
        if (response.getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR
                || response.getStatusInfo().getFamily() == Response.Status.Family.SERVER_ERROR) {
            String message = response.readEntity(String.class);
            Assert.fail("Response status code assertion error [" + message + "] during get change version.");
        }

        PendingChangesStatus currentChanges = response.readEntity(PendingChangesStatus.class);
        return currentChanges.getVersion();
    }

// ========================= Pending changes for concrete objects located in the other helpers =============

    public static void approvePendingChanges(WebTarget webTargetWithPath) throws AssertionError {
        // path should contain changeVersion
        Response response = webTargetWithPath.request().post(null);
        // expect HTTP/1.1 200
        String message = response.readEntity(String.class);
        Assert.assertEquals("Response status code assertion error [" + message + "] during Approve pending changes:",
                HttpURLConnection.HTTP_OK, response.getStatus());
    }

    public static void cancelPendingChanges(WebTarget webTargetWithPath) throws AssertionError {
        Response response = webTargetWithPath.request().delete();
        // expect HTTP/1.1 200
        String message = response.readEntity(String.class);
        Assert.assertEquals("Response status code assertion error [" + message + "] during Cancel pending changes:",
                HttpURLConnection.HTTP_OK, response.getStatus());
    }

    public static RuleIdsWrapper getNewRuleIds (String serviceName, String objectType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(PENDING_SERVICE_PATH).path(serviceName)
                .path("newRuleIds").path(objectType);
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, RuleIdsWrapper.class);
    }

    public static Distribution getDistributionPendingChangesPreview(String serviceName) {
        WebTarget webTarget = HttpTestServerHelper.target().path(PENDING_SERVICE_PATH).path(serviceName)
                .path("preview").path("distribution");
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, Distribution.class);
    }

    public static Whitelisted getWhitelistedPendingChangesPreview(String serviceName) {
        WebTarget webTarget = HttpTestServerHelper.target().path(PENDING_SERVICE_PATH).path(serviceName)
                .path("preview").path("stackmanagement");
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, Whitelisted.class);
    }
}
