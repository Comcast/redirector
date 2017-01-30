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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.auth;

import com.comcast.redirector.api.auth.model.AccessMode;
import com.comcast.redirector.api.auth.model.GranularPermission;
import com.comcast.redirector.api.auth.model.Permission;
import com.comcast.redirector.api.filter.Request;
import com.comcast.redirector.common.RedirectorConstants;

import javax.ws.rs.HttpMethod;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.comcast.redirector.api.auth.PermissionHelper.GranularPermissionResult.*;

/**
 * Static (non-granular) permission helper, which determines if user has access to the given url.
 * It is designed to support both ACL and SAT as long as they have synchronized permission sets.
 *
 * Permission format
 * [RESTOnly-]{access mode}-{endpoint}[-{entity}]
 *
 * where access mode in: read, write, allow-write, deny-write, allow-read, deny-read, redirector-accessApp
 *
 * Permission examples:
 * read-urlRules
 * read-* (grants read permission to all endpoints)
 * RESTOnly-read-applicationNames (for those functions that appear only in REST. not in the UI)
 * write-urlRules
 * write-* (grants write permission to all endpoints)
 * RESTOnly-deny-write-namespacedLists-someName
 * RESTOnly-allow-write-namespacedLists-someName
 * RESTOnly-deny-read-namespacedLists-someName
 * RESTOnly-allow-read-namespacedLists-someName
 * granular permission override general ones, e.g. write-namespacedLists + deny-write-namespacedLists-someName results in denying writes to this NSList
 * granular deny overrides granular allow (in case if both are present)
 *
 * AccessApp permissions:
 * redirector-accessApp-xreGuide
 * redirector-accessApp-pandora
 *
 */
class PermissionHelper {

    private static Set<Request> nonFilterableRequests = Stream.of(
        new Request(RedirectorConstants.NAMESPACE_CONTROLLER_PATH + "/duplicates", HttpMethod.POST),
        new Request(RedirectorConstants.NAMESPACE_CONTROLLER_PATH + "/validate", HttpMethod.POST),
        new Request(RedirectorConstants.DELIMETER + "preview/distribution/", HttpMethod.POST),

        new Request(RedirectorConstants.NAMESPACE_CONTROLLER_PATH, HttpMethod.GET),
        new Request(RedirectorConstants.NAMESPACE_CONTROLLER_PATH + "/getAllNamespacedListsWithoutValues", HttpMethod.GET)
    ).collect(Collectors.toSet());

    private static Map<String, String> endpointsToReadPermissionMap = new LinkedHashMap<>();
    private static Map<String, String> endpointsToWritePermissionMap = new LinkedHashMap<>();

    static {
        endpointsToReadPermissionMap.put(RedirectorConstants.WHITELISTED_CONTROLLER_PATH, "read-whitelist");
        endpointsToReadPermissionMap.put(RedirectorConstants.STACK_COMMENTS_CONTROLLER_PATH, "read-whitelist");

        endpointsToWritePermissionMap.put(RedirectorConstants.WHITELISTED_CONTROLLER_PATH, "write-whitelist");
        endpointsToWritePermissionMap.put(RedirectorConstants.STACK_COMMENTS_CONTROLLER_PATH, "write-whitelist");
    }

    private static Set<String> granularPermissionControllableEndpoints = new HashSet<String>(){{
        add(RedirectorConstants.NAMESPACE_CONTROLLER_PATH.replace("/", ""));
    }};

    private static AccessAppPermissionHelper accessAppPermissionHelper = new AccessAppPermissionHelper();

    static Boolean isAuthorized (String url, String method, Collection<String> permissions) {
        if (Permission.rawPermissionsContainPermitAll(permissions)) {
            return true;
        }

        Request request = new Request(url, method);
        if (!accessAppPermissionHelper.isAuthorizedToAccessApp(request, permissions)) {
            return false;
        }
        return isAuthorizedInternal(request, permissions);
    }

    static Boolean isAuthorizedDisregardingAccessAppPermissions(String url, String method, Collection<String> permissions) {
        if (Permission.rawPermissionsContainPermitAll(permissions)) {
            return true;
        }

        Request request = new Request(url, method);
        return isAuthorizedInternal(request, permissions);
    }

    private static Boolean isAuthorizedInternal(Request request, Collection<String> permissions) {
        GranularPermissionResult granularPermissionStatus = authorizeGranular(request, permissions);
        if (granularPermissionStatus == NONE) {
            return isAuthorizedStatic(request, permissions);
        }
        return granularPermissionStatus == ALLOWED;
    }

    private static Boolean isAuthorizedStatic(Request request, Collection<String> permissions) {
        boolean isNonFilterableUrl = nonFilterableRequests.stream()
            .filter(request::isMethodEquals)
            .filter(request::isUrlMatchingRequestUrl)
            .findFirst().isPresent();

        return isNonFilterableUrl || permissions.stream()
            .map(Permission::new)
            .filter(Permission::isValid)
            .filter(Permission::isRegular)
            .filter(permission -> (request.isUrlMatchingEndpoint(permission.getEndpoint()))
                    || isMatchedByReadOrWritePermissionsMap(request, permission))
            .filter(request::accessModeEquals)
            .findFirst().isPresent();
    }

    private static boolean isMatchedByReadOrWritePermissionsMap (Request request, Permission permission) {
        return  isMatchedByPermissionMap(request, permission, endpointsToReadPermissionMap)
                || isMatchedByPermissionMap(request, permission, endpointsToWritePermissionMap);
    }

    private static boolean isMatchedByPermissionMap (Request request, Permission permission, Map<String, String> permissionMap) {
        return permissionMap.keySet().stream()
                .filter(urlFromMap -> request.getNormalizedUrl().contains(urlFromMap))
                .filter(key -> permission.equals(new Permission(permissionMap.get(key))))
                .findFirst().isPresent();
    }

    private static GranularPermissionResult authorizeGranular(Request request, Collection<String> permissions) {
        if (! isEligibleForGranularPermissions(request)) {
            return NONE;
        }

        Optional<GranularPermissionResult> result = permissions.stream()
            .map(Permission::new)
            .filter(Permission::isValid)
            .filter(Permission::isGranular)
            .filter(permission -> request.isUrlMatchingEndpoint(permission.getEndpoint()))
            .filter(permission -> request.isUrlMatchingEntity(permission.getEntity()))
            .map(permission -> {
                AccessMode denyAccessMode =
                    AccessMode.fromGranularPermissionAndHttpMethod(GranularPermission.DENY , request.getMethod());
                AccessMode allowAccessMode =
                    AccessMode.fromGranularPermissionAndHttpMethod(GranularPermission.ALLOW, request.getMethod());

                if (permission.getAccessMode() == denyAccessMode) {
                    return DENIED;
                } else if (permission.getAccessMode() == allowAccessMode) {
                    return ALLOWED;
                } else {
                    return NONE;
                }
            })
            .filter(granularPermissionResult -> granularPermissionResult != NONE)
            .findFirst();

        return result.isPresent() ? result.get() : NONE;
    }

    private static boolean isEligibleForGranularPermissions(Request request) {
        return granularPermissionControllableEndpoints.stream()
            .filter(url -> request.getNormalizedUrl().contains(url))
            .findFirst().isPresent();
    }

    enum GranularPermissionResult {
        NONE, ALLOWED, DENIED
    }
}
