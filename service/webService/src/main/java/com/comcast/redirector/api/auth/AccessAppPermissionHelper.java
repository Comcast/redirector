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

import com.comcast.redirector.api.auth.model.Permission;
import com.comcast.redirector.api.filter.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.comcast.redirector.common.DeciderConstants.DECIDER_PATH;
import static com.comcast.redirector.common.RedirectorConstants.*;

//toDo: replace it with more elegant solution
class AccessAppPermissionHelper {
    private static Logger log = LoggerFactory.getLogger(AccessAppPermissionHelper.class);

    private static final List<String> ENDPOINTS_WITHOUT_APP_NAME = Arrays.asList(
        AUTH_CONTROLLER_PATH, REDIRECTOR_CONTROLLER_PATH, NAMESPACE_CONTROLLER_PATH, SETTINGS,
        CORE_BACKUP_PENDING_CONTROLLER_PATH, REDIRECTOR_OFFLINE_CONTROLLER_PATH,
        STACKS_CONTROLLER_PATH_DELETE_ALL_INACTIVE,

        DECIDER_PATH
    );

    boolean isAuthorizedToAccessApp(Request request, Collection<String> permissions) {
        String normalizedUrl = request.getNormalizedUrl();

        if (isAvailableForAnyApp(normalizedUrl)) {
            return true;
        }

        List<Permission> accessAppPermissions = getAccessAppPermissions(permissions);

        if (Request.isUrlStartingWithOneOfEndpointsInList(normalizedUrl, Collections.singletonList(TRAFFIC_PATH))) {
            return checkPermissionByUrl(normalizedUrl, accessAppPermissions);
        }

        Optional<String> application = Request.AppNameExtractionStrategies.stream()
            .filter(strategy -> strategy.isEligible(normalizedUrl))
            .map(request::getApplicationFromUrl)
            .findFirst();

        if (application.isPresent()) {
            Permission permissionToCheck = Permission.accessAppPermission(application.get());
            return accessAppPermissions.stream().filter(Predicate.isEqual(permissionToCheck)).count() > 0;
        } else {
            log.warn("We got url which is not registered in accessApp permission, access is granted: {}", normalizedUrl);
            return true;
        }
    }

    private boolean isAvailableForAnyApp(String url) {
        return Request.isUrlStartingWithOneOfEndpointsInList(url, ENDPOINTS_WITHOUT_APP_NAME);
    }

    private List<Permission> getAccessAppPermissions(Collection<String> permissions) {
        return permissions.stream()
            .map(Permission::new)
            .filter(Permission::isValid)
            .filter(Permission::isAccessApp)
            .collect(Collectors.toList());
    }

    private static Boolean checkPermissionByUrl(String url, List<Permission> permissions) {
        for (Permission permission : permissions) {
            if (url.contains(permission.getApplicationName())) {
                return true;
            }
        }
        return false;
    }
}
